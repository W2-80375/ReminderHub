package com.own.remindme.domain.repository

import kotlinx.coroutines.flow.Flow

data class UserPreferences(
    val userName: String = "",
    val isNotificationsEnabled: Boolean = true,
    val emergencyContact: String = "",
    val medicineSoundPath: String? = null,
    val otherSoundPath: String? = null
)

interface UserPreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>
    suspend fun updateUserName(name: String)
    suspend fun updateNotificationPreference(enabled: Boolean)
    suspend fun updateEmergencyContact(contact: String)
    suspend fun updateMedicineSound(path: String?)
    suspend fun updateOtherSound(path: String?)
}
