package com.jurianoff.irlmate.data.platform

import android.content.Context
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.twitch.TwitchChatClient
import com.jurianoff.irlmate.data.twitch.TwitchStatusChecker
import com.jurianoff.irlmate.data.twitch.TwitchStreamStatus
import com.jurianoff.irlmate.ui.settings.TwitchSession

class TwitchPlatform(private val context: Context) : StreamingPlatform(
    name = "Twitch",
    isLoggedInProvider = { TwitchSession.isLoggedIn() },
    isEnabledProvider = { TwitchSession.showChatAndStatus },
    getStreamStatus = suspend {
        val status = TwitchStatusChecker.getStreamStatus(context)
        println("ℹ️ [TwitchPlatform] Status streama: $status")
        status?.let { StreamStatus.Twitch(it) }
    },
    connectChat = { onMessage: (ChatMessage) -> Unit ->
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
