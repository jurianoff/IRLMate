package com.jurianoff.irlmate

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.snapshotFlow
import com.jurianoff.irlmate.navigation.IRLMateApp
import com.jurianoff.irlmate.ui.settings.ThemeSettings
import kotlinx.coroutines.launch
import com.jurianoff.irlmate.ui.settings.KickSession


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Załaduj sesję
        lifecycleScope.launch {
            KickSession.loadSession(this@MainActivity)
        }

        // 2. Sprawdź, czy przejść od razu do ustawień
        val startInSettings = intent?.getBooleanExtra("navigateToSettings", false) == true

        // 3. Uruchom UI
        setContent {
            IRLMateApp(startInSettings = startInSettings)
        }

        // 4. Obsługa wygaszania
        lifecycleScope.launch {
            snapshotFlow { ThemeSettings.keepScreenOn }
                .collect { keep -> applyKeepScreenOnFlag(keep) }
        }
    }


    private fun applyKeepScreenOnFlag(keepOn: Boolean) {
        if (keepOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
