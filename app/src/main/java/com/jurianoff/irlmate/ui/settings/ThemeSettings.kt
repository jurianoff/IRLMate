package com.jurianoff.irlmate.ui.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jurianoff.irlmate.data.settings.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

object ThemeSettings {
    var darkMode by mutableStateOf(ThemeMode.SYSTEM)
    var keepScreenOn by mutableStateOf(true) // domyślnie: NIE wygaszaj

    fun loadTheme(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val savedMode = SettingsDataStore.readThemeMode(context).first()
                val savedKeepScreenOn = SettingsDataStore.readKeepScreenOn(context).first()
                darkMode = ThemeMode.values()[savedMode]
                keepScreenOn = savedKeepScreenOn
            } catch (e: Exception) {
                darkMode = ThemeMode.SYSTEM
                keepScreenOn = true
            }
        }
    }

    fun saveTheme(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            SettingsDataStore.saveThemeMode(context, darkMode.ordinal)
            SettingsDataStore.saveKeepScreenOn(context, keepScreenOn)
        }
    }
    var languageCode by mutableStateOf("en")

    fun loadLanguage(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val savedCode = SettingsDataStore.readLanguageCode(context).first()
                languageCode = savedCode
            } catch (e: Exception) {
                languageCode = "en"
            }
        }
    }

    fun saveLanguage(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            SettingsDataStore.saveLanguageCode(context, languageCode)
        }
    }

}
