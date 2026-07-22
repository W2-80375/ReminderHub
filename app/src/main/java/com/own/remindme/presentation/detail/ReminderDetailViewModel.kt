package com.own.remindme.presentation.detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.own.remindme.domain.model.Reminder
import com.own.remindme.domain.usecase.ReminderUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderDetailViewModel @Inject constructor(
    private val reminderUseCases: ReminderUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _reminder = mutableStateOf<Reminder?>(null)
    val reminder: State<Reminder?> = _reminder

    init {
        savedStateHandle.get<Long>("reminderId")?.let { reminderId ->
            viewModelScope.launch {
                _reminder.value = reminderUseCases.getReminder(reminderId)
            }
        }
    }
}
