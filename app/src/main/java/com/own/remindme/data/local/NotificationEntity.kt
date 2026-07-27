package com.own.remindme.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long,
    val frequency: String,
    val category: String = "CUSTOM",
    val isRead: Boolean = false
)
