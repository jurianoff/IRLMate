package com.jurianoff.irlmate.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurianoff.irlmate.data.kick.KickChatClient
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.twitch.TwitchChatClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val twitchChannel = "jurianoff" // TODO: dynamiczne w przyszłości

    init {
        startConnections()
    }

    private fun startConnections() {
        viewModelScope.launch(Dispatchers.IO) {
            val twitch = TwitchChatClient(twitchChannel) { message ->
                addMessage(message)
            }
            twitch.connect()
        }

        viewModelScope.launch(Dispatchers.IO) {
            val kick = KickChatClient { message ->
                addMessage(message)
            }
            kick.connect()
        }
    }

    private fun addMessage(message: ChatMessage) {
        _messages.value = (_messages.value + message).takeLast(100)
    }
}
