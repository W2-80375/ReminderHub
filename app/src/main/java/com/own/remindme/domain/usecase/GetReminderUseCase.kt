package com.own.remindme.domain.usecase

import com.own.remindme.domain.model.Reminder
import com.own.remindme.domain.repository.ReminderRepository
import javax.inject.Inject

class GetReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(id: Long): Reminder? {
        return repository.getReminderById(id)
    }
}
