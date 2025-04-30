package com.jurianoff.irlmate.ui.main

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.R
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

    /*────────────── 1. kolekcjonowanie wiadomości ──────────────*/
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            com.jurianoff.irlmate.data.twitch.TwitchChatClient("jurianoff") {
                chatMessages.add(it)
                if (chatMessages.size > 100) chatMessages.removeAt(0)
            }.connect()
        }
        launch(Dispatchers.IO) {
            com.jurianoff.irlmate.data.kick.KickChatClient {
                chatMessages.add(it)
                if (chatMessages.size > 100) chatMessages.removeAt(0)
            }.connect()
        }
    }

    /*────────────── 2. statusy streamów ──────────────*/
    var kickStatus by remember { mutableStateOf<KickStreamStatus?>(null) }
    var twitchStatus by remember { mutableStateOf<TwitchStreamStatus?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            kickStatus = KickStatusChecker.getStreamStatus()
            twitchStatus = TwitchStatusChecker.getStreamStatus()
            delay(10_000)
        }
    }

    /*────────────── 3. układ responsywny ──────────────*/
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {

            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor          = MaterialTheme.colorScheme.surface,          // działa w obu motywach
                    scrolledContainerColor  = MaterialTheme.colorScheme.surfaceVariant,   // lekki cień przy scrollu
                    titleContentColor       = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor  = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.irlmate_logo),
                            contentDescription = "IRLMate logo",
                            modifier = Modifier
                                .height(48.dp)
                                .padding(end = 12.dp)
                        )
                        Text("IRLMate")
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Ustawienia")
                    }
                }
            )


        }
    ) { padding ->
        if (isLandscape) {
            /*──────── layout w poziomie ────────*/
            Row(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    /* logo przeniesione do TopAppBar – tutaj tylko statusy */
                    StreamStatusBar(
                        kickStatus = kickStatus,
                        twitchStatus = twitchStatus,
                        vertical = true
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
            /*──────── layout w pionie ────────*/
            Column(
                modifier = Modifier
                    .padding(padding)
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
