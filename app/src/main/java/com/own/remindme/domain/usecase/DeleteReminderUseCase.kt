package com.own.remindme.domain.usecase

import com.own.remindme.domain.model.Reminder
import com.own.remindme.domain.repository.ReminderRepository
import javax.inject.Inject

class DeleteReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder) {
        repository.delete(reminder)
    }
}
