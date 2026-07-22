package com.own.remindme.domain.usecase

data class ReminderUseCases(
    val getAllReminders: GetAllRemindersUseCase,
    val getReminder: GetReminderUseCase,
    val addReminder: AddReminderUseCase,
    val updateReminder: UpdateReminderUseCase,
    val deleteReminder: DeleteReminderUseCase
)
