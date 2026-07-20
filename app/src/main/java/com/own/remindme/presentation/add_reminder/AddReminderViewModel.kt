package com.own.remindme.presentation.add_reminder

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.own.remindme.domain.model.Category
import com.own.remindme.domain.model.Priority
import com.own.remindme.domain.model.Reminder
import com.own.remindme.domain.model.RepeatType
import com.own.remindme.domain.usecase.ReminderUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val reminderUseCases: ReminderUseCases
) : ViewModel() {

    private val _reminderTitle = mutableStateOf("")
    val reminderTitle: State<String> = _reminderTitle

    private val _reminderDescription = mutableStateOf("")
    val reminderDescription: State<String> = _reminderDescription

    private val _reminderCategory = mutableStateOf(Category.MEDICINE)
    val reminderCategory: State<Category> = _reminderCategory

    private val _reminderTime = mutableStateOf(System.currentTimeMillis())
    val reminderTime: State<Long> = _reminderTime

    private val _reminderRepeatType = mutableStateOf(RepeatType.NONE)
    val reminderRepeatType: State<RepeatType> = _reminderRepeatType

    private val _reminderPriority = mutableStateOf(Priority.MEDIUM)
    val reminderPriority: State<Priority> = _reminderPriority

    private val _expiryDate = mutableStateOf<Long?>(null)
    val expiryDate: State<Long?> = _expiryDate

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: AddReminderEvent) {
        when (event) {
            is AddReminderEvent.EnteredTitle -> {
                _reminderTitle.value = event.value
            }
            is AddReminderEvent.EnteredDescription -> {
                _reminderDescription.value = event.value
            }
            is AddReminderEvent.ChangeCategory -> {
                _reminderCategory.value = event.category
            }
            is AddReminderEvent.ChangeTime -> {
                _reminderTime.value = event.time
            }
            is AddReminderEvent.ChangeRepeatType -> {
                _reminderRepeatType.value = event.repeatType
            }
            is AddReminderEvent.ChangePriority -> {
                _reminderPriority.value = event.priority
            }
            is AddReminderEvent.ChangeExpiryDate -> {
                _expiryDate.value = event.date
            }
            is AddReminderEvent.SaveReminder -> {
                viewModelScope.launch {
                    try {
                        reminderUseCases.addReminder(
                            Reminder(
                                title = reminderTitle.value,
                                description = reminderDescription.value,
                                reminderTime = reminderTime.value,
                                category = reminderCategory.value,
                                repeatType = reminderRepeatType.value,
                                priority = reminderPriority.value,
                                completed = false
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveReminder)
                    } catch (e: Exception) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save reminder"
                            )
                        )
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object SaveReminder : UiEvent()
    }
}