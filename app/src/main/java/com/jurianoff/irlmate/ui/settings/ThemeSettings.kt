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

    fun loadTheme(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val savedMode = SettingsDataStore.readThemeMode(context).first()
                darkMode = ThemeMode.values()[savedMode]
            } catch (e: Exception) {
                darkMode = ThemeMode.SYSTEM
            }
        }
    }

    fun saveTheme(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            SettingsDataStore.saveThemeMode(context, darkMode.ordinal)
        }
    }
}
