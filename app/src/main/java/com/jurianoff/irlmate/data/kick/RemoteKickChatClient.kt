package com.jurianoff.irlmate.data.kick

import android.content.Context
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.model.MessagePart
import com.jurianoff.irlmate.ui.settings.KickSession
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import com.jurianoff.irlmate.data.kick.parseKickInlineEmotes

class RemoteKickChatClient(
    private val context: Context,
    private val channelName: String,
    private val onMessageReceived: (ChatMessage) -> Unit
) {
    private val client = OkHttpClient()
    private var pollingJob: Job? = null
    private var lastMessageId: Int? = null

    fun connect() {
        println("📡 [RemoteKickChatClient] Rozpoczynam pobieranie wiadomości z kanału: $channelName")

        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                fetchMessages()
                delay(2000)
            }
        }
    }

    fun disconnect() {
        pollingJob?.cancel()
    }

    private suspend fun fetchMessages() {
        if (!KickSession.ensureValidAccessToken(context)) {
            println("⚠️ [RemoteKickChatClient] Brak ważnego tokena – sesja wygasła, wylogowano.")
            return
        }

        val token = KickSession.accessToken

        if (token.isNullOrEmpty()) {
            println("⚠️ [RemoteKickChatClient] accessToken == null – nie można pobrać wiadomości")
            return
        }

        val url =
            "https://ah2d6m1qy4.execute-api.eu-central-1.amazonaws.com/kick/messages?channel=$channelName&token=$token"
        println("🌐 [RemoteKickChatClient] Wysyłam zapytanie: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("❌ [RemoteKickChatClient] Odpowiedź nieudana: ${response.code}")
                    return
                }

                val body = response.body?.string() ?: return

                if (!body.trim().startsWith("[")) {
                    println("⚠️ [RemoteKickChatClient] Odpowiedź to nie JSON-array: $body")
                    return
                }

                val json = JSONArray(body)
                println("📥 [RemoteKickChatClient] Odebrano ${json.length()} wiadomości")

                for (i in 0 until json.length()) {
                    val msg = json.getJSONObject(i)
                    val id = msg.getInt("id")
                    if (lastMessageId != null && id <= lastMessageId!!) continue

                    val user = msg.getJSONObject("sender").getString("username")
                    val content = msg.getString("content")
                    val emotesArray = if (msg.has("emotes") && !msg.isNull("emotes")) msg.getJSONArray("emotes") else null
                    val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                    println("💬 [RemoteKickChatClient] Nowa wiadomość: [$user] $content")

                    onMessageReceived(
                        ChatMessage(
                            platform = "Kick",
                            user = user,
                            message = content,
                            userColor = null,
                            timestamp = timestamp,
                            parts = parseKickEmotes(content, emotesArray)
                        )
                    )

                    lastMessageId = id
                }
            }
        } catch (e: Exception) {
            println("❌ [RemoteKickChatClient] Błąd podczas pobierania wiadomości: ${e.message}")
        }
    }

    // ====== EMOTE PARSER ======
    private fun parseKickEmotes(
        message: String,
        emotesArray: JSONArray?
    ): List<MessagePart> {
        // Najważniejsza linia!
        if (emotesArray == null || emotesArray.length() == 0) return parseKickInlineEmotes(message)
        data class EmoteRange(val start: Int, val end: Int, val url: String, val code: String)
        val emoteRanges = mutableListOf<EmoteRange>()
        for (i in 0 until emotesArray.length()) {
            val emoteObj = emotesArray.getJSONObject(i)
            val start = emoteObj.getInt("start")
            val end = emoteObj.getInt("end")
            val url = emoteObj.optString("url")
            val code = emoteObj.optString("name")
            emoteRanges += EmoteRange(start, end, url, code)
        }
        val sortedEmotes = emoteRanges.sortedBy { it.start }
        val parts = mutableListOf<MessagePart>()

        var i = 0
        while (i < message.length) {
            val emote = sortedEmotes.firstOrNull { it.start == i }
            if (emote != null) {
                parts += MessagePart.Emote(emote.url, message.substring(emote.start, emote.end + 1))
                i = emote.end + 1
            } else {
                val nextEmoteStart = sortedEmotes.map { it.start }.firstOrNull { it > i } ?: message.length
                if (i < nextEmoteStart) {
                    parts += MessagePart.Text(message.substring(i, nextEmoteStart))
                }
                i = nextEmoteStart
            }
        }
        return parts
    }
}
