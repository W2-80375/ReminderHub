package com.own.remindme.utils.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.model.Priority

object NotificationScheduler {
    fun scheduleNotification(
        context: Context,
        id: Int,
        title: String,
        message: String,
        timeInMillis: Long,
        frequency: String = "None",
        priority: Priority = Priority.MEDIUM,
        category: Category = Category.CUSTOM
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("TITLE", title)
            putExtra("MESSAGE", message)
            putExtra("ID", id)
            putExtra("FREQUENCY", frequency)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (timeInMillis > System.currentTimeMillis()) {
            setAlarm(alarmManager, timeInMillis, pendingIntent)

            // If it's a high priority medicine reminder, schedule 2 more notifications
            if (category == Category.MEDICINE && priority == Priority.HIGH) {
                // Schedule 2nd notification after 10 minutes
                val secondTime = timeInMillis + 10 * 60 * 1000
                val secondIntent = PendingIntent.getBroadcast(
                    context,
                    id + 2000000,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                setAlarm(alarmManager, secondTime, secondIntent)

                // Schedule 3rd notification after 20 minutes
                val thirdTime = timeInMillis + 20 * 60 * 1000
                val thirdIntent = PendingIntent.getBroadcast(
                    context,
                    id + 3000000,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                setAlarm(alarmManager, thirdTime, thirdIntent)
            }
        }
    }

    private fun setAlarm(alarmManager: AlarmManager, timeInMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelNotification(context: Context, id: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)

        // Also cancel the repeated ones if they exist
        val secondIntent = PendingIntent.getBroadcast(
            context,
            id + 2000000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(secondIntent)

        val thirdIntent = PendingIntent.getBroadcast(
            context,
            id + 3000000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(thirdIntent)
    }
}
