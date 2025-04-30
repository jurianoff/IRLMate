package com.jurianoff.irlmate.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Utworzenie DataStore
val Context.dataStore by preferencesDataStore(name = "settings")

object SettingsDataStore {
    private val THEME_MODE_KEY = intPreferencesKey("theme_mode")

    suspend fun saveThemeMode(context: Context, mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    fun readThemeMode(context: Context): Flow<Int> {
        return context.dataStore.data
            .map { preferences ->
                preferences[THEME_MODE_KEY] ?: 2 // 2 = SYSTEM jako domyślne
            }
    }
}
