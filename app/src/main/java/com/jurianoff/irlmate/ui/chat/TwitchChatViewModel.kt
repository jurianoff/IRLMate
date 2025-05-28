package com.jurianoff.irlmate.ui.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.platform.StreamStatus
import com.jurianoff.irlmate.data.platform.TwitchPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TwitchChatViewModel(private val context: Context) : ViewModel(), ChatViewModelBase {

    private val platform = TwitchPlatform(context)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _streamStatus = MutableStateFlow<StreamStatus?>(null)
    override val streamStatus: StateFlow<StreamStatus?> = _streamStatus.asStateFlow()

    private var chatJob: Job? = null
    private var statusJob: Job? = null

    private fun formatTimestamp(epochMillis: Long): String {
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return formatter.format(java.util.Date(epochMillis))
    }

    init {
        updateConnectionState()
    }

    override fun updateConnectionState() {
        if (platform.isLoggedIn && platform.isEnabled) {
            connect()
            startStatusUpdates()
        } else {
            disconnect()
        }
    }

    private fun connect() {
        if (chatJob != null) return // już połączony

        println("✅ [TwitchChatVM] Łączenie z czatem Twitch")
        chatJob = viewModelScope.launch(Dispatchers.IO) {
            platform.connectChat { msg ->
                val formatted = msg.copy(timestamp = formatTimestamp(msg.createdAt))
                println("💬 [TwitchChatVM] ${formatted.user}: ${formatted.message} @ ${formatted.timestamp}")
                _messages.update { current -> (current + formatted).takeLast(100) }
            }

        }
    }

    private fun startStatusUpdates() {
        if (statusJob != null) return // już działa

        statusJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val status = platform.getStreamStatus()
                println("📶 [TwitchChatVM] Status: $status")
                _streamStatus.emit(status)
                delay(10_000)
            }
        }
    }

    override fun disconnect() {
        println("📴 [TwitchChatVM] Rozłączanie z Twitch")
        chatJob?.cancel()
        chatJob = null
        statusJob?.cancel()
        statusJob = null
        platform.disconnectChat()
        _messages.value = emptyList()
        _streamStatus.value = null
    }
}
