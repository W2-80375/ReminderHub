package com.own.remindme.domain.repository

import com.own.remindme.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

data class UserPreferences(
    val userName: String = "",
    val isNotificationsEnabled: Boolean = true,
    val emergencyContact: String = "",
    val categorySounds: Map<String, String> = emptyMap(),
    val appTheme: AppTheme = AppTheme.SYSTEM
)

interface UserPreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>
    suspend fun updateUserName(name: String)
    suspend fun updateNotificationPreference(enabled: Boolean)
    suspend fun updateEmergencyContact(contact: String)
    suspend fun updateCategorySound(category: String, path: String?)
    suspend fun updateAppTheme(theme: AppTheme)
}
