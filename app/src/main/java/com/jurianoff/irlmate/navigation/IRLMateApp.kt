package com.jurianoff.irlmate.navigation

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.jurianoff.irlmate.ui.main.MainScreen
import com.jurianoff.irlmate.ui.settings.*
import com.jurianoff.irlmate.ui.theme.IRLMateTheme
import java.util.*

@Composable
fun IRLMateApp(startDestination: String? = null) {
    val darkMode = ThemeSettings.darkMode
    val isDarkTheme = when (darkMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val systemUi = rememberSystemUiController()
    val bgColor = MaterialTheme.colorScheme.background
    val useDarkIcons = !isDarkTheme

    val languageCode = ThemeSettings.languageCode
    val locale = remember(languageCode) { Locale(languageCode) }
    val configuration = LocalConfiguration.current
    val updatedConfig = Configuration(configuration).apply {
        setLocale(locale)
    }
    val localizedContext = context.createConfigurationContext(updatedConfig)

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

            LaunchedEffect(Unit) {
                ThemeSettings.loadTheme(context)
            }

            // 🔄 Rozwijamy startDestination
            val initialStart = if (startDestination == "open_settings_after_start") "main"
            else (startDestination ?: "main")

            // 🚀 Po starcie z parametrem – idź od razu do settings
            LaunchedEffect(startDestination) {
                if (startDestination == "open_settings_after_start") {
                    navController.navigate("settings")
                }
            }

            NavHost(
                navController = navController,
                startDestination = initialStart
            ) {
                composable("main") {
                    MainScreen(onSettingsClick = { navController.navigate("settings") })
                }
                composable("settings") {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToKickLogin = {
                            navController.navigate("kick_enter_username")
                        }
                    )
                }
                composable("kick_enter_username") {
                    KickEnterUsernameScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
