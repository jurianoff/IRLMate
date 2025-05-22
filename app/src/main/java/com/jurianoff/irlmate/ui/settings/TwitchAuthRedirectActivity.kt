package com.jurianoff.irlmate.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.jurianoff.irlmate.MainActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class TwitchAuthRedirectActivity : Activity() {

    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data: Uri? = intent?.data
        if (data != null && data.toString().startsWith("irlmate://auth/twitch/callback")) {
            val accessToken = data.getQueryParameter("access_token")
            val refreshToken = data.getQueryParameter("refresh_token")
            val expiresIn = data.getQueryParameter("expires_in")?.toLongOrNull() ?: 3600L // domyślnie 1h
            val userId = data.getQueryParameter("user_id")
            val username = data.getQueryParameter("username")

            if (!accessToken.isNullOrBlank()
                && !refreshToken.isNullOrBlank()
                && !userId.isNullOrBlank()
                && !username.isNullOrBlank()
            ) {
                scope.launch {
                    TwitchSession.showChatAndStatus = true
                    TwitchSession.saveSession(
                        context = this@TwitchAuthRedirectActivity,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        userId = userId,
                        username = username,
                        expiresInSeconds = expiresIn  // <-- przekazujemy expiresIn
                    )

                    Toast.makeText(
                        this@TwitchAuthRedirectActivity,
                        "Zalogowano do Twitch 🎉",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(
                        this@TwitchAuthRedirectActivity,
                        MainActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("navigateTo", "open_settings_after_start")
                    }

                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, "Błąd logowania do Twitch", Toast.LENGTH_LONG).show()
                finish()
            }
        } else {
            finish()
        }
    }
}
