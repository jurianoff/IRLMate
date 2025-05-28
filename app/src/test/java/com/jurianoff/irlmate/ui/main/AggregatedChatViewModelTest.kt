package com.jurianoff.irlmate.ui.main

import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.platform.StreamStatus
import com.jurianoff.irlmate.ui.chat.ChatViewModelBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeChatViewModel(initialMessages: List<ChatMessage>) : ChatViewModelBase {
    override val messages: StateFlow<List<ChatMessage>> = MutableStateFlow(initialMessages)
    override val streamStatus: StateFlow<StreamStatus?> = MutableStateFlow(null)
    override fun updateConnectionState() {}
    override fun disconnect() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class AggregatedChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeKick: ChatViewModelBase
    private lateinit var fakeTwitch: ChatViewModelBase
    private lateinit var aggregated: AggregatedChatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeKick = FakeChatViewModel(
            listOf(
                ChatMessage(platform = "Kick", user = "kick1", message = "Hej z Kick!", createdAt = 1000),
                ChatMessage(platform = "Kick", user = "kick2", message = "Kolejna wiadomość", createdAt = 3000)
            )
        )
        fakeTwitch = FakeChatViewModel(
            listOf(
                ChatMessage(platform = "Twitch", user = "tw1", message = "Siema z Twitch", createdAt = 2000)
            )
        )
        aggregated = AggregatedChatViewModel(
            kick = fakeKick,
            twitch = fakeTwitch,
            startUpdateLoop = false // ważne: nie uruchamiamy pętli updateLoop w testach!
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `messages should aggregate and sort from both platforms`() = runTest {
        advanceUntilIdle()
        var result: List<ChatMessage>? = null

        val job = launch {
            aggregated.messages.collect {
                if (it.size == 3) {
                    result = it
                    cancel() // kończymy kolekcjonowanie po otrzymaniu oczekiwanej wartości
                }
            }
        }

        advanceUntilIdle()
        job.join()

        requireNotNull(result) { "Nie otrzymano oczekiwanej liczby wiadomości z flow!" }
        assertEquals(3, result!!.size)
        assertEquals("kick1", result!![0].user)   // createdAt = 1000
        assertEquals("tw1", result!![1].user)     // createdAt = 2000
        assertEquals("kick2", result!![2].user)   // createdAt = 3000
    }
}
