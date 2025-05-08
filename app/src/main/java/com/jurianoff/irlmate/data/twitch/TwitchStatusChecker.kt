package com.jurianoff.irlmate.data.twitch

import com.jurianoff.irlmate.ui.settings.TwitchSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.jurianoff.irlmate.BuildConfig



data class TwitchStreamStatus(
    val isLive: Boolean,
    val viewers: Int?
)

object TwitchStatusChecker {
    private val client = OkHttpClient()

    suspend fun getStreamStatus(): TwitchStreamStatus? = withContext(Dispatchers.IO) {
        val userId = TwitchSession.userId
        val accessToken = TwitchSession.accessToken
        val clientId = BuildConfig.TWITCH_CLIENT_ID

        if (userId.isNullOrEmpty() || accessToken.isNullOrEmpty()) {
            println("⚠️ [TwitchStatusChecker] Brak wymaganych danych – użytkownik nie jest zalogowany.")
            return@withContext null
        }

        val request = Request.Builder()
            .url("https://api.twitch.tv/helix/streams?user_id=$userId")
            .addHeader("Client-ID", clientId)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("⚠️ [TwitchStatusChecker] Nieudane zapytanie: ${response.code}")
                    return@withContext TwitchStreamStatus(false, null)
                }

                val body = response.body?.string() ?: return@withContext TwitchStreamStatus(false, null)
                val json = JSONObject(body)
                val dataArray = json.getJSONArray("data")

                if (dataArray.length() > 0) {
                    val streamData = dataArray.getJSONObject(0)
                    val viewerCount = streamData.getInt("viewer_count")
                    return@withContext TwitchStreamStatus(true, viewerCount)
                }

                return@withContext TwitchStreamStatus(false, null)
            }
        } catch (e: Exception) {
            println("❌ [TwitchStatusChecker] Błąd: ${e.message}")
            return@withContext null
        }
    }
}
