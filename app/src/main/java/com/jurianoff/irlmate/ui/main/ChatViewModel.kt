package com.jurianoff.irlmate.ui.main

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.platform.*
import com.jurianoff.irlmate.ui.settings.KickSession
import com.jurianoff.irlmate.ui.settings.TwitchSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()

    private val platforms = listOf(KickPlatform(), TwitchPlatform())

    /** Lista platform spełniających warunki: załadowane, zalogowane i włączone */
    private val activePlatforms get() = platforms.filter {
        when (it) {
            is KickPlatform -> KickSession.isLoaded && it.isLoggedIn && it.isEnabled
            is TwitchPlatform -> TwitchSession.isLoaded && it.isLoggedIn && it.isEnabled
            else -> it.isLoggedIn && it.isEnabled
        }
    }

    private val _streamStatuses = MutableStateFlow<Map<String, StreamStatus>>(emptyMap())
    val streamStatuses: StateFlow<Map<String, StreamStatus>> = _streamStatuses.asStateFlow()

    private var lastActive: Set<String> = emptySet()

    init {
        println("🚀 [ChatViewModel] Uruchomiono ViewModel")
        monitorChatConnections()
        startStatusRefreshing()
    }

    private fun monitorChatConnections() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val current = activePlatforms.map { it.name }.toSet()

                if (current != lastActive) {
                    println("🔄 [ChatViewModel] Zmiana aktywnych platform:")
                    println("    ➤ Aktualne: $current")
                    println("    ➤ Poprzednie: $lastActive")
                }

                val doRozlaczenia = lastActive - current
                val doPolaczenia = current - lastActive

                // Rozłącz czaty
                doRozlaczenia.forEach { name ->
                    println("📴 [ChatViewModel] Rozłączam czat: $name")
                    platforms.find { it.name == name }?.disconnectChat()

                    // Usuń wiadomości z tej platformy
                    val before = messages.size
                    messages.removeAll { it.platform == name }
                    println("🧹 [ChatViewModel] Usunięto ${before - messages.size} wiadomości z platformy $name")
                }

                // Połącz nowe czaty
                doPolaczenia.forEach { name ->
                    println("🔌 [ChatViewModel] Łączę czat: $name")
                    platforms.find { it.name == name }?.let { platform ->
                        platform.connectChat { msg -> addMessage(msg) }
                    }
                }

                lastActive = current
                delay(1000)
            }
        }
    }

    private fun startStatusRefreshing() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val statuses = activePlatforms.mapNotNull { platform ->
                    val status = platform.getStreamStatus()
                    println("📶 [ChatViewModel] Status ${platform.name}: $status")
                    status?.let { platform.name to it }
                }.toMap()

                _streamStatuses.emit(statuses)
                delay(10_000)
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        println("💬 [${message.platform}] ${message.user}: ${message.message}")
        messages.add(message)
        if (messages.size > 100) messages.removeAt(0)
    }
}
