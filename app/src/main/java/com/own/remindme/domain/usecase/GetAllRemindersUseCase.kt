package com.own.remindme.domain.usecase

import com.own.remindme.domain.repository.ReminderRepository
import javax.inject.Inject

class GetAllRemindersUseCase @Inject constructor(

    private val repository: ReminderRepository

) {

    operator fun invoke() = repository.getReminders()

}