package com.own.remindme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ReminderEntity::class, NotificationEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ReminderDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun notificationDao(): NotificationDao

}