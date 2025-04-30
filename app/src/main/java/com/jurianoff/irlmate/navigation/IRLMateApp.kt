package com.jurianoff.irlmate.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jurianoff.irlmate.ui.main.MainScreen
import com.jurianoff.irlmate.ui.settings.SettingsScreen
import com.jurianoff.irlmate.ui.settings.ThemeMode
import com.jurianoff.irlmate.ui.settings.ThemeSettings
import com.jurianoff.irlmate.ui.theme.IRLMateTheme

@Composable
fun IRLMateApp() {
    val darkMode = ThemeSettings.darkMode
    val isDarkTheme = when (darkMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    IRLMateTheme(useDarkTheme = isDarkTheme) {
        val navController = rememberNavController()
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            ThemeSettings.loadTheme(context)
        }

        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                MainScreen(onSettingsClick = { navController.navigate("settings") })
            }
            composable("settings") {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
