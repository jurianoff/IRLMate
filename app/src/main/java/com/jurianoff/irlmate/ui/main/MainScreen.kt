package com.jurianoff.irlmate.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onSettingsClick: () -> Unit) {
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }

    // Łączenie z czatami
    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            val twitchClient = com.jurianoff.irlmate.data.twitch.TwitchChatClient(channelName = "jurianoff") {
                chatMessages.add(it)
            }
            twitchClient.connect()
        }
        launch(Dispatchers.IO) {
            val kickClient = com.jurianoff.irlmate.data.kick.KickChatClient {
                chatMessages.add(it)
            }
            kickClient.connect()
        }
    }

    var kickStatus by remember { mutableStateOf<KickStreamStatus?>(null) }
    var twitchStatus by remember { mutableStateOf<TwitchStreamStatus?>(null) }

    // Aktualizacja statusów co 15 sekund
    LaunchedEffect(Unit) {
        while (true) {
            kickStatus = KickStatusChecker.getStreamStatus()
            twitchStatus = TwitchStatusChecker.getStreamStatus()
            delay(15_000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IRLMate") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Ustawienia")
                    }
                }
            )
        }
    ) { paddingValues ->
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
