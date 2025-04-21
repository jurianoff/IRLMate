package com.example.streamchat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.net.URLEncoder
import java.util.concurrent.TimeUnit


class TwitchChatClient(
    private val channelName: String,
    private val onMessageReceived: (ChatMessage) -> Unit
) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null

    suspend fun connect() = withContext(Dispatchers.IO) {
        println("📡 [TwitchChatClient] Nawiązywanie połączenia IRC WebSocket...")

        val request = Request.Builder()
            .url("wss://irc-ws.chat.twitch.tv:443")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("✅ [TwitchChatClient] Połączono z IRC Twitch")

                webSocket.send("CAP REQ :twitch.tv/tags")
                webSocket.send("NICK justinfan12345")
                webSocket.send("JOIN #$channelName")

                println("📨 [TwitchChatClient] Dołączono do kanału: #$channelName")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (text.startsWith("PING")) {
                    webSocket.send("PONG :tmi.twitch.tv")
                    return
                }

                val lines = text.split("\r\n")
                for (line in lines) {
                    if (line.contains("PRIVMSG")) {
                        val user = line.substringAfter("display-name=").substringBefore(";")
                        val message = line.substringAfter("PRIVMSG #$channelName :")
                        val color = line.substringAfter("color=").substringBefore(";").ifBlank { null }

                        val timestamp = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date())

                        onMessageReceived(
                            ChatMessage(
                                platform = "Twitch",
                                user = user,
                                message = message,
                                userColor = color,
                                timestamp = timestamp
                            )
                        )

                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("❌ [TwitchChatClient] Błąd połączenia: ${t.message}")
            }
        })
    }
}
