// AggregatedChatViewModelFactory.kt
package com.jurianoff.irlmate.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jurianoff.irlmate.ui.chat.KickChatViewModel
import com.jurianoff.irlmate.ui.chat.TwitchChatViewModel

class AggregatedChatViewModelFactory(
    private val kick: KickChatViewModel,
    private val twitch: TwitchChatViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AggregatedChatViewModel(kick, twitch) as T
    }
}
