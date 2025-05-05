package com.jurianoff.irlmate.data.kick

import com.jurianoff.irlmate.data.model.ChatMessage
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class KickChatClient(
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

    private fun getChannelId(channelName: String): String? {
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

    //  ───────────────────────────────────────────────────────────────
//  Zamiana T E J  funkcji w KickChatClient.kt
//  ───────────────────────────────────────────────────────────────
    private fun fetchMessages(channelId: String) {
        try {
            val request = createKickRequest(
                "https://kick.com/api/v2/chatrooms/$channelId/messages"
            )

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("❌ [KickChatClient] Błąd pobierania wiadomości: ${response.code}")
                    return
                }

                val body = response.body?.string() ?: return

                // Jeśli serwer odpowie HTML‑em (np. stroną błędu/logowania),
                // pomijamy parsowanie, żeby nie spamować LogCat.
                val trimmed = body.trim()
                if (!trimmed.startsWith("[")) {      // oczekujemy JSON‑owej tablicy
                    return
                }

                val messagesArray = JSONArray(trimmed)

                for (i in 0 until messagesArray.length()) {
                    val msg = messagesArray.getJSONObject(i)
                    val id = msg.getInt("id")

                    if (lastMessageId != null && id <= lastMessageId!!) continue

                    val user = msg.getJSONObject("sender").getString("username")
                    val message = msg.getString("content")
                    val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

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
            // Tu zostawiamy log, żeby widzieć faktyczne wyjątki sieciowe/JSON,
            // ale błąd HTML‑parsing już nie trafi do tego miejsca.
            println("❌ [KickChatClient] Błąd pobierania wiadomości: ${e.message}")
        }
    }

}
