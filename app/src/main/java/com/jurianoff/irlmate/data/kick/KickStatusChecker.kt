package com.jurianoff.irlmate.data.kick

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import com.jurianoff.irlmate.ui.settings.KickSession

data class KickStreamStatus(
    val isLive: Boolean,
    val viewers: Int?
)

object KickStatusChecker {
    private val client = OkHttpClient()

    /**
     * Nowa funkcja – czysty parser JSON -> KickStreamStatus (do testów unit i refaktoryzacji)
     */
    fun parseKickStreamStatus(jsonString: String): KickStreamStatus {
        val json = JSONObject(jsonString)
        val livestream = json.optJSONObject("livestream")
        return if (livestream != null && livestream.optBoolean("is_live")) {
            val viewers = livestream.optInt("viewer_count", 0)
            KickStreamStatus(true, viewers)
        } else {
            KickStreamStatus(false, null)
        }
    }

    // Oryginalna funkcja, teraz korzysta z parsera!
    suspend fun getStreamStatus(context: Context, channelName: String): KickStreamStatus = withContext(Dispatchers.IO) {
        try {
            // Odśwież token, jeśli trzeba
            if (!KickSession.ensureValidAccessToken(context)) {
                println("❌ [KickStatusChecker] Brak ważnego tokena – sesja wygasła")
                return@withContext KickStreamStatus(false, null)
            }

            val request = createKickRequest("https://kick.com/api/v2/channels/$channelName")

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("❌ [KickStatusChecker] Błąd odpowiedzi: ${response.code}")
                    return@withContext KickStreamStatus(false, null)
                }

                val body = response.body?.string() ?: return@withContext KickStreamStatus(false, null)
                // Korzystamy z parsera:
                return@withContext parseKickStreamStatus(body)
            }
        } catch (e: Exception) {
            println("❌ [KickStatusChecker] Wyjątek: ${e.message}")
            return@withContext KickStreamStatus(false, null)
        }
    }
}
