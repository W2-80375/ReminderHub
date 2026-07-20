package com.own.remindme.data.mapper

import com.own.remindme.data.local.ReminderEntity
import com.own.remindme.domain.model.*

fun ReminderEntity.toDomain() = Reminder(

    id = id,

    title = title,

    description = description,

    category = Category.valueOf(category),

    reminderTime = reminderTime,

    repeatType = RepeatType.valueOf(repeatType),

    priority = Priority.valueOf(priority),

    completed = completed
)

fun Reminder.toEntity() = ReminderEntity(

    id = id,

    title = title,

    description = description,

    category = category.name,

    reminderTime = reminderTime,

    repeatType = repeatType.name,

    priority = priority.name,

    completed = completed
)