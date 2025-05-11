package com.jurianoff.irlmate.ui.theme

import androidx.appcompat.app.AppCompatDelegate
import com.jurianoff.irlmate.ui.settings.ThemeMode

object ThemeController {
    fun applyUserTheme(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
