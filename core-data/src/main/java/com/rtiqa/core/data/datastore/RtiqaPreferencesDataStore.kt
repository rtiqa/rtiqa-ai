package com.rtiqa.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import com.rtiqa.core.domain.repository.SettingsPreferences
import com.rtiqa.core.domain.repository.SettingsPreferencesContract

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rtiqa_user_preferences")

data class UserPreferences(
    val isDarkTheme: Boolean,
    val isOfflineModeEnabled: Boolean,
    val activeUserId: String?,
    val lastSyncTimestamp: Long,
    val activeSchoolId: String? = null,
    val languageCode: String = "ar"
)

/**
 * Production DataStore repository for managing typed user preferences.
 */
open class RtiqaPreferencesDataStore(
    private val context: Context
) : SettingsPreferencesContract {
    private val dataStore by lazy { context.dataStore }

    open val userPreferencesFlow: Flow<UserPreferences>
        get() = dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                UserPreferences(
                    isDarkTheme = preferences[KEY_DARK_THEME] ?: false,
                    isOfflineModeEnabled = preferences[KEY_OFFLINE_MODE] ?: false,
                    activeUserId = preferences[KEY_ACTIVE_USER_ID],
                    lastSyncTimestamp = preferences[KEY_LAST_SYNC_TIMESTAMP] ?: 0L,
                    activeSchoolId = preferences[KEY_ACTIVE_SCHOOL_ID]?.takeIf { it.isNotBlank() },
                    languageCode = sanitizeLanguage(preferences[KEY_LANGUAGE_CODE])
                )
            }

    override val settingsFlow: Flow<SettingsPreferences>
        get() = userPreferencesFlow.map {
            SettingsPreferences(it.languageCode, it.isDarkTheme, it.isOfflineModeEnabled)
        }

    override suspend fun setLanguageCode(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE_CODE] = sanitizeLanguage(languageCode)
        }
    }

    open suspend fun setActiveSchoolId(schoolId: String?) {
        dataStore.edit { preferences ->
            if (!schoolId.isNullOrBlank()) {
                preferences[KEY_ACTIVE_SCHOOL_ID] = schoolId
            } else {
                preferences.remove(KEY_ACTIVE_SCHOOL_ID)
            }
        }
    }

    override suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DARK_THEME] = enabled
        }
    }

    override suspend fun setOfflineMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_OFFLINE_MODE] = enabled
        }
    }

    open suspend fun setActiveUserId(userId: String?) {
        dataStore.edit { preferences ->
            if (userId != null) {
                preferences[KEY_ACTIVE_USER_ID] = userId
            } else {
                preferences.remove(KEY_ACTIVE_USER_ID)
            }
        }
    }

    open suspend fun updateLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    companion object {
        private val KEY_DARK_THEME = booleanPreferencesKey("key_dark_theme")
        private val KEY_OFFLINE_MODE = booleanPreferencesKey("key_offline_mode")
        private val KEY_ACTIVE_USER_ID = stringPreferencesKey("key_active_user_id")
        private val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey("key_last_sync_timestamp")
        private val KEY_ACTIVE_SCHOOL_ID = stringPreferencesKey("key_active_school_id")
        private val KEY_LANGUAGE_CODE = stringPreferencesKey("key_language_code")

        private fun sanitizeLanguage(languageCode: String?): String =
            if (languageCode == "en") "en" else "ar"
    }
}
