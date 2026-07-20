package com.own.remindme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(

    entities = [ReminderEntity::class],

    version = 1,

    exportSchema = false

)
abstract class ReminderDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

}