package com.jurianoff.irlmate.data.twitch

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
    private const val CLIENT_ID = "gp762nuuoqcoxypju8c569th9wz7q5"
    private const val ACCESS_TOKEN = "bq6sern242s2kkoe24bf8vigq8doo2"
    private const val CHANNEL_NAME = "jurianoff"

    suspend fun getStreamStatus(): TwitchStreamStatus = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.twitch.tv/helix/streams?user_login=$CHANNEL_NAME")
                .addHeader("Client-ID", CLIENT_ID)
                .addHeader("Authorization", "Bearer $ACCESS_TOKEN")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext TwitchStreamStatus(false, null)

                val body =
                    response.body?.string() ?: return@withContext TwitchStreamStatus(false, null)
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
            println("❌ Błąd pobierania statusu Twitch: ${e.message}")
            return@withContext TwitchStreamStatus(false, null)
        }
    }
}