package com.own.remindme.domain.usecase

import com.own.remindme.domain.model.Reminder
import com.own.remindme.domain.repository.ReminderRepository
import javax.inject.Inject

class AddReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder) {
        if (reminder.title.isBlank()) {
            throw Exception("The title of the reminder can't be empty.")
        }
        repository.insert(reminder)
    }
}