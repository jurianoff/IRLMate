package com.jurianoff.irlmate.data.kick

import android.content.Context
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.model.MessagePart
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class RemoteKickChatClient(
    private val context: Context,
    private val broadcasterId: String,
    private val onMessageReceived: (ChatMessage) -> Unit
) {
    private val client = OkHttpClient()
    private var pollingJob: Job? = null
    private var lastMessageId: String? = null

    fun connect() {
        println("📡 [RemoteKickChatClient] Start polling for broadcaster $broadcasterId")
        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                fetchMessages()
                delay(2_000)
            }
        }
    }

    fun disconnect() {
        pollingJob?.cancel()
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
                    println("❌ [RemoteKickChatClient] Response error: ${response.code}")
                    return
                }

                val body = response.body?.string() ?: return
                val root = JSONObject(body)
                val json = root.optJSONArray("items") ?: JSONArray()

                for (i in 0 until json.length()) {
                    val item = json.getJSONObject(i)
                    val id = item.optString("id")
                    if (id.isNullOrEmpty() || id == lastMessageId) continue

                    val sender = item.optJSONObject("sender")
                    val username = sender?.optString("username") ?: "KickUser"
                    val content = item.optString("content", "")
                    val createdAt = parseTimestamp(item.optString("created_at"))
                    val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(createdAt))

                    onMessageReceived(
                        ChatMessage(
                            platform = "Kick",
                            user = username,
                            message = content,
                            userColor = sender?.optString("color"),
                            timestamp = timestamp,
                            createdAt = createdAt,
                            parts = listOf<MessagePart>(MessagePart.Text(content))
                        )
                    )

                    lastMessageId = id
                }
            }
        } catch (e: Exception) {
            println("❌ [RemoteKickChatClient] Error: ${e.message}")
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
}
