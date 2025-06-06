package com.jurianoff.irlmate.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.jurianoff.irlmate.MainActivity
import com.jurianoff.irlmate.R
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

                val sharedPref = getSharedPreferences("kick_auth", MODE_PRIVATE)
                val username = sharedPref.getString("username", null)

                if (
                    accessToken.isNullOrEmpty() ||
                    refreshToken.isNullOrEmpty() ||
                    username.isNullOrEmpty()
                ) {
                    showToast(R.string.kick_auth_failed)
                    finishOnMain()
                    return@launch
                }

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://kick.com/api/v2/channels/$username")
                    .header("Authorization", "Bearer $accessToken")
                    .header("User-Agent", "Mozilla/5.0")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (!response.isSuccessful || body.isNullOrEmpty()) {
                    showToast(R.string.kick_auth_failed)
                    finishOnMain()
                    return@launch
                }

                val json = JSONObject(body)
                val userId = json.optString("id", null)
                val actualUsername = json.optString("slug", username)
                val channelId = userId
                val chatroomId = json.optJSONObject("chatroom")?.optString("id", null)

                if (userId.isNullOrEmpty() || channelId.isNullOrEmpty() || chatroomId.isNullOrEmpty()) {
                    showToast(R.string.kick_auth_failed)
                    finishOnMain()
                    return@launch
                }

                Log.i("KickOAuth", "✅ Zalogowano jako $actualUsername (ID: $userId, ChannelID: $channelId, ChatroomID: $chatroomId)")

                withContext(Dispatchers.Main) {
                    KickSession.saveSession(
                        context = this@KickAuthRedirectActivity,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        userId = userId,
                        username = actualUsername,
                        channelId = channelId,
                        chatroomId = chatroomId,
                        tokenType = tokenType,
                        expiresInSeconds = expiresIn
                    )

                    Toast.makeText(
                        this@KickAuthRedirectActivity,
                        getString(R.string.kick_login_success, actualUsername),
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this@KickAuthRedirectActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("navigateTo", "open_settings_after_start")
                    }
                    startActivity(intent)
                    finish()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                showToast(R.string.kick_auth_failed)
                finishOnMain()
            }
        }
    }

    private suspend fun finishOnMain() {
        withContext(Dispatchers.Main) {
            finish()
        }
    }

    private suspend fun showToast(resId: Int) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@KickAuthRedirectActivity, getString(resId), Toast.LENGTH_LONG).show()
        }
    }
}
