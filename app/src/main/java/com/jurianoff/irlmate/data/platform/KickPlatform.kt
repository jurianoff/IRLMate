package com.jurianoff.irlmate.data.platform

import com.jurianoff.irlmate.data.kick.KickStatusChecker
import com.jurianoff.irlmate.data.kick.PusherKickChatClient
import com.jurianoff.irlmate.ui.settings.KickSession

class KickPlatform : StreamingPlatform(
    name = "Kick",
    isLoggedIn = KickSession.isLoggedIn(),
    isEnabled = KickSession.showChatAndStatus,
    getStreamStatus = {
        KickSession.username?.let { username ->
            KickStatusChecker.getStreamStatus(username)?.let { status ->
                StreamStatus.Kick(status)
            }
        }
    },
    connectChat = { onMessage ->
        val username = KickSession.username
        val channelId = KickSession.channelId

        println("🔌 [KickPlatform] connectChat wywołany dla: $username (channelId=$channelId)")

        if (channelId != null) {
            val client = PusherKickChatClient(channelId, onMessage)
            client.connect()
        } else {
            println("⚠️ [KickPlatform] channelId == null – nie można połączyć z czatem")
        }
    }
)
