package com.jurianoff.irlmate.data.platform

import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.kick.KickStreamStatus
import com.jurianoff.irlmate.data.twitch.TwitchStreamStatus

sealed class StreamingPlatform(
    val name: String,
    private val isLoggedInProvider: () -> Boolean,
    private val isEnabledProvider: () -> Boolean,
    val getStreamStatus: suspend () -> StreamStatus?,
    val connectChat: suspend (onMessage: (ChatMessage) -> Unit) -> Unit,
    val disconnectChat: () -> Unit = {}
) {
    val isLoggedIn: Boolean get() = isLoggedInProvider()
    val isEnabled: Boolean get() = isEnabledProvider()
}

sealed class StreamStatus {
    data class Kick(val data: KickStreamStatus) : StreamStatus()
    data class Twitch(val data: TwitchStreamStatus) : StreamStatus()
}
