package com.jurianoff.irlmate.data.twitch

import android.content.Context
import com.jurianoff.irlmate.BuildConfig
import com.jurianoff.irlmate.ui.settings.TwitchSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class TwitchStreamStatus(
    val isLive: Boolean,
    val viewers: Int?
)

object TwitchStatusChecker {
    private val client = OkHttpClient()

    /** ──────────────────────────────────────────────────────────────────────────
     *  CZYSTY PARSER  -  do testów JVM (nie wymaga Androida, sieci ani coroutines)
     *  ───────────────────────────────────────────────────────────────────────── */
    fun parseTwitchStreamStatus(jsonString: String): TwitchStreamStatus {
        return try {
            val root = JSONObject(jsonString)
            val dataArr = root.optJSONArray("data") ?: return TwitchStreamStatus(false, null)
            if (dataArr.length() == 0) return TwitchStreamStatus(false, null)

            val stream = dataArr.getJSONObject(0)
            val viewerCount = stream.optInt("viewer_count", 0)
            TwitchStreamStatus(true, viewerCount)
        } catch (e: Exception) {
            // Nieudane parsowanie = traktujemy jako offline
            TwitchStreamStatus(false, null)
        }
    }

    /**  Oryginalna metoda – teraz korzysta z parsera                              */
    suspend fun getStreamStatus(context: Context): TwitchStreamStatus? = withContext(Dispatchers.IO) {
        if (!TwitchSession.ensureValidAccessToken(context)) {
            println("⚠️ [TwitchStatusChecker] Access token nieważny i nie udało się go odświeżyć.")
            return@withContext null
        }

        val userId = TwitchSession.userId
        val accessToken = TwitchSession.accessToken
        val clientId = BuildConfig.TWITCH_CLIENT_ID

        if (userId.isNullOrEmpty() || accessToken.isNullOrEmpty()) {
            println("⚠️ [TwitchStatusChecker] Brak wymaganych danych – użytkownik niezalogowany.")
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
                    println("⚠️ [TwitchStatusChecker] HTTP ${response.code}")
                    return@withContext TwitchStreamStatus(false, null)
                }

                val body = response.body?.string() ?: return@withContext TwitchStreamStatus(false, null)
                return@withContext parseTwitchStreamStatus(body)
            }
        } catch (e: Exception) {
            println("❌ [TwitchStatusChecker] Błąd sieci: ${e.message}")
            return@withContext null
        }
    }
}
