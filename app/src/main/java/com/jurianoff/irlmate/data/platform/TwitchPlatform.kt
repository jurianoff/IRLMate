package com.jurianoff.irlmate.data.platform

import com.jurianoff.irlmate.data.twitch.TwitchChatClient
import com.jurianoff.irlmate.data.twitch.TwitchStatusChecker
import com.jurianoff.irlmate.ui.settings.TwitchSession

class TwitchPlatform : StreamingPlatform(
    name = "Twitch",
    isLoggedIn = TwitchSession.isLoggedIn(),
    isEnabled = TwitchSession.showChatAndStatus,
    getStreamStatus = {
        TwitchSession.username?.let { username ->
            TwitchStatusChecker.getStreamStatus(username)?.let { status ->
                StreamStatus.Twitch(status)
            }
        }
    },
    connectChat = { onMessage ->
        TwitchSession.username?.let { username ->
            val client = TwitchChatClient(username, onMessage)
            client.connect()
        }
    }
)
