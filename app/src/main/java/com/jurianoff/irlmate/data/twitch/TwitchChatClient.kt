package com.jurianoff.irlmate.data.twitch

import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.model.MessagePart
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
                            // [emotes] Parsowanie tagów IRC
                            val tagsPart = line.substringAfter('@').substringBefore(' ')
                            val tags = tagsPart.split(';').associate { tag ->
                                val (k, v) = tag.split('=', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
                                k to v
                            }
                            val user = tags["display-name"] ?: "unknown"
                            val message = line.substringAfter("PRIVMSG #$channelName :").trim()
                            val color = tags["color"].takeIf { !it.isNullOrBlank() }
                            val emotes = tags["emotes"]

                            val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                            val createdAt = System.currentTimeMillis()

                            onMessageReceived(
                                ChatMessage(
                                    platform = "Twitch",
                                    user = user,
                                    message = message,
                                    userColor = color,
                                    timestamp = timestamp,
                                    createdAt = createdAt,
                                    parts = parseTwitchEmotes(message, emotes) // [emotes]
                                )
                            )
                        } catch (e: Exception) {
                            println("⚠️ [TwitchChatClient] Błąd parsowania linii: $line\n$e")
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
            val regex = Regex("""^@(.+?)\s+:([^\s!]+)!.* PRIVMSG #[^\s]+ :(.*)$""")
            val match = regex.find(ircLine) ?: return null

            // [emotes] Parsowanie tagów IRC
            val tagsRaw = match.groupValues[1]
            val tags = tagsRaw.split(';').associate { tag ->
                val (k, v) = tag.split('=', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
                k to v
            }
            val username = tags["display-name"] ?: match.groupValues[2]
            val message = match.groupValues[3]
            val color = tags["color"].takeIf { !it.isNullOrBlank() }
            val emotes = tags["emotes"]

            val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val createdAt = System.currentTimeMillis()

            return com.jurianoff.irlmate.data.model.ChatMessage(
                platform = "Twitch",
                user = username,
                message = message,
                userColor = color,
                timestamp = timestamp,
                createdAt = createdAt,
                parts = parseTwitchEmotes(message, emotes) // [emotes]
            )
        }
    }
}

// [emotes] Funkcja pomocnicza do parsowania emotek Twitch
private fun parseTwitchEmotes(
    message: String,
    emotesTag: String?
): List<MessagePart> {
    if (emotesTag.isNullOrBlank() || emotesTag == "") return listOf(MessagePart.Text(message))

    data class EmoteRange(val start: Int, val end: Int, val emoteId: String)
    val emoteRanges = emotesTag.split('/').flatMap { group ->
        val splitGroup = group.split(':')
        if (splitGroup.size < 2) return@flatMap emptyList<EmoteRange>()
        val emoteId = splitGroup[0]
        splitGroup[1].split(',').map { range ->
            val (start, end) = range.split('-').map { it.toInt() }
            EmoteRange(start, end, emoteId)
        }
    }

    val map = emoteRanges.associateBy { it.start }
    val parts = mutableListOf<MessagePart>()

    var i = 0
    while (i < message.length) {
        val emoteRange = map[i]
        if (emoteRange != null) {
            val emoteText = message.substring(emoteRange.start, emoteRange.end + 1)
            val animatedUrl = "https://static-cdn.jtvnw.net/emoticons/v2/${emoteRange.emoteId}/animated/dark/1.0"
            val staticUrl = "https://static-cdn.jtvnw.net/emoticons/v2/${emoteRange.emoteId}/default/dark/1.0"
            parts += MessagePart.Emote(animatedUrl, emoteText, fallbackUrl = staticUrl)
            i = emoteRange.end + 1
        } else {
            val nextEmote = map.keys.filter { it > i }.minOrNull() ?: message.length
            if (i < nextEmote) {
                parts += MessagePart.Text(message.substring(i, nextEmote))
            }
            i = nextEmote
        }
    }
    return parts
}
