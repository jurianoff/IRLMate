package com.jurianoff.irlmate.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.jurianoff.irlmate.BuildConfig

@Composable
fun TwitchLoginScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // 1. Otwórz przeglądarkę z adresem logowania Twitch
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://ah2d6m1qy4.execute-api.eu-central-1.amazonaws.com/auth/twitch/start")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)


}
