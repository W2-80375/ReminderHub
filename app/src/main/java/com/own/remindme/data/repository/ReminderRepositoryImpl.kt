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
        
        // Schedule notification for reminder time
        NotificationScheduler.scheduleNotification(
            context = context,
            id = id.toInt(),
            title = "Reminder: ${reminder.title}",
            message = reminder.description,
            timeInMillis = reminder.reminderTime,
            frequency = reminder.repeatType.label,
            priority = reminder.priority,
            category = reminder.category
        )

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
        
        // Reschedule notification for reminder time
        NotificationScheduler.scheduleNotification(
            context = context,
            id = reminder.id.toInt(),
            title = "Reminder: ${reminder.title}",
            message = reminder.description,
            timeInMillis = reminder.reminderTime,
            frequency = reminder.repeatType.label,
            priority = reminder.priority,
            category = reminder.category
        )

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
        NotificationScheduler.cancelNotification(context, reminder.id.toInt())
        NotificationScheduler.cancelNotification(context, reminder.id.toInt() + 1000000)
    }
}