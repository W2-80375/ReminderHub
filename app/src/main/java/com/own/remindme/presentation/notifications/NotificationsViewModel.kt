package com.own.remindme.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.own.remindme.data.local.NotificationEntity
import com.own.remindme.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    init {
        notificationRepository.getAllNotifications()
            .onEach { _notifications.value = it }
            .launchIn(viewModelScope)
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun deleteNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notification)
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            notificationRepository.deleteAllNotifications()
        }
    }
}
