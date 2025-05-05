package com.jurianoff.irlmate.data.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.stringPreferencesKey



// Utworzenie DataStore
val Context.dataStore by preferencesDataStore(name = "settings")

object SettingsDataStore {
    private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
    private val KEEP_SCREEN_ON_KEY = booleanPreferencesKey("keep_screen_on")

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

    suspend fun saveKeepScreenOn(context: Context, keepOn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEEP_SCREEN_ON_KEY] = keepOn
        }
    }

    fun readKeepScreenOn(context: Context): Flow<Boolean> {
        return context.dataStore.data
            .map { prefs ->
                prefs[KEEP_SCREEN_ON_KEY] ?: true // true = domyślnie NIE wygaszaj ekranu
            }
    }
    private val LANGUAGE_CODE_KEY = stringPreferencesKey("language_code")

    suspend fun saveLanguageCode(context: Context, code: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE_KEY] = code
        }
    }

    fun readLanguageCode(context: Context): Flow<String> {
        return context.dataStore.data
            .map { preferences ->
                preferences[LANGUAGE_CODE_KEY] ?: "en" // Domyślnie angielski
            }
    }
    private val TWITCH_CHANNEL_KEY = stringPreferencesKey("twitch_channel")
    private val KICK_CHANNEL_KEY = stringPreferencesKey("kick_channel")

    suspend fun saveTwitchChannel(context: Context, name: String) {
        context.dataStore.edit { preferences ->
            preferences[TWITCH_CHANNEL_KEY] = name
        }
    }

    fun readTwitchChannel(context: Context) = context.dataStore.data.map { prefs ->
        prefs[TWITCH_CHANNEL_KEY] ?: "jurianoff"
    }

    suspend fun saveKickChannel(context: Context, name: String) {
        context.dataStore.edit { preferences ->
            preferences[KICK_CHANNEL_KEY] = name
        }
    }

    fun readKickChannel(context: Context) = context.dataStore.data.map { prefs ->
        prefs[KICK_CHANNEL_KEY] ?: "jurianoff"
    }
}
