package com.jurianoff.irlmate.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.jurianoff.irlmate.ui.settings.ThemeMode
import com.jurianoff.irlmate.ui.settings.ThemeSettings

// Pomarańcz IRLMate
private val Orange         = Color(0xFFFF6D00)
private val OrangeSecondary = Color(0xFFFF9100)

// Własne palety
private val DarkColorScheme = darkColorScheme(
    primary      = Orange,
    secondary    = OrangeSecondary,
    background   = Color(0xFF121212),
    surface      = Color(0xFF1E1E1E),
    onPrimary    = Color.Black,
    onSecondary  = Color.Black,
    onBackground = Color.White,
    onSurface    = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary        = Orange,
    secondary      = OrangeSecondary,
    background     = Color(0xFFF5F5F5),
    surface        = Color(0xFFEFEFEF),
    onPrimary      = Color.White,
    onSecondary    = Color.Black,
    onBackground   = Color.Black,
    onSurface      = Color.Black,
    surfaceVariant = Color(0xFFE0E0E0)
)

@Composable
fun IRLMateTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    /*  ✅  Dynamic color TYLKO gdy użytkownik ustawił tryb SYSTEM  */
    val dynamicColorEnabled =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ThemeSettings.darkMode == ThemeMode.SYSTEM

    val colorScheme = when {
        dynamicColorEnabled -> {
            if (useDarkTheme) dynamicDarkColorScheme(context)
            else               dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColorScheme
        else         -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
