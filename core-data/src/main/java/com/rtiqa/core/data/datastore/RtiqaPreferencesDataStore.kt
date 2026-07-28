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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rtiqa_user_preferences")

data class UserPreferences(
    val isDarkTheme: Boolean,
    val isOfflineModeEnabled: Boolean,
    val activeUserId: String?,
    val lastSyncTimestamp: Long
)

/**
 * Production DataStore repository for managing typed user preferences.
 */
open class RtiqaPreferencesDataStore(
    private val context: Context
) {
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
                    lastSyncTimestamp = preferences[KEY_LAST_SYNC_TIMESTAMP] ?: 0L
                )
            }

    open suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DARK_THEME] = enabled
        }
    }

    open suspend fun setOfflineMode(enabled: Boolean) {
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
    }
}
