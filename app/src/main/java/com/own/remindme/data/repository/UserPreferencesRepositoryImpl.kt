package com.own.remindme.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.own.remindme.domain.model.AppTheme
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

    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, String>>() {}.type

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val EMERGENCY_CONTACT = stringPreferencesKey("emergency_contact")
        val CATEGORY_SOUNDS = stringPreferencesKey("category_sounds")
        val APP_THEME = stringPreferencesKey("app_theme")
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
            val themeName = preferences[PreferencesKeys.APP_THEME] ?: AppTheme.SYSTEM.name
            val theme = try {
                AppTheme.valueOf(themeName)
            } catch (e: Exception) {
                AppTheme.SYSTEM
            }

            val soundsJson = preferences[PreferencesKeys.CATEGORY_SOUNDS] ?: "{}"
            val soundsMap: Map<String, String> = try {
                gson.fromJson(soundsJson, mapType)
            } catch (e: Exception) {
                emptyMap()
            }

            UserPreferences(
                userName = preferences[PreferencesKeys.USER_NAME] ?: "",
                isNotificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                emergencyContact = preferences[PreferencesKeys.EMERGENCY_CONTACT] ?: "",
                categorySounds = soundsMap,
                appTheme = theme
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

    override suspend fun updateCategorySound(category: String, path: String?) {
        dataStore.edit { preferences ->
            val currentJson = preferences[PreferencesKeys.CATEGORY_SOUNDS] ?: "{}"
            val currentMap: MutableMap<String, String> = try {
                gson.fromJson(currentJson, mapType)
            } catch (e: Exception) {
                mutableMapOf()
            }

            if (path == null) {
                currentMap.remove(category)
            } else {
                currentMap[category] = path
            }

            preferences[PreferencesKeys.CATEGORY_SOUNDS] = gson.toJson(currentMap)
        }
    }

    override suspend fun updateAppTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = theme.name
        }
    }
}
