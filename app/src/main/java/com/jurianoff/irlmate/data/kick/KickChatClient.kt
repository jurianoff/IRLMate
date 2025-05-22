package com.jurianoff.irlmate.data.kick

import android.content.Context
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.ui.settings.KickSession
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class KickChatClient(
    private val context: Context, // <-- DODANE!
    private val channelName: String,
    private val onMessageReceived: (ChatMessage) -> Unit
) {

    private val client = OkHttpClient()
    private var pollingJob: Job? = null
    private var lastMessageId: Int? = null

    fun connect() {
        println("📡 [KickChatClient] Start pobierania wiadomości z kanału: $channelName")

        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            val channelId = getChannelId(channelName)
            if (channelId == null) {
                println("❌ [KickChatClient] Nie udało się pobrać channelId")
                return@launch
            }

            println("📨 [KickChatClient] channelId Kick: $channelId")

            while (isActive) {
                fetchMessages(channelId)
                delay(2000) // Odświeżanie co 2 sekundy
            }
        }
    }

    fun disconnect() {
        pollingJob?.cancel()
    }

    private suspend fun getChannelId(channelName: String): String? {
        // --- REFRESH TOKEN PRZED ŻĄDANIEM ---
        if (!KickSession.ensureValidAccessToken(context)) {
            println("❌ [KickChatClient] Token nieważny lub nie można odświeżyć – wylogowano")
            return null
        }
        return try {
            val request = createKickRequest("https://kick.com/api/v2/channels/$channelName")

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("❌ [KickChatClient] Błąd pobierania channelId: ${response.code}")
                    return null
                }

                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                json.getString("id")
            }
        } catch (e: Exception) {
            println("❌ [KickChatClient] Błąd pobierania channelId: ${e.message}")
            null
        }
    }

    private suspend fun fetchMessages(channelId: String) {
        // --- REFRESH TOKEN PRZED ŻĄDANIEM ---
        if (!KickSession.ensureValidAccessToken(context)) {
            println("❌ [KickChatClient] Token nieważny lub nie można odświeżyć – wylogowano")
            return
        }
        try {
            val request = createKickRequest(
                "https://kick.com/api/v2/chatrooms/$channelId/messages"
            )

            println("🔑 [KickChatClient] Używam tokenu: ${KickSession.accessToken?.take(10)}...")

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("❌ [KickChatClient] Błąd pobierania wiadomości: ${response.code}")
                    return
                }

                val body = response.body?.string() ?: return

                val trimmed = body.trim()
                println("🧪 [KickChatClient] Odpowiedź serwera:\n$trimmed")

                if (!trimmed.startsWith("[")) {
                    println("⚠️ [KickChatClient] Odpowiedź nie jest tablicą JSON, prawdopodobnie HTML.")
                    return
                }

                val messagesArray = JSONArray(trimmed)
                println("📥 [KickChatClient] Odebrano ${messagesArray.length()} wiadomości")

                for (i in 0 until messagesArray.length()) {
                    val msg = messagesArray.getJSONObject(i)
                    val id = msg.getInt("id")

                    if (lastMessageId != null && id <= lastMessageId!!) continue

                    val user = msg.getJSONObject("sender").getString("username")
                    val message = msg.getString("content")
                    val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                    println("💬 [KickChatClient] Nowa wiadomość: [$user] $message")

                    onMessageReceived(
                        ChatMessage(
                            platform = "Kick",
                            user = user,
                            message = message,
                            userColor = null,
                            timestamp = timestamp
                        )
                    )

                    lastMessageId = id
                }
            }
        } catch (e: Exception) {
            println("❌ [KickChatClient] Błąd pobierania wiadomości: ${e.message}")
        }
    }

    private fun createKickRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .apply {
                KickSession.accessToken?.let { token ->
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()
    }
}
