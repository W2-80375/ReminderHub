package com.own.remindme.data.repository

import android.content.Context
import com.own.remindme.data.local.ReminderDao
import com.own.remindme.data.mapper.toDomain
import com.own.remindme.data.mapper.toEntity
import com.own.remindme.domain.model.Reminder
import com.own.remindme.domain.model.label
import com.own.remindme.domain.repository.ReminderRepository
import com.own.remindme.utils.notifications.NotificationScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao,
    @ApplicationContext private val context: Context
) : ReminderRepository {

    override fun getReminders(): Flow<List<Reminder>> {
        return dao.getReminders()
            .map {
                it.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun getReminderById(id: Long): Reminder? {
        return dao.getReminderById(id)?.toDomain()
    }

    override suspend fun insert(reminder: Reminder): Long {
        val id = dao.insert(reminder.toEntity())
        
        // Schedule notifications for all reminder times
        reminder.reminderTimes.forEachIndexed { index, time ->
            NotificationScheduler.scheduleNotification(
                context = context,
                id = id.toInt() + (index * 1000), // Unique ID for each time slot
                title = "Reminder: ${reminder.title}",
                message = reminder.description,
                timeInMillis = time,
                frequency = reminder.repeatType.label,
                priority = reminder.priority,
                category = reminder.category
            )
        }

        // Schedule notification for expiry date if it exists
        reminder.expiryDate?.let { expiry ->
            NotificationScheduler.scheduleNotification(
                context = context,
                id = id.toInt() + 1000000, // Use a different ID for expiry
                title = "Reminder Expired: ${reminder.title}",
                message = "The reminder for ${reminder.title} has reached its expiry date.",
                timeInMillis = expiry,
                frequency = "None"
            )
        }

        return id
    }

    override suspend fun update(reminder: Reminder) {
        dao.update(reminder.toEntity())
        
        // First cancel existing notifications for all potential slots (up to 10)
        for (i in 0 until 10) {
            NotificationScheduler.cancelNotification(context, reminder.id.toInt() + (i * 1000))
        }

        // Reschedule notifications for all current reminder times
        reminder.reminderTimes.forEachIndexed { index, time ->
            NotificationScheduler.scheduleNotification(
                context = context,
                id = reminder.id.toInt() + (index * 1000),
                title = "Reminder: ${reminder.title}",
                message = reminder.description,
                timeInMillis = time,
                frequency = reminder.repeatType.label,
                priority = reminder.priority,
                category = reminder.category
            )
        }

        // Reschedule notification for expiry date if it exists
        reminder.expiryDate?.let { expiry ->
            NotificationScheduler.scheduleNotification(
                context = context,
                id = reminder.id.toInt() + 1000000,
                title = "Reminder Expired: ${reminder.title}",
                message = "The reminder for ${reminder.title} has reached its expiry date.",
                timeInMillis = expiry,
                frequency = "None"
            )
        }
    }

    override suspend fun delete(reminder: Reminder) {
        dao.delete(reminder.toEntity())
        // Cancel all potential slots (up to 10)
        for (i in 0 until 10) {
            NotificationScheduler.cancelNotification(context, reminder.id.toInt() + (i * 1000))
        }
        NotificationScheduler.cancelNotification(context, reminder.id.toInt() + 1000000)
    }
}