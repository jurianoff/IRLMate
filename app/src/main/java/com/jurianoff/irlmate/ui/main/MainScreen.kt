package com.jurianoff.irlmate.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onSettingsClick: () -> Unit) {
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val uiScope = rememberCoroutineScope()

    // Łączenie z czatami
    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            val twitchClient = com.jurianoff.irlmate.data.twitch.TwitchChatClient(channelName = "jurianoff") { msg ->
                uiScope.launch {
                    chatMessages.add(msg)           // tylko dopisujemy na KONIEC listy
                    if (chatMessages.size > 500) {   // ewentualny limit – usuwamy z KOŃCA
                        chatMessages.removeAt(chatMessages.lastIndex)
                    }
                }
            }
            twitchClient.connect()
        }
        launch(Dispatchers.IO) {
            val kickClient = com.jurianoff.irlmate.data.kick.KickChatClient { msg ->
                uiScope.launch {
                    chatMessages.add(msg)
                    if (chatMessages.size > 100) chatMessages.removeAt(0)
                }
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
