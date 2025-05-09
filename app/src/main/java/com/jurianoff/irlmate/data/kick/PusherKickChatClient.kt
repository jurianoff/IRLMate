package com.jurianoff.irlmate.data.kick

import com.jurianoff.irlmate.data.model.ChatMessage
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
                println("📶 [PusherKickChatClient] Status: ${change.currentState}")
            }

            override fun onError(message: String?, code: String?, e: Exception?) {
                println("❌ [PusherKickChatClient] Błąd połączenia: $message ($code) ${e?.message}")
            }
        })

        val channel = pusher!!.subscribe(channelName)

        channel.bind("App\\Events\\ChatMessageEvent", SubscriptionEventListener { event ->
            try {
                println("📥 [PusherKickChatClient] Surowa wiadomość: ${event.data}")
                val messageObj = JSONObject(event.data)

                val username = messageObj.getJSONObject("sender").getString("username")
                val message = messageObj.getString("content")
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
                        createdAt = createdAt
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
