package com.jurianoff.irlmate.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.jurianoff.irlmate.ui.main.MainScreen
import com.jurianoff.irlmate.ui.settings.SettingsScreen
import com.jurianoff.irlmate.ui.settings.ThemeMode
import com.jurianoff.irlmate.ui.settings.ThemeSettings
import com.jurianoff.irlmate.ui.theme.IRLMateTheme

@Composable
fun IRLMateApp() {
    val darkMode = ThemeSettings.darkMode
    val isDarkTheme = when (darkMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    IRLMateTheme(useDarkTheme = isDarkTheme) {
        val navController = rememberNavController()
        val context = LocalContext.current
        val view = LocalView.current

        // System UI kolory (status bar + nawigacyjny)
        val systemUi = rememberSystemUiController()
        val bgColor = MaterialTheme.colorScheme.background
        val useDarkIcons = !isDarkTheme

        SideEffect {
            systemUi.setSystemBarsColor(
                color = bgColor,
                darkIcons = useDarkIcons
            )
            systemUi.setNavigationBarColor(
                color = bgColor,
                darkIcons = useDarkIcons
            )
        }

        // Wczytanie motywu z pamięci
        LaunchedEffect(Unit) {
            ThemeSettings.loadTheme(context)
        }

        // Nawigacja
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
