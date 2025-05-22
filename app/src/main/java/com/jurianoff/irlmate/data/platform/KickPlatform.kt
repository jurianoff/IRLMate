package com.jurianoff.irlmate.data.platform

import android.content.Context
import com.jurianoff.irlmate.data.kick.KickStatusChecker
import com.jurianoff.irlmate.data.kick.PusherKickChatClient
import com.jurianoff.irlmate.ui.settings.KickSession
import com.jurianoff.irlmate.data.model.ChatMessage

class KickPlatform(private val context: Context) : StreamingPlatform(
    name = "Kick",
    isLoggedInProvider = { KickSession.isLoggedIn() },
    isEnabledProvider = { KickSession.showChatAndStatus },
    getStreamStatus = suspend {
        val username = KickSession.username
        val status = if (username != null) KickStatusChecker.getStreamStatus(context, username) else null
        println("ℹ️ [KickPlatform] Status streama: $status")
        status?.let { StreamStatus.Kick(it) }
    },
    connectChat = { onMessage: (ChatMessage) -> Unit ->
        val chatroomId = KickSession.chatroomId
        val username = KickSession.username
        println("🔌 [KickPlatform] connectChat wywołany dla: $username (chatroomId=$chatroomId)")

        if (chatroomId != null) {
            chatClient = PusherKickChatClient(onMessage)
            chatClient?.connect()
        } else {
            println("⚠️ [KickPlatform] chatroomId == null – nie można połączyć z czatem")
        }
    },
    disconnectChat = {
        chatClient?.disconnect()
        println("📴 [KickPlatform] Rozłączono z Kick")
    }
) {
    companion object {
        private var chatClient: PusherKickChatClient? = null
    }
}
