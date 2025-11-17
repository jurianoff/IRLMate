package com.jurianoff.irlmate.data.platform

import android.content.Context
import com.jurianoff.irlmate.data.kick.KickStatusChecker
import com.jurianoff.irlmate.data.kick.RemoteKickChatClient
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.ui.settings.KickSession

class KickPlatform(private val context: Context) : StreamingPlatform(
    name = "Kick",
    isLoggedInProvider = { KickSession.isLoggedIn() },
    isEnabledProvider = { KickSession.showChatAndStatus },
    getStreamStatus = suspend {
        val username = KickSession.username
        val status = if (username != null) KickStatusChecker.getStreamStatus(context, username) else null
        println("[KickPlatform] Stream status: $status")
        status?.let { StreamStatus.Kick(it) }
    },
    connectChat = {
        KickPlatform.instance.startChat(it)
    },
    disconnectChat = {
        KickPlatform.instance.stopChat()
    }
) {
    private var chatClient: RemoteKickChatClient? = null

    init {
        instance = this
    }

    private fun startChat(onMessage: (ChatMessage) -> Unit) {
        val broadcasterId = KickSession.userId
        if (broadcasterId.isNullOrEmpty()) {
            println("[KickPlatform] Missing broadcasterId - cannot connect")
            return
        }
        println("[KickPlatform] Polling backend for broadcaster=$broadcasterId")
        chatClient = RemoteKickChatClient(context, broadcasterId, onMessage)
        chatClient?.connect()
    }

    private fun stopChat() {
        chatClient?.disconnect()
        println("[KickPlatform] Disconnected from Kick")
    }

    companion object {
        lateinit var instance: KickPlatform
            private set
    }
}
