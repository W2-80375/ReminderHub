package com.own.remindme.presentation.add_reminder

import com.own.remindme.domain.model.Category
import com.own.remindme.domain.model.Priority
import com.own.remindme.domain.model.RepeatType

sealed class AddReminderEvent {
    data class EnteredTitle(val value: String) : AddReminderEvent()
    data class EnteredDescription(val value: String) : AddReminderEvent()
    data class ChangeCategory(val category: Category) : AddReminderEvent()
    data class ChangeTime(val time: Long) : AddReminderEvent()
    data class ChangeRepeatType(val repeatType: RepeatType) : AddReminderEvent()
    data class ChangePriority(val priority: Priority) : AddReminderEvent()
    data class ChangeExpiryDate(val date: Long?) : AddReminderEvent()
    object SaveReminder : AddReminderEvent()
}