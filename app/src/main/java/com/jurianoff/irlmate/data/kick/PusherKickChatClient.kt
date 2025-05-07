package com.jurianoff.irlmate.data.kick

import com.jurianoff.irlmate.data.model.ChatMessage
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.SubscriptionEventListener
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PusherKickChatClient(
    private val channelId: String,
    private val onMessageReceived: (ChatMessage) -> Unit
) {

    private var pusher: Pusher? = null

    fun connect() {
        val options = PusherOptions().apply {
            setCluster("us2")
            isUseTLS = true
        }

        pusher = Pusher("32cbd69e4b950bf97679", options)
        val channelName = "chatrooms.$channelId.v2"

        println("🔌 [PusherKickChatClient] Łączenie z kanałem: $channelName")

        val channel = pusher!!.subscribe(channelName)

        channel.bind("App\\Events\\ChatMessageEvent", SubscriptionEventListener { event ->
            try {
                val json = JSONObject(event.data)
                val messageObj = json.getJSONObject("data")

                val username = messageObj.getJSONObject("sender").getString("username")
                val message = messageObj.getString("content")

                val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                println("💬 [PusherKickChatClient] $username: $message")

                onMessageReceived(
                    ChatMessage(
                        platform = "Kick",
                        user = username,
                        message = message,
                        userColor = null,
                        timestamp = timestamp
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
}
