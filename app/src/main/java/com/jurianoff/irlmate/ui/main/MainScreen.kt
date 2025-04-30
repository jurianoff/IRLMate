package com.jurianoff.irlmate.ui.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.data.kick.KickStatusChecker
import com.jurianoff.irlmate.data.kick.KickStreamStatus
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.twitch.TwitchStatusChecker
import com.jurianoff.irlmate.data.twitch.TwitchStreamStatus
import com.jurianoff.irlmate.ui.main.components.ChatList
import com.jurianoff.irlmate.ui.main.components.StreamStatusBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onSettingsClick: () -> Unit) {
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }

    // Połączenia do czatu
    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            val twitchClient = com.jurianoff.irlmate.data.twitch.TwitchChatClient(channelName = "jurianoff") {
                chatMessages.add(it)
                if (chatMessages.size > 100) chatMessages.removeAt(0)
            }
            twitchClient.connect()
        }

        launch(Dispatchers.IO) {
            val kickClient = com.jurianoff.irlmate.data.kick.KickChatClient {
                chatMessages.add(it)
                if (chatMessages.size > 100) chatMessages.removeAt(0)
            }
            kickClient.connect()
        }
    }

    var kickStatus by remember { mutableStateOf<KickStreamStatus?>(null) }
    var twitchStatus by remember { mutableStateOf<TwitchStreamStatus?>(null) }

    // Odświeżanie co 10 sekund
    LaunchedEffect(Unit) {
        while (true) {
            kickStatus = KickStatusChecker.getStreamStatus()
            twitchStatus = TwitchStatusChecker.getStreamStatus()
            delay(10_000)
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IRLMate") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Ustawienia")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    StreamStatusBar(
                        kickStatus = kickStatus,
                        twitchStatus = twitchStatus,
                        vertical = true // będzie obsłużone w StreamStatusBar
                    )
                }

                ChatList(
                    messages = chatMessages,
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                StreamStatusBar(
                    kickStatus = kickStatus,
                    twitchStatus = twitchStatus
                )
                ChatList(
                    messages = chatMessages,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
