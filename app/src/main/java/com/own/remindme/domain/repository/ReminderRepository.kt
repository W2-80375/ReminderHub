package com.own.remindme.domain.repository

import com.own.remindme.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {

    fun getReminders(): Flow<List<Reminder>>

    suspend fun getReminderById(id: Long): Reminder?

    suspend fun insert(reminder: Reminder): Long

    suspend fun update(reminder: Reminder)

    suspend fun delete(reminder: Reminder)
}