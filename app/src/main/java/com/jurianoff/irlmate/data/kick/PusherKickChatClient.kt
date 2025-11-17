package com.jurianoff.irlmate.data.kick

import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.model.MessagePart
import com.jurianoff.irlmate.ui.settings.KickSession
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.SubscriptionEventListener
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import com.jurianoff.irlmate.data.kick.parseKickInlineEmotes

class PusherKickChatClient(
    private val onMessageReceived: (ChatMessage) -> Unit
) {
    private var pusher: Pusher? = null

    fun connect() {
        val chatroomId = KickSession.chatroomId
        if (chatroomId.isNullOrEmpty()) {
            println("❌ [PusherKickChatClient] chatroomId == null – nie można połączyć")
            return
        }

        val options = PusherOptions().apply {
            setCluster("us2")
            isUseTLS = true
        }

        pusher = Pusher("32cbd69e4b950bf97679", options)
        val channelName = "chatrooms.$chatroomId.v2"

        println("🔌 [PusherKickChatClient] Łączenie z kanałem: $channelName")

        pusher?.connection?.bind(ConnectionState.ALL, object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                println(
                    "📶 [PusherKickChatClient] Status zmiana: ${change.previousState} -> ${change.currentState}"
                )
            }

            override fun onError(message: String?, code: String?, e: Exception?) {
                println("❌ [PusherKickChatClient] Błąd połączenia: $message ($code) ${e?.message}")
            }
        })

        val channel = pusher!!.subscribe(channelName)

        channel.bindGlobal(SubscriptionEventListener { event ->
            println("🌐 [PusherKickChatClient] Global event ${event.eventName}: ${event.data}")
        })

        channel.bind("App\\Events\\ChatMessageEvent", SubscriptionEventListener { event ->
            println("ℹ️ [PusherKickChatClient] Otrzymano event: ${event.eventName} length=${event.data?.length}")
            try {
                println("📥 [PusherKickChatClient] Surowa wiadomość: ${event.data}")
                val messageObj = JSONObject(event.data)

                val username = messageObj.getJSONObject("sender").getString("username")
                val message = messageObj.getString("content")
                val emotesArray = if (messageObj.has("emotes") && !messageObj.isNull("emotes"))
                    messageObj.getJSONArray("emotes") else null
                val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val createdAt = System.currentTimeMillis()

                println("💬 [PusherKickChatClient] $username: $message")

                onMessageReceived(
                    ChatMessage(
                        platform = "Kick",
                        user = username,
                        message = message,
                        userColor = null,
                        timestamp = timestamp,
                        createdAt = createdAt,
                        parts = parseKickEmotes(message, emotesArray)
                    )
                )
            } catch (e: Exception) {
                println("❌ [PusherKickChatClient] Błąd parsowania wiadomości: ${e.message}")
            }
        })

        pusher!!.connect()
    }

    fun disconnect() {
        pusher?.disconnect()
        println("📴 [PusherKickChatClient] Rozłączono z Pusher")
    }

    companion object {
        fun parseKickChatEvent(jsonString: String): ChatMessage {
            val messageObj = JSONObject(jsonString)

            val senderObj = messageObj.getJSONObject("sender")
            val username = senderObj.getString("username")
            val message = messageObj.getString("content")
            val emotesArray = if (messageObj.has("emotes") && !messageObj.isNull("emotes"))
                messageObj.getJSONArray("emotes") else null
            val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val createdAt = System.currentTimeMillis()

            return ChatMessage(
                platform = "Kick",
                user = username,
                message = message,
                userColor = null,
                timestamp = timestamp,
                createdAt = createdAt,
                parts = parseKickEmotes(message, emotesArray)
            )
        }

        private fun parseKickEmotes(
            message: String,
            emotesArray: org.json.JSONArray?
        ): List<MessagePart> {
            if (emotesArray == null || emotesArray.length() == 0)
                return parseKickInlineEmotes(message)
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
}

// Dla funkcji poza companion object, np. w klasie głównej:
private fun parseKickEmotes(
    message: String,
    emotesArray: org.json.JSONArray?
): List<MessagePart> {
    if (emotesArray == null || emotesArray.length() == 0)
        return parseKickInlineEmotes(message)
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
