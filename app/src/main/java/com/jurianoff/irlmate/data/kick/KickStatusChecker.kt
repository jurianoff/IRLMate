package com.jurianoff.irlmate.data.kick

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject

data class KickStreamStatus(
    val isLive: Boolean,
    val viewers: Int?
)

object KickStatusChecker {
    private val client = OkHttpClient()

    suspend fun getStreamStatus(channelName: String): KickStreamStatus = withContext(Dispatchers.IO) {
        try {
            val request = createKickRequest("https://kick.com/api/v2/channels/$channelName")

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("❌ [KickStatusChecker] Błąd odpowiedzi: ${response.code}")
                    return@withContext KickStreamStatus(false, null)
                }

                val body = response.body?.string() ?: return@withContext KickStreamStatus(false, null)
                val json = JSONObject(body)

                val livestream = json.optJSONObject("livestream")
                if (livestream != null && livestream.optBoolean("is_live")) {
                    val viewerCount = livestream.optInt("viewer_count", 0)
                    return@withContext KickStreamStatus(true, viewerCount)
                }

                return@withContext KickStreamStatus(false, null)
            }
        } catch (e: Exception) {
            println("❌ [KickStatusChecker] Wyjątek: ${e.message}")
            return@withContext KickStreamStatus(false, null)
        }
    }
}
