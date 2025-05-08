package com.jurianoff.irlmate.data.platform

import com.jurianoff.irlmate.data.twitch.TwitchChatClient
import com.jurianoff.irlmate.data.twitch.TwitchStatusChecker
import com.jurianoff.irlmate.ui.settings.TwitchSession
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.twitch.TwitchStreamStatus

class TwitchPlatform : StreamingPlatform(
    name = "Twitch",
    isLoggedIn = TwitchSession.isLoggedIn(),
    isEnabled = TwitchSession.showChatAndStatus,
    getStreamStatus = {
        val status = TwitchStatusChecker.getStreamStatus()
        println("ℹ️ [TwitchPlatform] Status streama: $status")
        status?.let { StreamStatus.Twitch(it) }
    },
    connectChat = { onMessage ->
        TwitchSession.username?.let { username ->
            println("🔌 [TwitchPlatform] Połączenie z czatem Twitch jako: $username")
            chatClient = TwitchChatClient(username, onMessage)
            chatClient?.connect()
        } ?: println("⚠️ [TwitchPlatform] Brak username – nie można połączyć z czatem")
    },
    disconnectChat = {
        chatClient?.disconnect()
        println("📴 [TwitchPlatform] Rozłączono z Twitch")
    }
) {
    companion object {
        private var chatClient: TwitchChatClient? = null
    }
}
