package com.example.streamchat.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF82AAFF),
    secondary = Color(0xFFC792EA),
    background = Color(0xFF2B2B2B),
    surface = Color(0xFF3C3F41),
    onPrimary = Color(0xFF1E1E1E),
    onSecondary = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFF5F5F5),
)


@Composable
fun StreamChatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
