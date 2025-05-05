package com.jurianoff.irlmate.ui.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jurianoff.irlmate.data.settings.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object ChannelSettings {
    var twitchChannel by mutableStateOf("jurianoff")
    var kickChannel by mutableStateOf("jurianoff")

    var isInitialized by mutableStateOf(false)

    fun loadChannels(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                twitchChannel = SettingsDataStore.readTwitchChannel(context).first()
                kickChannel = SettingsDataStore.readKickChannel(context).first()
            } catch (e: Exception) {
                twitchChannel = "jurianoff"
                kickChannel = "jurianoff"
            } finally {
                isInitialized = true
            }
        }
    }

    fun saveTwitchChannel(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            SettingsDataStore.saveTwitchChannel(context, twitchChannel)
        }
    }

    fun saveKickChannel(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            SettingsDataStore.saveKickChannel(context, kickChannel)
        }
    }
}
