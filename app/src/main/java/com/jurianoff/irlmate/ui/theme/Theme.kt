package com.jurianoff.irlmate.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF6D00),
    secondary = Color(0xFFFF9100),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary        = Color(0xFF6200EE),
    secondary      = Color(0xFF03DAC6),
    background     = Color(0xFFF5F5F5),
    surface        = Color(0xFFEFEFEF),
    onPrimary      = Color.White,
    onSecondary    = Color.Black,
    onBackground   = Color.Black,
    onSurface      = Color.Black,

    /* 👇 DODAJ TO: */
    surfaceVariant = Color(0xFFE0E0E0)   // lekki, jasnoszary

)

@Composable
fun IRLMateTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
