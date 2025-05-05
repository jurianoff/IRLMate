package com.jurianoff.irlmate.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

/**
 * Przechowuje dane sesji Kick: tokeny i dane użytkownika
 */
object KickSession {

    // ─── In‑memory ────────────────────────────────────────────────────────────────
    var accessToken: String? = null
    var refreshToken: String? = null
    var tokenType: String = "Bearer"
    var expiresInSeconds: Long = 7200

    var userId: String? = null
    var username: String? = null

    val isLoggedIn: Boolean
        get() = !accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty() && !userId.isNullOrEmpty()

    // ─── DataStore ────────────────────────────────────────────────────────────────
    private val Context.dataStore by preferencesDataStore(name = "kick_session")

    private val KEY_ACCESS = stringPreferencesKey("access_token")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token")
    private val KEY_TYPE = stringPreferencesKey("token_type")
    private val KEY_EXPIRES = stringPreferencesKey("expires_in")
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_USERNAME = stringPreferencesKey("username")

    suspend fun saveSession(
        context: Context,
        accessToken: String,
        refreshToken: String,
        userId: String?,
        username: String?,
        tokenType: String = "Bearer",
        expiresInSeconds: Long = 7200
    ) {
        // zapisz w pamięci
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.userId = userId
        this.username = username
        this.tokenType = tokenType
        this.expiresInSeconds = expiresInSeconds

        println("💾 [KickSession] Zapisywanie sesji:")
        println("  access: ${accessToken.take(8)}...")
        println("  refresh: ${refreshToken.take(8)}...")
        println("  user_id: $userId, username: $username")

        // zapisz w EncryptedDataStore
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = accessToken
            prefs[KEY_REFRESH] = refreshToken
            userId?.let { prefs[KEY_USER_ID] = it }
            username?.let { prefs[KEY_USERNAME] = it }
            prefs[KEY_TYPE] = tokenType
            prefs[KEY_EXPIRES] = expiresInSeconds.toString()
        }
    }

    suspend fun loadSession(context: Context) {
        context.dataStore.data.first().let { prefs ->
            accessToken = prefs[KEY_ACCESS]
            refreshToken = prefs[KEY_REFRESH]
            userId = prefs[KEY_USER_ID]
            username = prefs[KEY_USERNAME]
            tokenType = prefs[KEY_TYPE] ?: "Bearer"
            expiresInSeconds = prefs[KEY_EXPIRES]?.toLongOrNull() ?: 7200

            println("📦 [KickSession] Załadowano sesję:")
            println("  access: ${accessToken?.take(8)}..., user_id: $userId, username: $username")
        }
    }

    suspend fun clearSession(context: Context) {
        accessToken = null
        refreshToken = null
        userId = null
        username = null
        tokenType = "Bearer"
        expiresInSeconds = 7200
        context.dataStore.edit { it.clear() }

        println("🧹 [KickSession] Wyczyszczono sesję")
    }
}
