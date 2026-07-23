package com.own.remindme.data.repository

import android.content.Context
import com.own.remindme.data.local.ReminderDao
import com.own.remindme.data.local.ReminderEntity
import com.own.remindme.data.mapper.toDomain
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReminderRepositoryImplTest {

    private lateinit var repository: ReminderRepositoryImpl
    private lateinit var dao: ReminderDao
    private lateinit var context: Context

    @Before
    fun setUp() {
        dao = mockk()
        context = mockk()
        repository = ReminderRepositoryImpl(dao, context)
    }

    @Test
    fun `getReminders returns domain models`() = runTest {
        val entities = listOf(
            ReminderEntity(
                id = 1,
                title = "Test",
                description = "Desc",
                category = "MEDICINE",
                reminderTimes = listOf(123456789),
                repeatType = "NONE",
                priority = "MEDIUM",
                completed = false
            )
        )
        
        every { dao.getReminders() } returns flowOf(entities)
        
        val result = repository.getReminders().first()
        
        assertEquals(1, result.size)
        assertEquals("Test", result[0].title)
        assertEquals(1L, result[0].id)
    }
}
