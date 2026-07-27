package com.own.remindme.presentation.add_reminder

import com.own.remindme.domain.model.*

sealed class AddReminderEvent {
    data class EnteredTitle(val value: String) : AddReminderEvent()
    data class EnteredDescription(val value: String) : AddReminderEvent()
    data class ChangeCategory(val category: Category) : AddReminderEvent()
    data class AddTime(val time: Long) : AddReminderEvent()
    data class RemoveTime(val index: Int) : AddReminderEvent()
    data class UpdateTime(val index: Int, val time: Long) : AddReminderEvent()
    data class ChangeRepeatType(val repeatType: RepeatType) : AddReminderEvent()
    data class ChangePriority(val priority: Priority) : AddReminderEvent()
    data class ChangeExpiryDate(val date: Long?) : AddReminderEvent()
    data class AddAttachment(val uri: String) : AddReminderEvent()
    data class AddAttachments(val uris: List<String>) : AddReminderEvent()
    data class RemoveAttachment(val uri: String) : AddReminderEvent()
    object SaveReminder : AddReminderEvent()
    object DeleteReminder : AddReminderEvent()
    object ToggleAiListening : AddReminderEvent()
}
