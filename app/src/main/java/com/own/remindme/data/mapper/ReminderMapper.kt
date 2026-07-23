package com.own.remindme.data.mapper

import com.own.remindme.data.local.ReminderEntity
import com.own.remindme.domain.model.*

fun ReminderEntity.toDomain() = Reminder(
    id = id,
    title = title,
    description = description,
    category = Category.valueOf(category),
    reminderTimes = reminderTimes,
    repeatType = RepeatType.valueOf(repeatType),
    priority = Priority.valueOf(priority),
    completed = completed,
    expiryDate = expiryDate,
    imageUris = imageUris,
    lastTakenTimestamp = lastTakenTimestamp
)

fun Reminder.toEntity() = ReminderEntity(
    id = id,
    title = title,
    description = description,
    category = category.name,
    reminderTimes = reminderTimes,
    repeatType = repeatType.name,
    priority = priority.name,
    completed = completed,
    expiryDate = expiryDate,
    imageUris = imageUris,
    lastTakenTimestamp = lastTakenTimestamp
)
