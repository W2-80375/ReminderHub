package com.own.remindme.utils.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.model.Priority
import com.own.remindme.domain.model.RepeatType
import java.util.Calendar
import java.util.Date

object NotificationScheduler {
    private const val TAG = "NotificationScheduler"

    // Notification Types
    const val TYPE_PRIMARY = 0
    const val TYPE_FOLLOW_UP_1 = 1
    const val TYPE_FOLLOW_UP_2 = 2
    const val TYPE_EMERGENCY = 3
    const val TYPE_EXPIRY = 5

    // ID Generation Offsets (using large values to avoid collision with reminder IDs)
    private const val REPEAT_1_OFFSET = 100000000
    private const val REPEAT_2_OFFSET = 200000000
    private const val EMERGENCY_OFFSET = 300000000
    private const val EXPIRY_OFFSET = 500000000

    fun scheduleNotification(
        context: Context,
        id: Int,
        title: String,
        message: String,
        timeInMillis: Long,
        repeatType: RepeatType = RepeatType.NONE,
        priority: Priority = Priority.MEDIUM,
        category: Category = Category.CUSTOM,
        originalReminderId: Long? = null,
        includeFollowUps: Boolean = true
    ): Long {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        
        var targetTime = timeInMillis

        if (repeatType != RepeatType.NONE && targetTime <= now) {
            targetTime = getNextOccurrence(targetTime, repeatType)
        }

        // If targetTime is still in the past (for non-repeating or if getNextOccurrence returned past),
        // only schedule if it's recent (e.g., within last 15 minutes) to avoid "notification storms" on boot.
        if (targetTime < now) {
            if (now - targetTime > 15 * 60 * 1000) {
                Log.d(TAG, "Skipping old notification: ID=$id, scheduled was ${Date(targetTime)}")
                return targetTime
            }
            targetTime = now + 1000 
        }

        Log.d(TAG, "Scheduling notification: ID=$id, Title='$title', Time=$targetTime, Now=$now, includeFollowUps=$includeFollowUps")

        // Primary alarm
        val intent = createIntent(context, id, title, message, repeatType, category, originalReminderId, TYPE_PRIMARY)
        val pendingIntent = createPendingIntent(context, id, intent)
        setAlarm(alarmManager, targetTime, pendingIntent)

        // Medicine repeats (3 notifications total: T, T+5, T+10)
        if (includeFollowUps && category == Category.MEDICINE) {
            scheduleMedicineFollowUps(
                context = context,
                id = id,
                title = title,
                message = message,
                baseTime = targetTime,
                repeatType = repeatType,
                priority = priority,
                originalReminderId = originalReminderId
            )
        }
        
        return targetTime
    }

    fun scheduleMedicineFollowUps(
        context: Context,
        id: Int, // Primary ID (reminderId * 10 + index)
        title: String,
        message: String,
        baseTime: Long,
        repeatType: RepeatType,
        priority: Priority,
        originalReminderId: Long?
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        
        Log.d(TAG, "Scheduling medicine follow-ups for baseTime=$baseTime")

        // 5 mins later
        val t2 = baseTime + 5 * 60 * 1000
        if (t2 > now) {
            val id2 = id + REPEAT_1_OFFSET
            val intent2 = createIntent(context, id2, "$title (Follow-up)", message, repeatType, Category.MEDICINE, originalReminderId, TYPE_FOLLOW_UP_1)
            setAlarm(alarmManager, t2, createPendingIntent(context, id2, intent2))
        }

        // 10 mins later
        val t3 = baseTime + 10 * 60 * 1000
        if (t3 > now) {
            val id3 = id + REPEAT_2_OFFSET
            val intent3 = createIntent(context, id3, "$title (Urgent)", message, repeatType, Category.MEDICINE, originalReminderId, TYPE_FOLLOW_UP_2)
            setAlarm(alarmManager, t3, createPendingIntent(context, id3, intent3))
        }

        // Emergency Alert for High Priority (T+15)
        if (priority == Priority.HIGH) {
            val t4 = baseTime + 15 * 60 * 1000
            if (t4 > now) {
                val id4 = id + EMERGENCY_OFFSET
                val intent4 = createIntent(context, id4, title, message, repeatType, Category.MEDICINE, originalReminderId, TYPE_EMERGENCY)
                setAlarm(alarmManager, t4, createPendingIntent(context, id4, intent4))
            }
        }
    }

    private fun getNextOccurrence(startTime: Long, repeatType: RepeatType): Long {
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()
        calendar.timeInMillis = startTime
        
        // Ensure we actually move forward
        while (calendar.timeInMillis <= now) {
            val prevTime = calendar.timeInMillis
            when (repeatType) {
                RepeatType.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                RepeatType.ALTERNATE -> calendar.add(Calendar.DAY_OF_YEAR, 2)
                RepeatType.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                RepeatType.TWO_WEEKS -> calendar.add(Calendar.WEEK_OF_YEAR, 2)
                RepeatType.THREE_WEEKS -> calendar.add(Calendar.WEEK_OF_YEAR, 3)
                RepeatType.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                RepeatType.THREE_MONTHS -> calendar.add(Calendar.MONTH, 3)
                RepeatType.SIX_MONTHS -> calendar.add(Calendar.MONTH, 6)
                RepeatType.YEARLY -> calendar.add(Calendar.YEAR, 1)
                else -> {
                    // Default to DAILY for anything else (like CUSTOM if not handled) to avoid infinite loops
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            // If for some reason the calendar didn't move forward, break to avoid infinite loop
            if (calendar.timeInMillis <= prevTime) {
                calendar.timeInMillis = now + 24 * 60 * 60 * 1000 // Fallback to tomorrow
                break
            }
        }
        return calendar.timeInMillis
    }

    fun scheduleExpiryAlerts(context: Context, reminderId: Int, title: String, expiryDate: Long) {
        val oneDay = 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()

        for (i in 0..6) {
            val daysBefore = 30 - (i * 5)
            val alertTime = expiryDate - (daysBefore * oneDay)
            
            if (alertTime > now - 60000) {
                val daysRemaining = ((expiryDate - alertTime) / oneDay).toInt()
                val message = if (daysRemaining <= 0) "Your medicine '$title' expires today!"
                             else "Your medicine '$title' will expire in $daysRemaining days."

                val id = reminderId * 10 + EXPIRY_OFFSET + i
                val intent = createIntent(context, id, "Medicine Expiry Alert", message, RepeatType.NONE, Category.MEDICINE, reminderId.toLong(), TYPE_EXPIRY)
                setAlarm(
                    context.getSystemService(Context.ALARM_SERVICE) as AlarmManager,
                    alertTime,
                    createPendingIntent(context, id, intent)
                )
            }
        }
    }

    fun cancelAllReminderNotifications(context: Context, reminderId: Int) {
        Log.d(TAG, "Cancelling all notifications for Reminder ID=$reminderId")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancel up to 10 time slots and their potential repeats/alerts
        for (index in 0 until 10) {
            val id = reminderId * 10 + index
            cancel(context, alarmManager, id)
            cancel(context, alarmManager, id + REPEAT_1_OFFSET)
            cancel(context, alarmManager, id + REPEAT_2_OFFSET)
            cancel(context, alarmManager, id + EMERGENCY_OFFSET)
            
            // Cleanup for old offsets (1M, 2M, 3M, 5M)
            cancel(context, alarmManager, id + 1000000)
            cancel(context, alarmManager, id + 2000000)
            cancel(context, alarmManager, id + 3000000)
            cancel(context, alarmManager, id + 5000000)
        }

        // Cancel expiry alerts
        for (i in 0..6) {
            cancel(context, alarmManager, reminderId * 10 + EXPIRY_OFFSET + i)
            cancel(context, alarmManager, reminderId * 10 + 5000000 + i) // Old offset
        }
        
        // Backward compatibility cleanup for old ID schemes if any
        for (i in 0 until 10) {
            cancel(context, alarmManager, reminderId + (i * 1000))
        }
        cancel(context, alarmManager, reminderId + 1000000)
    }

    private fun cancel(context: Context, alarmManager: AlarmManager, id: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            Log.d(TAG, "Cancelled pending intent for ID=$id")
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun createIntent(
        context: Context, id: Int, title: String, message: String, 
        repeatType: RepeatType, category: Category, originalReminderId: Long?,
        notificationType: Int
    ): Intent {
        return Intent(context, NotificationReceiver::class.java).apply {
            putExtra("TITLE", title)
            putExtra("MESSAGE", message)
            putExtra("ID", id)
            putExtra("NOTIFICATION_TYPE", notificationType)
            putExtra("REPEAT_TYPE", repeatType.name)
            putExtra("CATEGORY", category.name)
            originalReminderId?.let { putExtra("REMINDER_ID", it) }
        }
    }

    private fun createPendingIntent(context: Context, id: Int, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun setAlarm(alarmManager: AlarmManager, timeInMillis: Long, pendingIntent: PendingIntent) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val dateString = sdf.format(java.util.Date(timeInMillis))
        
        try {
            Log.d(TAG, "Setting Alarm: TargetTime=$dateString ($timeInMillis)")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                } else {
                    Log.w(TAG, "Cannot schedule exact alarms, falling back to non-exact.")
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set alarm: ${e.message}")
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }
}
