package com.own.remindme.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.own.remindme.domain.model.AppTheme
import com.own.remindme.domain.repository.UserPreferences
import com.own.remindme.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun updateUserName(name: String) {
        viewModelScope.launch {
            repository.updateUserName(name)
        }
    }

    fun updateEmergencyContact(contact: String) {
        viewModelScope.launch {
            repository.updateEmergencyContact(contact)
        }
    }

    fun updateCategorySound(category: String, path: String?) {
        viewModelScope.launch {
            repository.updateCategorySound(category, path)
        }
    }

    fun updateAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            repository.updateAppTheme(theme)
        }
    }
}
