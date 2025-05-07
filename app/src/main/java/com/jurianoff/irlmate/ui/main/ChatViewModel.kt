package com.jurianoff.irlmate.ui.main

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.platform.KickPlatform
import com.jurianoff.irlmate.data.platform.StreamingPlatform
import com.jurianoff.irlmate.data.platform.TwitchPlatform
import com.jurianoff.irlmate.ui.settings.KickSession
import com.jurianoff.irlmate.ui.settings.TwitchSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()

    init {
        startConnections()
    }

    private fun startConnections() {
        val platforms = buildList {
            if (KickSession.isLoaded && KickSession.isLoggedIn() && KickSession.showChatAndStatus) {
                add(KickPlatform())
            }
            if (TwitchSession.isLoaded && TwitchSession.isLoggedIn() && TwitchSession.showChatAndStatus) {
                add(TwitchPlatform())
            }
        }

        println("🔍 [ChatViewModel] Aktywne platformy: ${platforms.map { it.name }}")

        for (platform in platforms) {
            viewModelScope.launch(Dispatchers.IO) {
                platform.connectChat { message ->
                    addMessage(message)
                }
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        if (messages.size > 100) {
            messages.removeAt(0)
        }
    }
}
