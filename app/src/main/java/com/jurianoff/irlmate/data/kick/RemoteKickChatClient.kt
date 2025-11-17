package com.jurianoff.irlmate.data.kick

import android.content.Context
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.model.MessagePart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

class RemoteKickChatClient(
    private val context: Context,
    private val broadcasterId: String,
    private val onMessageReceived: (ChatMessage) -> Unit
) {
    private val client = OkHttpClient()
    private var pollingJob: Job? = null

    // 🔒 Zbiór już przetworzonych ID, żeby nie było duplikatów
    private val seenMessageIds = mutableSetOf<String>()

    // 🕒 Początek sesji – nie chcemy starych wiadomości po restarcie aplikacji
    private var sessionStart: Long = System.currentTimeMillis()

    fun connect() {
        println("[RemoteKickChatClient] Start polling for broadcaster $broadcasterId")
        sessionStart = System.currentTimeMillis()
        seenMessageIds.clear()

        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                fetchMessages()
                // Zostawiamy 2 sekundy – opóźnienie ~1–2s jest OK
                delay(2_000)
            }
        }
    }

    fun disconnect() {
        pollingJob?.cancel()
        pollingJob = null
        seenMessageIds.clear()
    }

    private suspend fun fetchMessages() {
        val url =
            "https://ah2d6m1qy4.execute-api.eu-central-1.amazonaws.com/kick/messages?broadcaster_id=$broadcasterId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("[RemoteKickChatClient] Response error: ${response.code}")
                    return
                }

                val body = response.body?.string() ?: return
                println("[RemoteKickChatClient] Body: $body")
                val root = JSONObject(body)
                val json = root.optJSONArray("items") ?: JSONArray()

                for (i in 0 until json.length()) {
                    val item = json.getJSONObject(i)
                    val id = item.optString("id")
                    if (id.isNullOrEmpty() || seenMessageIds.contains(id)) continue

                    val sender = item.optJSONObject("sender")
                    val username = sender?.optString("username") ?: "KickUser"
                    val contentRaw = item.optString("content", "")
                    val createdAt = parseTimestamp(item.optString("created_at"))

                    // 🧹 Nie ładujemy starej historii po restarcie appki
                    if (createdAt < sessionStart) {
                        continue
                    }

                    val timestamp =
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(createdAt))

                    // 🧩 Parsowanie treści na części (tekst + emotki)
                    val parts = parseKickMessageParts(contentRaw)
                    val displayText = parts.joinToString(separator = "") { part ->
                        when (part) {
                            is MessagePart.Text -> part.text
                            is MessagePart.Emote -> part.alt.ifBlank { "[emote]" }
                        }
                    }

                    onMessageReceived(
                        ChatMessage(
                            platform = "Kick",
                            user = username,
                            message = displayText,
                            // webhook nie niesie koloru w "sender.color"
                            userColor = sender?.optString("color", null),
                            timestamp = timestamp,
                            createdAt = createdAt,
                            parts = parts
                        )
                    )

                    seenMessageIds.add(id)
                }
            }
        } catch (e: Exception) {
            println("[RemoteKickChatClient] Error: ${e.message}")
        }
    }

    private fun parseTimestamp(raw: String?): Long {
        return try {
            if (raw.isNullOrBlank()) System.currentTimeMillis()
            else Instant.parse(raw).toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    /**
     * Parsuje Kick-owe kody typu:
     *   "[emote:1730753:emojiAngry]"
     *   "Siema [emote:1730756:emojiCheerful] test"
     *
     * Jeśli wiadomość to TYLKO emotki, zwracamy Text z ich nazwą,
     * żeby UI nie pokazywał pustej bańki, gdy nie ma URL-a obrazka.
     */
    private fun parseKickMessageParts(content: String): List<MessagePart> {
    if (content.isBlank()) return listOf(MessagePart.Text(""))

    val regex = "\\[emote:(\\d+):([^\\]]+)]".toRegex()
    val parts = mutableListOf<MessagePart>()
    var lastIndex = 0

    for (match in regex.findAll(content)) {
        val range = match.range

        // Tekst przed emote
        if (range.first > lastIndex) {
            val text = content.substring(lastIndex, range.first)
            if (text.isNotEmpty()) {
                parts += MessagePart.Text(text)
            }
        }

        val emoteId = match.groupValues[1]
        val emoteName = match.groupValues[2]

        // 🔗 Bezpośredni URL do emotki w CDN Kick
        val emoteUrl = "https://files.kick.com/emotes/$emoteId/fullsize"

        parts += MessagePart.Emote(
            url = emoteUrl,
            alt = emoteName,
            fallbackUrl = null
        )

        lastIndex = range.last + 1
    }

    // Tekst po ostatnim emote
    if (lastIndex < content.length) {
        val text = content.substring(lastIndex)
        if (text.isNotEmpty()) {
            parts += MessagePart.Text(text)
        }
    }

    // Jeśli regex nic nie znalazł – całość to zwykły tekst
    if (parts.isEmpty()) {
        parts += MessagePart.Text(content)
    }

    return parts
}


}
