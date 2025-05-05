package com.jurianoff.irlmate.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.jurianoff.irlmate.MainActivity
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class KickAuthRedirectActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        if (uri != null && uri.toString().startsWith("irlmate://auth/kick/callback")) {
            handleRedirectUri(uri)
        } else {
            finish()
        }
    }

    private fun handleRedirectUri(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = uri.getQueryParameter("access_token")
                val refreshToken = uri.getQueryParameter("refresh_token")
                val expiresIn = uri.getQueryParameter("expires_in")?.toLongOrNull() ?: 7200
                val tokenType = uri.getQueryParameter("token_type") ?: "Bearer"

                // Username został wcześniej podany przez użytkownika (zachowany tymczasowo)
                val sharedPref = getSharedPreferences("kick_auth", MODE_PRIVATE)
                val username = sharedPref.getString("username", null)

                if (
                    accessToken.isNullOrEmpty() ||
                    refreshToken.isNullOrEmpty() ||
                    username.isNullOrEmpty()
                ) {
                    println("❌ [KickOAuth] Brakuje wymaganych danych")
                    finishOnMain()
                    return@launch
                }

                // Pobierz dane użytkownika przez Kick API v2
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://kick.com/api/v2/channels/$username")
                    .header("User-Agent", "Mozilla/5.0")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (!response.isSuccessful || body.isNullOrEmpty()) {
                    println("❌ [KickOAuth] Błąd pobierania danych użytkownika: ${response.code}")
                    finishOnMain()
                    return@launch
                }

                val json = JSONObject(body)
                val userId = json.optString("id", null)
                val actualUsername = json.optString("slug", username)

                if (userId.isNullOrEmpty()) {
                    println("❌ [KickOAuth] Brak user_id w odpowiedzi")
                    finishOnMain()
                    return@launch
                }

                println("✅ [KickOAuth] Zalogowano jako $actualUsername (ID: $userId)")

                withContext(Dispatchers.Main) {
                    KickSession.saveSession(
                        context = this@KickAuthRedirectActivity,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        userId = userId,
                        username = actualUsername,
                        tokenType = tokenType,
                        expiresInSeconds = expiresIn
                    )

                    Toast.makeText(
                        this@KickAuthRedirectActivity,
                        "Zalogowano do Kick jako $actualUsername",
                        Toast.LENGTH_LONG
                    ).show()

                    // Wróć do SettingsScreen przez MainActivity z navController.navigate("settings")
                    val intent = Intent(this@KickAuthRedirectActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("navigateToSettings", true)
                    }

                    startActivity(intent)
                    finish()
                }


            } catch (e: Exception) {
                e.printStackTrace()
                finishOnMain()
            }
        }
    }

    private suspend fun finishOnMain() {
        withContext(Dispatchers.Main) {
            finish()
        }
    }
}
