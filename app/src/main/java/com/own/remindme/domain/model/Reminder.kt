package com.own.remindme.domain.model

data class Reminder(
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: Category,
    val reminderTimes: List<Long>,
    val repeatType: RepeatType,
    val priority: Priority,
    val completed: Boolean,
    val expiryDate: Long? = null,
    val imageUris: List<String> = emptyList(),
    val lastTakenTimestamp: Long? = null
)
