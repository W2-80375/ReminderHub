package com.own.remindme.utils.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import com.own.remindme.MainActivity
import com.own.remindme.data.local.NotificationDao
import com.own.remindme.data.local.NotificationEntity
import com.own.remindme.data.local.ReminderDao
import com.own.remindme.data.mapper.toDomain
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.model.Priority
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
        val title = intent.getStringExtra("TITLE") ?: "Reminder"
        val message = intent.getStringExtra("MESSAGE") ?: "You have a reminder"
        val notificationId = intent.getIntExtra("ID", 0)
        val frequency = intent.getStringExtra("FREQUENCY") ?: "None"

        // For repeated alarms, the ID might be id + 2000000 or id + 3000000
        val originalId = when {
            notificationId >= 3000000 -> (notificationId - 3000000).toLong()
            notificationId >= 2000000 -> (notificationId - 2000000).toLong()
            notificationId >= 1000000 -> (notificationId - 1000000).toLong()
            else -> notificationId.toLong()
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val preferences = preferencesRepository.userPreferencesFlow.first()
                val reminder = reminderDao.getReminderById(originalId)?.toDomain()
                
                // If it's a medicine and it was already taken today, don't notify
                if (reminder?.category == Category.MEDICINE) {
                    val lastTaken = reminder.lastTakenTimestamp
                    if (lastTaken != null) {
                        val cal1 = Calendar.getInstance().apply { timeInMillis = lastTaken }
                        val cal2 = Calendar.getInstance()
                        if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
                            // Already taken today, skip notification
                            return@launch
                        }
                    }
                }

                // Emergency Logic: 3rd notification missed for high priority medicine
                if (notificationId >= 3000000 && 
                    reminder?.category == Category.MEDICINE && 
                    reminder.priority == Priority.HIGH &&
                    preferences.emergencyContact.isNotBlank()
                ) {
                    sendEmergencyAlert(context, preferences.emergencyContact, reminder.title)
                }

                notificationDao.insertNotification(
                    NotificationEntity(
                        title = title,
                        message = message,
                        timestamp = System.currentTimeMillis(),
                        frequency = frequency,
                        isRead = false
                    )
                )

                val activityIntent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId,
                    activityIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                val soundUri = if (reminder?.category == Category.MEDICINE) {
                    preferences.medicineSoundPath?.let { Uri.fromFile(File(it)) }
                } else {
                    preferences.otherSoundPath?.let { Uri.fromFile(File(it)) }
                }

                val channelId = if (reminder?.category == Category.MEDICINE) "medicine_reminders" else "reminders"
                val notificationBuilder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                
                soundUri?.let {
                    notificationBuilder.setSound(it)
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notificationId, notificationBuilder.build())
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
