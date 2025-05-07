package com.jurianoff.irlmate.data.platform

import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.kick.KickStreamStatus
import com.jurianoff.irlmate.data.twitch.TwitchStreamStatus

sealed class StreamingPlatform(
    val name: String,
    val isLoggedIn: Boolean,
    val isEnabled: Boolean,
    val getStreamStatus: suspend () -> StreamStatus?,
    val connectChat: suspend (onMessage: (ChatMessage) -> Unit) -> Unit
)

sealed class StreamStatus {
    data class Kick(val data: KickStreamStatus) : StreamStatus()
    data class Twitch(val data: TwitchStreamStatus) : StreamStatus()
}
