package com.jurianoff.irlmate.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.platform.KickPlatform
import com.jurianoff.irlmate.data.platform.StreamStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class KickChatViewModel : ViewModel() {

    private val platform = KickPlatform()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _streamStatus = MutableStateFlow<StreamStatus?>(null)
    val streamStatus: StateFlow<StreamStatus?> = _streamStatus.asStateFlow()

    private var chatJob: Job? = null
    private var statusJob: Job? = null

    private fun formatTimestamp(epochMillis: Long): String {
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return formatter.format(java.util.Date(epochMillis))
    }

    init {
        updateConnectionState()
    }

    fun updateConnectionState() {
        if (platform.isLoggedIn && platform.isEnabled) {
            connect()
            startStatusUpdates()
        } else {
            disconnect()
        }
    }

    private fun connect() {
        if (chatJob != null) return // już połączony

        println("✅ [KickChatVM] Łączenie z czatem Kick")
        chatJob = viewModelScope.launch(Dispatchers.IO) {
            platform.connectChat { msg ->
                val formatted = msg.copy(timestamp = formatTimestamp(msg.createdAt))
                println("💬 [KickChatVM] ${formatted.user}: ${formatted.message} @ ${formatted.timestamp}")
                _messages.update { current -> (current + formatted).takeLast(100) }
            }

        }
    }

    private fun startStatusUpdates() {
        if (statusJob != null) return

        statusJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val status = platform.getStreamStatus()
                println("📶 [KickChatVM] Status: $status")
                _streamStatus.emit(status)
                delay(10_000)
            }
        }
    }

    fun disconnect() {
        println("📴 [KickChatVM] Rozłączanie z Kick")
        chatJob?.cancel()
        chatJob = null
        statusJob?.cancel()
        statusJob = null
        platform.disconnectChat()
        _messages.value = emptyList()
        _streamStatus.value = null
    }
}
