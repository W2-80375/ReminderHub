package com.own.remindme.domain.repository

import com.own.remindme.data.local.NotificationEntity
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllNotifications(): Flow<List<NotificationEntity>>
    fun getUnreadCount(): Flow<Int>
    suspend fun markAsRead(id: Long)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(notification: NotificationEntity)
    suspend fun deleteAllNotifications()
}
