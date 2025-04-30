package com.jurianoff.irlmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jurianoff.irlmate.navigation.IRLMateApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = 0xFFF5F5F5.toInt(),  // jasne tło w trybie jasnym
                darkScrim = 0xFF121212.toInt()    // ciemne tło w trybie ciemnym
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = 0xFFF5F5F5.toInt(),
                darkScrim = 0xFF121212.toInt()
            )
        )

        super.onCreate(savedInstanceState)

        setContent {
            IRLMateApp()
        }
    }
}
