package com.jurianoff.irlmate.data.kick

import com.jurianoff.irlmate.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class KickChatClient(private val onMessageReceived: (ChatMessage) -> Unit) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val channelId = "56774420" // Możesz to dynamicznie pobierać później

    suspend fun connect() = withContext(Dispatchers.IO) {
        println("📡 [KickChatClient] Nawiązywanie połączenia z Pusher WebSocket...")

        val request = Request.Builder()
            .url("wss://ws-us2.pusher.com/app/32cbd69e4b950bf97679?protocol=7&client=js&version=8.4.0-rc2&flash=false")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("✅ [KickChatClient] Połączono z WebSocket Pusher")

                val subscribeMessage = JSONObject().apply {
                    put("event", "pusher:subscribe")
                    put("data", JSONObject().apply {
                        put("channel", "chatrooms.$channelId.v2")
                    })
                }.toString()

                webSocket.send(subscribeMessage)
                println("📨 [KickChatClient] Subskrypcja kanału Kick: chatrooms.$channelId.v2")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("💬 [KickChatClient] Odebrano wiadomość: $text")

                val json = JSONObject(text)
                val event = json.optString("event")

                if (event == "App\\Events\\ChatMessageEvent") {
                    val data = JSONObject(json.getString("data"))
                    val user = data.getJSONObject("sender").getString("username")
                    val message = data.getString("content")

                    // Dodaj timestamp (HH:mm)
                    val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date())

                    onMessageReceived(
                        ChatMessage(
                            platform = "Kick",
                            user = user,
                            message = message,
                            userColor = null,
                            timestamp = timestamp
                        )
                    )
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("❌ [KickChatClient] Błąd połączenia: ${t.message}")
            }
        })
    }
}