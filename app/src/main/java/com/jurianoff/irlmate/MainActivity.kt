package com.jurianoff.irlmate

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.jurianoff.irlmate.navigation.IRLMateApp
import com.jurianoff.irlmate.ui.settings.KickSession
import com.jurianoff.irlmate.ui.settings.TwitchSession
import com.jurianoff.irlmate.ui.settings.ThemeSettings
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var navigationTarget by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Załaduj sesje
        lifecycleScope.launch {
            KickSession.loadSession(this@MainActivity)
            TwitchSession.loadSession(this@MainActivity)
        }

        // 2. Sprawdź, czy użytkownik wraca z logowania
        val initialTarget = intent?.getStringExtra("navigateTo")
        navigationTarget = initialTarget

        // 3. Uruchom UI
        setContent {
            IRLMateApp(startDestination = navigationTarget)
        }

        // 4. Obsługa wygaszania ekranu
        lifecycleScope.launch {
            snapshotFlow { ThemeSettings.keepScreenOn }
                .collect { keep -> applyKeepScreenOnFlag(keep) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val newTarget = intent.getStringExtra("navigateTo")
        if (!newTarget.isNullOrEmpty()) {
            navigationTarget = newTarget
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
