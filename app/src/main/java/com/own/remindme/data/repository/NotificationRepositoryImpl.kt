package com.own.remindme.data.repository

import com.own.remindme.data.local.NotificationDao
import com.own.remindme.data.local.NotificationEntity
import com.own.remindme.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {
    override fun getAllNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getAllNotifications()
    }

    override fun getUnreadCount(): Flow<Int> {
        return notificationDao.getUnreadCount()
    }

    override suspend fun markAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    override suspend fun deleteNotification(notification: NotificationEntity) {
        notificationDao.deleteNotification(notification)
    }

    override suspend fun deleteAllNotifications() {
        notificationDao.deleteAllNotifications()
    }
}
