package com.own.remindme.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val reminderTimes: List<Long>,
    val repeatType: String,
    val priority: String,
    val completed: Boolean,
    val expiryDate: Long? = null,
    val imageUris: List<String> = emptyList(),
    val lastTakenTimestamp: Long? = null
)
