package com.jurianoff.irlmate.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object TwitchSession {

    // ─── In-memory ───────────────────────────────────────────────────────────────
    var accessToken: String? = null
    var refreshToken: String? = null
    var userId: String? = null
    var username: String? = null
    var expiresInSeconds: Long = 3600      // <--- NOWE POLE
    var tokenReceivedAt: Long = 0          // <--- NOWE POLE
    var showChatAndStatus: Boolean = false
    var isLoaded: Boolean = false
        private set

    // ─── DataStore ───────────────────────────────────────────────────────────────
    private val Context.dataStore by preferencesDataStore(name = "twitch_session")

    private val KEY_ACCESS = stringPreferencesKey("access_token")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token")
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_USERNAME = stringPreferencesKey("username")
    private val KEY_EXPIRES = stringPreferencesKey("expires_in")
    private val KEY_TOKEN_RECEIVED_AT = longPreferencesKey("token_received_at")
    private val KEY_SHOW_CHAT = booleanPreferencesKey("show_chat_and_status")

    fun isLoggedIn(): Boolean {
        return !accessToken.isNullOrEmpty() && !userId.isNullOrEmpty() && !username.isNullOrEmpty()
    }

    suspend fun setShowChatAndStatus(context: Context, show: Boolean) {
        showChatAndStatus = show
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_CHAT] = show
        }
    }

    suspend fun saveSession(
        context: Context,
        accessToken: String,
        refreshToken: String,
        userId: String?,
        username: String?,
        expiresInSeconds: Long = 3600       // <--- przyjmujemy domyślnie godzinę
    ) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.userId = userId
        this.username = username
        this.expiresInSeconds = expiresInSeconds
        val receivedAt = System.currentTimeMillis()
        this.tokenReceivedAt = receivedAt

        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = accessToken
            prefs[KEY_REFRESH] = refreshToken
            userId?.let { prefs[KEY_USER_ID] = it }
            username?.let { prefs[KEY_USERNAME] = it }
            prefs[KEY_EXPIRES] = expiresInSeconds.toString()
            prefs[KEY_TOKEN_RECEIVED_AT] = receivedAt
            prefs[KEY_SHOW_CHAT] = showChatAndStatus
        }

        println("💾 [TwitchSession] Zapisano sesję: $username (expiresIn: $expiresInSeconds)")
    }

    suspend fun loadSession(context: Context) {
        context.dataStore.data.first().let { prefs ->
            accessToken = prefs[KEY_ACCESS]
            refreshToken = prefs[KEY_REFRESH]
            userId = prefs[KEY_USER_ID]
            username = prefs[KEY_USERNAME]
            expiresInSeconds = prefs[KEY_EXPIRES]?.toLongOrNull() ?: 3600
            tokenReceivedAt = prefs[KEY_TOKEN_RECEIVED_AT] ?: 0
            showChatAndStatus = prefs[KEY_SHOW_CHAT] ?: false
        }
        isLoaded = true
    }

    suspend fun clearSession(context: Context) {
        accessToken = null
        refreshToken = null
        userId = null
        username = null
        expiresInSeconds = 3600
        tokenReceivedAt = 0
        showChatAndStatus = false
        context.dataStore.edit { it.clear() }

        println("🧹 [TwitchSession] Wyczyszczono sesję")
    }

    // NOWA FUNKCJA – sprawdzanie ważności access_token
    fun isAccessTokenValid(): Boolean {
        if (accessToken.isNullOrEmpty() || expiresInSeconds == 0L || tokenReceivedAt == 0L) return false
        val expiryMillis = tokenReceivedAt + expiresInSeconds * 1000
        // 60 sekund zapasu bezpieczeństwa!
        return System.currentTimeMillis() < (expiryMillis - 60_000)
    }

    // NOWA FUNKCJA – automatyczny refresh access_token
    suspend fun ensureValidAccessToken(context: Context): Boolean {
        if (isAccessTokenValid()) return true
        val refreshToken = this.refreshToken ?: return false
        try {
            val client = OkHttpClient()
            // PODSTAW SWÓJ ADRES BACKENDU!
            val url = "https://ah2d6m1qy4.execute-api.eu-central-1.amazonaws.com/auth/twitch/refresh?refresh_token=$refreshToken"
            val request = Request.Builder().url(url).get().build()
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            if (!response.isSuccessful) return false
            val body = response.body?.string() ?: return false
            val json = JSONObject(body)
            val newAccessToken = json.optString("access_token", null)
            val newRefreshToken = json.optString("refresh_token", null)
            val expiresIn = json.optLong("expires_in", 3600)
            if (newAccessToken.isNullOrEmpty() || newRefreshToken.isNullOrEmpty()) return false

            saveSession(
                context = context,
                accessToken = newAccessToken,
                refreshToken = newRefreshToken,
                userId = userId,
                username = username,
                expiresInSeconds = expiresIn
            )
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
