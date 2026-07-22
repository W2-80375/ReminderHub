package com.own.remindme.utils.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.own.remindme.MainActivity

class ReminderService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra("TITLE") ?: "Active Reminder"
        val message = intent?.getStringExtra("MESSAGE") ?: "Don't forget!"

        startForeground(NOTIFICATION_ID, createNotification(title, message))
        
        return START_NOT_STICKY
    }

    private fun createNotification(title: String, message: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ReminderService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "reminders")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Done", stopPendingIntent)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 101

        fun start(context: Context, title: String, message: String) {
            val intent = Intent(context, ReminderService::class.java).apply {
                putExtra("TITLE", title)
                putExtra("MESSAGE", message)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ReminderService::class.java)
            context.stopService(intent)
        }
    }
}
