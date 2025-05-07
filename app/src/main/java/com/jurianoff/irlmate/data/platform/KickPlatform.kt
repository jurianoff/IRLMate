package com.jurianoff.irlmate.data.platform

import com.jurianoff.irlmate.data.kick.KickStatusChecker
import com.jurianoff.irlmate.data.kick.PusherKickChatClient
import com.jurianoff.irlmate.ui.settings.KickSession

class KickPlatform : StreamingPlatform(
    name = "Kick",
    isLoggedIn = KickSession.isLoggedIn(),
    isEnabled = KickSession.showChatAndStatus,
    getStreamStatus = {
        val username = KickSession.username
        val status = if (username != null) KickStatusChecker.getStreamStatus(username) else null
        println("ℹ️ [KickPlatform] Status streama: $status")
        status?.let { StreamStatus.Kick(it) }
    },
    connectChat = { onMessage ->
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
