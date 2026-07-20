package com.own.remindme.data.repository

import com.own.remindme.data.local.ReminderDao
import com.own.remindme.data.mapper.toDomain
import com.own.remindme.data.mapper.toEntity
import com.own.remindme.domain.model.Reminder
import com.own.remindme.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(

    private val dao: ReminderDao

) : ReminderRepository {

    override fun getReminders(): Flow<List<Reminder>> {

        return dao.getReminders()

            .map {

                it.map { entity ->

                    entity.toDomain()

                }

            }

    }

    override suspend fun insert(reminder: Reminder) {

        dao.insert(reminder.toEntity())

    }

    override suspend fun update(reminder: Reminder) {

        dao.update(reminder.toEntity())

    }

    override suspend fun delete(reminder: Reminder) {

        dao.delete(reminder.toEntity())

    }

}