package com.jurianoff.irlmate.data.twitch

import com.jurianoff.irlmate.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

        val randomNick = "justinfan" + (1000..9999).random()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("✅ [TwitchChatClient] Połączono z IRC Twitch")

                webSocket.send("CAP REQ :twitch.tv/tags")
                webSocket.send("NICK $randomNick")
                webSocket.send("JOIN #$channelName")

                println("📨 [TwitchChatClient] Dołączono do kanału: #$channelName jako $randomNick")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (text.startsWith("PING")) {
                    webSocket.send("PONG :tmi.twitch.tv")
                    return
                }

                val lines = text.split("\r\n").filter { it.isNotBlank() }

                for (line in lines) {
                    if ("PRIVMSG" in line && "display-name=" in line) {
                        try {
                            val user = line.substringAfter("display-name=").substringBefore(";")
                            val message = line.substringAfter("PRIVMSG #$channelName :").trim()
                            val color = line.substringAfter("color=").substringBefore(";").ifBlank { null }

                            val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                            val createdAt = System.currentTimeMillis()

                            onMessageReceived(
                                ChatMessage(
                                    platform = "Twitch",
                                    user = user,
                                    message = message,
                                    userColor = color,
                                    timestamp = timestamp,
                                    createdAt = createdAt
                                )
                            )
                        } catch (e: Exception) {
                            println("⚠️ [TwitchChatClient] Błąd parsowania linii: $line")
                        }
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("❌ [TwitchChatClient] Błąd połączenia: ${t.message}")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnected by user")
        println("🔌 [TwitchChatClient] Połączenie z IRC zostało zakończone")
    }
    companion object {
        /**
         * Parsuje pojedynczą linię IRC Twitch do ChatMessage (podstawowy format).
         * Przykład linii:
         * :someuser!someuser@someuser.tmi.twitch.tv PRIVMSG #somechannel :Treść wiadomości
         */
        fun parseTwitchIrcMessage(ircLine: String): com.jurianoff.irlmate.data.model.ChatMessage? {
            val regex = Regex("""^:([^\s!]+)!.* PRIVMSG #[^\s]+ :(.*)$""")
            val match = regex.find(ircLine) ?: return null

            val username = match.groupValues[1]
            val message = match.groupValues[2]
            val timestamp = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            val createdAt = System.currentTimeMillis()

            return com.jurianoff.irlmate.data.model.ChatMessage(
                platform = "Twitch",
                user = username,
                message = message,
                userColor = null,
                timestamp = timestamp,
                createdAt = createdAt
            )
        }
    }
}
