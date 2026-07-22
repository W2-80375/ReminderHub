package com.own.remindme.utils.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.own.remindme.data.local.ReminderDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val reminderDao: ReminderDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            reminderDao.deleteCompletedReminders()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
