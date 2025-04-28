package com.jurianoff.irlmate.data.kick

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class KickStreamStatus(
    val isLive: Boolean,
    val viewers: Int?
)

object KickStatusChecker {
    private val client = OkHttpClient()
    private const val CHANNEL_NAME = "jurianoff"

    suspend fun getStreamStatus(): KickStreamStatus = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://kick.com/api/v2/channels/$CHANNEL_NAME")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext KickStreamStatus(false, null)

                val body =
                    response.body?.string() ?: return@withContext KickStreamStatus(false, null)
                val json = JSONObject(body)

                val livestream = json.optJSONObject("livestream")
                if (livestream != null && livestream.optBoolean("is_live")) {
                    val viewerCount = livestream.optInt("viewer_count", 0)
                    return@withContext KickStreamStatus(true, viewerCount)
                }

                return@withContext KickStreamStatus(false, null)
            }
        } catch (e: Exception) {
            println("❌ Błąd pobierania statusu Kick: ${e.message}")
            return@withContext KickStreamStatus(false, null)
        }
    }
}