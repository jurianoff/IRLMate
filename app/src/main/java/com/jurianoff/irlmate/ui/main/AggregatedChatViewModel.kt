package com.jurianoff.irlmate.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.platform.StreamStatus
import com.jurianoff.irlmate.ui.chat.KickChatViewModel
import com.jurianoff.irlmate.ui.chat.TwitchChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AggregatedChatViewModel(
    private val kick: KickChatViewModel,
    private val twitch: TwitchChatViewModel
) : ViewModel() {

    init {
        println("🚀 [AggregatedChatVM] Uruchomiono ViewModel")
        updateLoop()
    }

    // 🔁 Co sekundę sprawdzaj stan platform i reaguj
    private fun updateLoop() {
        viewModelScope.launch {
            while (true) {
                kick.updateConnectionState()
                twitch.updateConnectionState()
                delay(1000)
            }
        }
    }

    // 💬 Wszystkie wiadomości z aktywnych platform
    val messages: StateFlow<List<ChatMessage>> = combine(
        kick.messages,
        twitch.messages
    ) { kickMessages, twitchMessages ->
        (kickMessages + twitchMessages)
            .sortedBy { it.createdAt } // ✅ sortowanie po czasie rzeczywistym
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 📡 Wszystkie statusy z aktywnych platform
    val streamStatuses: StateFlow<Map<String, StreamStatus>> = combine(
        kick.streamStatus,
        twitch.streamStatus
    ) { kickStatus, twitchStatus ->
        buildMap {
            kickStatus?.let { put("Kick", it) }
            twitchStatus?.let { put("Twitch", it) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // ⛔ Awaryjne rozłączenie (np. onCleared)
    fun disconnectAll() {
        kick.disconnect()
        twitch.disconnect()
    }
}
