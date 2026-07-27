package com.own.remindme.data.repository

import android.content.Context
import com.own.remindme.data.local.ReminderDao
import com.own.remindme.data.mapper.toDomain
import com.own.remindme.data.mapper.toEntity
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.model.Reminder
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
        scheduleNotificationsForReminder(reminder.copy(id = id))
        return id
    }

    override suspend fun update(reminder: Reminder) {
        dao.update(reminder.toEntity())
        NotificationScheduler.cancelAllReminderNotifications(context, reminder.id.toInt())
        scheduleNotificationsForReminder(reminder)
    }

    private fun scheduleNotificationsForReminder(reminder: Reminder) {
        reminder.reminderTimes.forEachIndexed { index, time ->
            // Use (reminderId * 10 + index) for unique IDs per time slot
            val notificationId = reminder.id.toInt() * 10 + index
            
            NotificationScheduler.scheduleNotification(
                context = context,
                id = notificationId,
                title = "Reminder: ${reminder.title}",
                message = reminder.description,
                timeInMillis = time,
                repeatType = reminder.repeatType,
                priority = reminder.priority,
                category = reminder.category,
                originalReminderId = reminder.id
            )
        }

        // Handle Expiry
        reminder.expiryDate?.let { expiry ->
            if (reminder.category == Category.MEDICINE) {
                NotificationScheduler.scheduleExpiryAlerts(
                    context = context,
                    reminderId = reminder.id.toInt(),
                    title = reminder.title,
                    expiryDate = expiry
                )
            } else {
                // Use a dedicated ID range for non-medicine expiry: reminderId * 10 + 9
                val expiryNotificationId = reminder.id.toInt() * 10 + 9
                NotificationScheduler.scheduleNotification(
                    context = context,
                    id = expiryNotificationId,
                    title = "Expiry Reminder: ${reminder.title}",
                    message = "The reminder for ${reminder.title} has reached its expiry date.",
                    timeInMillis = expiry,
                    originalReminderId = reminder.id
                )
            }
        }
    }

    override suspend fun delete(reminder: Reminder) {
        dao.delete(reminder.toEntity())
        NotificationScheduler.cancelAllReminderNotifications(context, reminder.id.toInt())
    }
}