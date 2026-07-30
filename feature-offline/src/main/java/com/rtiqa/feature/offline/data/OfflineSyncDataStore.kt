package com.rtiqa.feature.offline.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.offlineSyncDataStore: DataStore<Preferences> by preferencesDataStore(name = "offline_sync_preferences")

object OfflineSyncPreferences {
    val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")

    fun getLastSyncTimestamp(context: Context): Flow<Long> {
        return context.offlineSyncDataStore.data.map { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] ?: 0L
        }
    }

    suspend fun saveLastSyncTimestamp(context: Context, timestamp: Long) {
        context.offlineSyncDataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] = timestamp
        }
    }
}
