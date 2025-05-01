package com.jurianoff.irlmate

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.snapshotFlow        // NEW
import com.jurianoff.irlmate.navigation.IRLMateApp
import com.jurianoff.irlmate.ui.settings.ThemeSettings
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { IRLMateApp() }

        // 🔄  obserwuj flagę keepScreenOn i stosuj ją natychmiast
        lifecycleScope.launch {
            snapshotFlow { ThemeSettings.keepScreenOn }     // NEW
                .collect { keep ->
                    applyKeepScreenOnFlag(keep)
                }
        }
    }

    private fun applyKeepScreenOnFlag(keepOn: Boolean) {   // <- przyjmuje parametr
        if (keepOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
