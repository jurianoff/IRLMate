package com.jurianoff.irlmate.ui.settings

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object ThemeSettings {
    var darkMode by mutableStateOf(ThemeMode.SYSTEM)
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
