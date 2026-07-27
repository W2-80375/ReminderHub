package com.own.remindme.utils.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.own.remindme.MainActivity
import kotlin.math.absoluteValue
import com.own.remindme.data.local.NotificationDao
import com.own.remindme.data.local.NotificationEntity
import com.own.remindme.data.local.ReminderDao
import com.own.remindme.data.mapper.toDomain
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.model.Priority
import com.own.remindme.domain.model.RepeatType
import com.own.remindme.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationDao: NotificationDao

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("ID", 0)
        android.util.Log.d("NotificationReceiver", "--- onReceive Start --- ID=$notificationId Action=${intent.action}")
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            android.util.Log.d("NotificationReceiver", "Boot completed, rescheduling all notifications")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val reminders = reminderDao.getPendingReminders().first()
                    reminders.forEach { entity ->
                        val reminder = entity.toDomain()
                        reminder.reminderTimes.forEachIndexed { index, time ->
                            NotificationScheduler.scheduleNotification(
                                context = context,
                                id = reminder.id.toInt() * 10 + index,
                                title = reminder.title,
                                message = reminder.description,
                                timeInMillis = time,
                                repeatType = reminder.repeatType,
                                priority = reminder.priority,
                                category = reminder.category,
                                originalReminderId = reminder.id
                            )
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NotificationReceiver", "Error rescheduling on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        val title = intent.getStringExtra("TITLE") ?: "Reminder"
        val message = intent.getStringExtra("MESSAGE") ?: "You have a reminder"
        val frequency = intent.getStringExtra("REPEAT_TYPE") ?: "None"
        val categoryNameFromIntent = intent.getStringExtra("CATEGORY") ?: "CUSTOM"

        android.util.Log.d("NotificationReceiver", "Received notification: ID=$notificationId, Title='$title'")

        val originalId = intent.getLongExtra("REMINDER_ID", -1L)
        
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val preferences = preferencesRepository.userPreferencesFlow.first()
                
                // Identify the type of notification
                // 0: Primary, 1: Follow-up 1, 2: Follow-up 2, 3: Emergency, 5: Expiry
                val notificationType = intent.getIntExtra("NOTIFICATION_TYPE", -1).let {
                    if (it != -1) it else {
                        // Fallback to legacy ID-based detection if extra is missing
                        when {
                            notificationId >= 500000000 -> 5
                            notificationId >= 300000000 -> 3
                            notificationId >= 200000000 -> 2
                            notificationId >= 100000000 -> 1
                            // Old offsets (1M, 2M, 3M, 5M)
                            notificationId >= 5000000 -> 5
                            notificationId >= 3000000 -> 3
                            notificationId >= 2000000 -> 2
                            notificationId >= 1000000 -> 1
                            else -> 0
                        }
                    }
                }
                
                android.util.Log.d("NotificationReceiver", "Notification Received: ID=$notificationId, Type=$notificationType")
                
                val isPrimary = notificationType == 0
                val isEmergency = notificationType == 3
                val isExpiry = notificationType == 5

                // Recover reminder ID if not provided in extras
                val finalReminderId = if (originalId == -1L) {
                    // Strip the offset to get (reminderId * 10 + index)
                    val baseId = when {
                        notificationId >= 500000000 -> notificationId - 500000000
                        notificationId >= 300000000 -> notificationId - 300000000
                        notificationId >= 200000000 -> notificationId - 200000000
                        notificationId >= 100000000 -> notificationId - 100000000
                        // Old offsets
                        notificationId >= 5000000 -> notificationId - 5000000
                        notificationId >= 3000000 -> notificationId - 3000000
                        notificationId >= 2000000 -> notificationId - 2000000
                        notificationId >= 1000000 -> notificationId - 1000000
                        else -> notificationId
                    }
                    val rid = (baseId / 10).toLong()
                    android.util.Log.d("NotificationReceiver", "Recovered Reminder ID $rid from notification ID $notificationId")
                    rid
                } else {
                    originalId
                }

                val reminder = if (finalReminderId != -1L) {
                    val r = reminderDao.getReminderById(finalReminderId)?.toDomain()
                    if (r == null) android.util.Log.w("NotificationReceiver", "Reminder ID $finalReminderId NOT found in DB")
                    r
                } else null
                
                android.util.Log.d("NotificationReceiver", "Processing: Title='$title', Reminder='${reminder?.title}', Category=${reminder?.category}")

                // Handle Emergency Alert (T+15)
                if (isEmergency) {
                    if (reminder?.category == Category.MEDICINE && 
                        reminder.priority == Priority.HIGH &&
                        preferences.emergencyContact.isNotBlank()
                    ) {
                        val lastTaken = reminder.lastTakenTimestamp
                        val alreadyTaken = if (lastTaken != null) {
                            val cal1 = Calendar.getInstance().apply { timeInMillis = lastTaken }
                            val cal2 = Calendar.getInstance()
                            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
                        } else false

                        if (!alreadyTaken) {
                            android.util.Log.d("NotificationReceiver", "Executing emergency alert for ${reminder.title}")
                            sendEmergencyAlert(context, preferences.emergencyContact, reminder.title)
                        }
                    }
                    return@launch 
                }

                // Check if already taken for medicine category
                if (reminder?.category == Category.MEDICINE) {
                    val lastTaken = reminder.lastTakenTimestamp
                    val alreadyTaken = if (lastTaken != null) {
                        val cal1 = Calendar.getInstance().apply { timeInMillis = lastTaken }
                        val cal2 = Calendar.getInstance()
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
                    } else false

                    if (alreadyTaken) {
                        android.util.Log.d("NotificationReceiver", "Medicine '${reminder.title}' already taken today. Skipping.")
                        // If it's a primary repeating notification, still reschedule for tomorrow
                        if (isPrimary && reminder.repeatType != RepeatType.NONE) {
                            NotificationScheduler.scheduleNotification(
                                context = context,
                                id = notificationId,
                                title = reminder.title,
                                message = reminder.description,
                                timeInMillis = System.currentTimeMillis(),
                                repeatType = reminder.repeatType,
                                priority = reminder.priority,
                                category = reminder.category,
                                originalReminderId = reminder.id,
                                includeFollowUps = false
                            )
                        }
                        return@launch
                    }
                }

                // Only insert into notification history for primary, follow-ups, and expiry notifications
                if (isPrimary || isExpiry || notificationType == 1 || notificationType == 2) {
                    notificationDao.insertNotification(
                        NotificationEntity(
                            title = title,
                            message = message,
                            timestamp = System.currentTimeMillis(),
                            frequency = reminder?.repeatType?.name ?: frequency,
                            category = reminder?.category?.name ?: categoryNameFromIntent,
                            isRead = false
                        )
                    )
                }

                // Reschedule next occurrence only for the primary notification
                if (reminder != null && reminder.repeatType != RepeatType.NONE && isPrimary) {
                    NotificationScheduler.scheduleNotification(
                        context = context,
                        id = notificationId,
                        title = reminder.title,
                        message = reminder.description,
                        timeInMillis = System.currentTimeMillis(),
                        repeatType = reminder.repeatType,
                        priority = reminder.priority,
                        category = reminder.category,
                        originalReminderId = reminder.id,
                        includeFollowUps = false
                    )
                }

                // If it's a primary MEDICINE reminder firing now, schedule TODAY'S follow-ups
                if (reminder?.category == Category.MEDICINE && isPrimary) {
                    NotificationScheduler.scheduleMedicineFollowUps(
                        context = context,
                        id = notificationId,
                        title = reminder.title,
                        message = reminder.description,
                        baseTime = System.currentTimeMillis(),
                        repeatType = reminder.repeatType,
                        priority = reminder.priority,
                        originalReminderId = reminder.id
                    )
                }

                val activityIntent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    activityIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                val soundKey = if (isExpiry) "MEDICINE_EXPIRY" else (reminder?.category?.name ?: "DEFAULT")
                val soundPath = preferences.categorySounds[soundKey] ?: preferences.categorySounds["DEFAULT"]
                
                val soundFile = soundPath?.let { File(it) }
                android.util.Log.d("NotificationReceiver", "Checking sound: $soundPath, exists=${soundFile?.exists()}")

                val soundUri = if (soundFile != null && soundFile.exists()) {
                    try {
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            soundFile
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("NotificationReceiver", "Error getting FileProvider URI", e)
                        null
                    }
                } else null

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                val isMedicine = reminder?.category == Category.MEDICINE
                val baseChannelId = if (isMedicine) "medicine_reminders" else "reminders"
                val channelName = if (isMedicine) "Medicine Reminders" else "General Reminders"
                
                // On Android 8.0+, sound is set on the CHANNEL, not the notification.
                // We use a dynamic channel ID based on the sound path AND its modified time
                // to ensure that when a user records a new sound with the same filename, 
                // a new channel is created and the system picks up the change.
                val lastModified = soundFile?.lastModified() ?: 0L
                val soundSuffix = if (soundFile != null && soundFile.exists()) {
                    "${soundPath?.hashCode()?.absoluteValue}_$lastModified"
                } else {
                    "default"
                }
                val finalChannelId = "${baseChannelId}_$soundSuffix"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Check if this specific channel already exists
                    if (notificationManager.getNotificationChannel(finalChannelId) == null) {
                        android.util.Log.d("NotificationReceiver", "Creating new channel for sound: $finalChannelId")
                        // Use HIGH importance if there's a custom sound to ensure it's audible
                        val importance = if (isMedicine || soundUri != null) 
                            NotificationManager.IMPORTANCE_HIGH 
                        else 
                            NotificationManager.IMPORTANCE_DEFAULT
                            
                        val channel = NotificationChannel(finalChannelId, channelName, importance).apply {
                            description = if (isMedicine) "Critical alerts for medicine intake" else "Standard task and event reminders"
                            enableLights(true)
                            enableVibration(true)
                            
                            if (soundUri != null) {
                                val audioAttributes = android.media.AudioAttributes.Builder()
                                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                    .build()
                                setSound(soundUri, audioAttributes)
                                
                                // Grant persistent read permission to system packages
                                listOf("com.android.systemui", "com.android.settings", "com.google.android.calendar").forEach { pkg ->
                                    try {
                                        context.grantUriPermission(pkg, soundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                        notificationManager.createNotificationChannel(channel)
                    }
                }

                val importance = if (isMedicine || soundUri != null) 
                    NotificationCompat.PRIORITY_MAX 
                else 
                    NotificationCompat.PRIORITY_HIGH
                
                val notificationBuilder = NotificationCompat.Builder(context, finalChannelId)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(importance)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                if (soundUri != null) {
                    android.util.Log.d("NotificationReceiver", "Applying custom sound URI: $soundUri")
                    notificationBuilder.setSound(soundUri)
                    // If we have a custom sound, don't use DEFAULT_SOUND which might override it
                    notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_LIGHTS)
                } else {
                    notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL)
                }

                android.util.Log.d("NotificationReceiver", "Showing notification now: ID=$notificationId")
                notificationManager.notify(notificationId, notificationBuilder.build())
            } catch (e: Exception) {
                android.util.Log.e("NotificationReceiver", "Error in onReceive", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun sendEmergencyAlert(context: Context, phoneNumber: String, medicineName: String) {
        try {
            // Send SMS
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(
                phoneNumber, 
                null, 
                "EMERGENCY: Medicine '$medicineName' has NOT been taken after multiple reminders.", 
                null, 
                null
            )
            
            // Initiate Call
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                context.startActivity(callIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
