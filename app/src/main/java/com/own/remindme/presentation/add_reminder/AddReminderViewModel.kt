package com.own.remindme.presentation.add_reminder

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
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
    private val reminderUseCases: ReminderUseCases,
    savedStateHandle: SavedStateHandle
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

    private val _attachmentUris = mutableStateOf<List<String>>(emptyList())
    val attachmentUris: State<List<String>> = _attachmentUris

    private var _lastTakenTimestamp = mutableStateOf<Long?>(null)

    private var currentReminderId: Long? = null

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        savedStateHandle.get<Long>("reminderId")?.let { reminderId ->
            if (reminderId != -1L) {
                viewModelScope.launch {
                    reminderUseCases.getReminder(reminderId)?.also { reminder ->
                        currentReminderId = reminder.id
                        _reminderTitle.value = reminder.title
                        _reminderDescription.value = reminder.description
                        _reminderCategory.value = reminder.category
                        _reminderTime.value = reminder.reminderTime
                        _reminderRepeatType.value = reminder.repeatType
                        _reminderPriority.value = reminder.priority
                        _expiryDate.value = reminder.expiryDate
                        _attachmentUris.value = reminder.imageUris
                        _lastTakenTimestamp.value = reminder.lastTakenTimestamp
                    }
                }
            }
        }
    }

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
            is AddReminderEvent.AddAttachment -> {
                _attachmentUris.value = _attachmentUris.value + event.uri
            }
            is AddReminderEvent.RemoveAttachment -> {
                _attachmentUris.value = _attachmentUris.value.filter { it != event.uri }
            }
            is AddReminderEvent.SaveReminder -> {
                viewModelScope.launch {
                    try {
                        val reminder = Reminder(
                            id = currentReminderId ?: 0,
                            title = reminderTitle.value,
                            description = reminderDescription.value,
                            reminderTime = reminderTime.value,
                            category = reminderCategory.value,
                            repeatType = reminderRepeatType.value,
                            priority = if (reminderCategory.value == Category.MEDICINE) reminderPriority.value else Priority.MEDIUM,
                            completed = false,
                            expiryDate = expiryDate.value,
                            imageUris = attachmentUris.value,
                            lastTakenTimestamp = _lastTakenTimestamp.value
                        )
                        
                        if (currentReminderId != null) {
                            reminderUseCases.updateReminder(reminder)
                        } else {
                            reminderUseCases.addReminder(reminder)
                        }
                        
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
            is AddReminderEvent.DeleteReminder -> {
                viewModelScope.launch {
                    try {
                        currentReminderId?.let { id ->
                            val reminder = reminderUseCases.getReminder(id)
                            reminder?.let {
                                reminderUseCases.deleteReminder(it)
                                _eventFlow.emit(UiEvent.DeleteReminder)
                            }
                        }
                    } catch (e: Exception) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't delete reminder"
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
        object DeleteReminder : UiEvent()
    }
}
