package com.own.remindme.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.own.remindme.domain.repository.UserPreferences
import com.own.remindme.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val EMERGENCY_CONTACT = stringPreferencesKey("emergency_contact")
        val MEDICINE_SOUND = stringPreferencesKey("medicine_sound")
        val OTHER_SOUND = stringPreferencesKey("other_sound")
    }

    override val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                userName = preferences[PreferencesKeys.USER_NAME] ?: "",
                isNotificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                emergencyContact = preferences[PreferencesKeys.EMERGENCY_CONTACT] ?: "",
                medicineSoundPath = preferences[PreferencesKeys.MEDICINE_SOUND],
                otherSoundPath = preferences[PreferencesKeys.OTHER_SOUND]
            )
        }

    override suspend fun updateUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    override suspend fun updateNotificationPreference(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun updateEmergencyContact(contact: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMERGENCY_CONTACT] = contact
        }
    }

    override suspend fun updateMedicineSound(path: String?) {
        dataStore.edit { preferences ->
            if (path == null) preferences.remove(PreferencesKeys.MEDICINE_SOUND)
            else preferences[PreferencesKeys.MEDICINE_SOUND] = path
        }
    }

    override suspend fun updateOtherSound(path: String?) {
        dataStore.edit { preferences ->
            if (path == null) preferences.remove(PreferencesKeys.OTHER_SOUND)
            else preferences[PreferencesKeys.OTHER_SOUND] = path
        }
    }
}
