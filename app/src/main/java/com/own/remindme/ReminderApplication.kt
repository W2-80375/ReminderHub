package com.own.remindme

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.own.remindme.utils.workers.ReminderCleanupWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ReminderApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        schedulePeriodicWork()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Reminders Channel
            val remindersChannel = NotificationChannel(
                "reminders",
                "General Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for general reminders"
            }
            notificationManager.createNotificationChannel(remindersChannel)

            // Medicine Channel
            val medicineChannel = NotificationChannel(
                "medicine_reminders",
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medicine reminders"
            }
            notificationManager.createNotificationChannel(medicineChannel)
        }
    }

    private fun schedulePeriodicWork() {
        val cleanupRequest = PeriodicWorkRequestBuilder<ReminderCleanupWorker>(
            24, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ReminderCleanupWork",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }
}
