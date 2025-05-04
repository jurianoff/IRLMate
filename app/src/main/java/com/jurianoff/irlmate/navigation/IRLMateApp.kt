package com.jurianoff.irlmate.navigation

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.jurianoff.irlmate.ui.main.MainScreen
import com.jurianoff.irlmate.ui.settings.SettingsScreen
import com.jurianoff.irlmate.ui.settings.ThemeMode
import com.jurianoff.irlmate.ui.settings.ThemeSettings
import com.jurianoff.irlmate.ui.theme.IRLMateTheme
import java.util.*

@Composable
fun IRLMateApp() {
    val context = LocalContext.current
    val isInitialized = ThemeSettings.isInitialized

    // Początkowe ładowanie ustawień
    LaunchedEffect(Unit) {
        ThemeSettings.loadTheme(context)
    }

    if (!isInitialized) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val darkMode = ThemeSettings.darkMode
    val isDarkTheme = when (darkMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val languageCode = ThemeSettings.languageCode
    val locale = remember(languageCode) { Locale(languageCode) }

    val configuration = LocalConfiguration.current
    val updatedConfig = Configuration(configuration).apply {
        setLocale(locale)
    }
    val localizedContext = context.createConfigurationContext(updatedConfig)

    val view = LocalView.current
    val systemUi = rememberSystemUiController()
    val bgColor = MaterialTheme.colorScheme.background
    val useDarkIcons = !isDarkTheme

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        IRLMateTheme(useDarkTheme = isDarkTheme) {
            SideEffect {
                systemUi.setSystemBarsColor(color = bgColor, darkIcons = useDarkIcons)
                systemUi.setNavigationBarColor(color = bgColor, darkIcons = useDarkIcons)
            }

            val navController = rememberNavController()

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
}
