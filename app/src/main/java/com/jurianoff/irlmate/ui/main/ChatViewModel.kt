package com.jurianoff.irlmate.ui.main

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurianoff.irlmate.data.kick.KickChatClient
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.twitch.TwitchChatClient
import com.jurianoff.irlmate.ui.settings.KickSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()

    init {
        startConnections()
    }

    private fun startConnections() {
        if (KickSession.isLoggedIn() && KickSession.showChatAndStatus) {
            val kickChannel = KickSession.username ?: return

            viewModelScope.launch(Dispatchers.IO) {
                val kick = KickChatClient(kickChannel) { message ->
                    addMessage(message)
                }
                kick.connect()
            }
        }

        // Twitch (tymczasowo – do zastąpienia później)
        val twitchChannel = "jurianoff"
        viewModelScope.launch(Dispatchers.IO) {
            val twitch = TwitchChatClient(twitchChannel) { message ->
                addMessage(message)
            }
            twitch.connect()
        }
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        if (messages.size > 100) {
            messages.removeAt(0)
        }
    }
}
