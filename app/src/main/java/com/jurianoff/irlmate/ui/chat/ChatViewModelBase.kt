package com.jurianoff.irlmate.ui.chat

import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.platform.StreamStatus
import kotlinx.coroutines.flow.StateFlow

interface ChatViewModelBase {
    val messages: StateFlow<List<ChatMessage>>
    val streamStatus: StateFlow<StreamStatus?>
    fun updateConnectionState()
    fun disconnect()
}
