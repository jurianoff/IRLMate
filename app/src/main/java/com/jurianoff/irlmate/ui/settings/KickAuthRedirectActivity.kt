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
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class KickAuthRedirectActivity : Activity() {

    companion object {
        private const val BACKEND_BASE_URL =
            "https://ah2d6m1qy4.execute-api.eu-central-1.amazonaws.com"
    }

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
                val channelIdFromUri = uri.getQueryParameter("channel_id")

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
                var channelId = channelIdFromUri
                var userId: String? = null
                var actualUsername = username

                fetchChannelObject(client, accessToken, username)?.let { channelObj ->
                    userId = channelObj.optString("broadcaster_user_id").takeIf { it.isNotBlank() }
                    actualUsername = channelObj.optString("slug", actualUsername)
                    channelId = channelId ?: channelObj.optString("id").takeIf { it.isNotBlank() }
                }

                if (channelId.isNullOrEmpty()) {
                    showToast(R.string.kick_auth_failed)
                    finishOnMain()
                    return@launch
                }
                if (userId.isNullOrEmpty()) {
                    userId = channelId
                }

                Log.i(
                    "KickOAuth",
                    "Logged in as $actualUsername (ID: $userId, ChannelID: $channelId)"
                )

                subscribeKickWebhook(accessToken, userId)

                withContext(Dispatchers.Main) {
                    KickSession.saveSession(
                        context = this@KickAuthRedirectActivity,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        userId = userId,
                        username = actualUsername,
                        channelId = channelId,
                        chatroomId = null,
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

    private fun fetchChatroomId(
        client: OkHttpClient,
        accessToken: String,
        username: String
    ): String? = null

    private fun fetchChannelObject(
        client: OkHttpClient,
        accessToken: String,
        username: String
    ): JSONObject? {
        return requestChannelObject(client, accessToken, username, arrayParam = true)
            ?: run {
                Log.w("KickOAuth", "channels?slug[]= returned empty, retrying slug=")
                requestChannelObject(client, accessToken, username, arrayParam = false)
            }
    }

    private fun requestChannelObject(
        client: OkHttpClient,
        accessToken: String,
        username: String,
        arrayParam: Boolean
    ): JSONObject? {
        val apiUrl = HttpUrl.Builder()
            .scheme("https")
            .host("api.kick.com")
            .addPathSegments("public/v1/channels")
            .addQueryParameter(if (arrayParam) "slug[]" else "slug", username)
            .build()
        val request = Request.Builder()
            .url(apiUrl)
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/json")
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (response.isSuccessful && !body.isNullOrEmpty()) {
                    JSONObject(body).optJSONArray("data")?.optJSONObject(0)
                } else {
                    Log.e(
                        "KickOAuth",
                        "Channel lookup failed (${if (arrayParam) "slug[]" else "slug"}): ${response.code} ${body ?: "empty body"}"
                    )
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("KickOAuth", "Channel lookup exception", e)
            null
        }
    }

    private suspend fun subscribeKickWebhook(accessToken: String, broadcasterId: String?) {
        if (broadcasterId.isNullOrEmpty()) return
        val client = OkHttpClient()
        val body = JSONObject(
            mapOf(
                "access_token" to accessToken,
                "broadcaster_id" to broadcasterId
            )
        ).toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$BACKEND_BASE_URL/auth/kick/subscribe")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.i("KickOAuth", "Subscribed to chat.message.sent for $broadcasterId")
                } else {
                    Log.e(
                        "KickOAuth",
                        "Subscription failed ${response.code} ${response.body?.string()}"
                    )
                }
            }
        } catch (err: Exception) {
            Log.e("KickOAuth", "Subscription exception", err)
        }
    }
}


