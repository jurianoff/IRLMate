package com.example.streamchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.streamchat.ui.theme.StreamChatTheme
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.animateContentSize
import com.example.streamchat.KickStatusChecker
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StreamChatTheme {
                val chatMessages = remember { mutableStateListOf<ChatMessage>() }

                // Łączenie się z czatem w tle
                LaunchedEffect(Unit) {
                    launch(Dispatchers.IO) {
                        println("🔥 Uruchamiam Twitch klienta")
                        val twitchClient = TwitchChatClient(channelName = "jurianoff") {
                            chatMessages.add(it)
                        }
                        twitchClient.connect()
                    }

                    launch(Dispatchers.IO) {
                        println("🔥 Uruchamiam Kick klienta")
                        val kickClient = KickChatClient {
                            chatMessages.add(it)
                        }
                        kickClient.connect()
                    }
                }

                ChatApp(messages = chatMessages)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatApp(messages: List<ChatMessage>) {
    val scope = rememberCoroutineScope()
    var kickStatus by remember { mutableStateOf<KickStreamStatus?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            val status = KickStatusChecker.getStreamStatus()
            kickStatus = status
            delay(15_000)
        }
    }

    var twitchStatus by remember { mutableStateOf<TwitchStreamStatus?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            val status = TwitchStatusChecker.getStreamStatus()
            twitchStatus = status
            delay(15_000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multi-Chat: Twitch & Kick") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            StreamStatusBar(kickStatus = kickStatus, twitchStatus = twitchStatus)
            ChatList(messages = messages, modifier = Modifier.weight(1f))
        }
    }
}



@Composable
fun ChatList(messages: List<ChatMessage>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Automatyczne przewinięcie na koniec przy dodaniu nowej wiadomości
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        itemsIndexed(messages) { index, message ->
            // Dodanie animacji dla każdej wiadomości
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(durationMillis = 300))
            ) {
                // Dodanie animacji rozmiaru
                ChatMessageItem(
                    message = message,
                    modifier = Modifier.animateContentSize() // Animacja zmiany rozmiaru
                )
            }
        }
    }
}

@Composable
fun StreamStatusBar(kickStatus: KickStreamStatus?, twitchStatus: TwitchStreamStatus?) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Kicka
            Icon(
                painter = painterResource(id = R.drawable.ic_kick_logo),
                contentDescription = "Kick",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))

            if (kickStatus == null) {
                Text("Ładowanie statusu Kick...", style = MaterialTheme.typography.bodySmall)
            } else {
                val statusText = if (kickStatus.isLive) "🟢 Online" else "🔴 Offline"
                val viewerText = if (kickStatus.isLive) " • 👥 ${kickStatus.viewers} widzów" else ""

                Text(
                    text = "$statusText$viewerText",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (kickStatus.isLive) Color(0xFF00C853) else Color.Red
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // Przerwa między statusami

            // Status Twitcha
            Icon(
                painter = painterResource(id = R.drawable.ic_twitch_logo),
                contentDescription = "Twitch",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))

            if (twitchStatus == null) {
                Text("Ładowanie statusu Twitch...", style = MaterialTheme.typography.bodySmall)
            } else {
                val statusText = if (twitchStatus.isLive) "🟢 Online" else "🔴 Offline"
                val viewerText = if (twitchStatus.isLive) " • 👥 ${twitchStatus.viewers} widzów" else ""

                Text(
                    text = "$statusText$viewerText",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (twitchStatus.isLive) Color(0xFF9146FF) else Color.Red
                )
            }
        }
    }
}



@Composable
fun ChatMessageItem(message: ChatMessage, modifier: Modifier = Modifier) {
    val backgroundColor = when (message.platform.lowercase()) {
        "twitch" -> Color(0xFF9146FF).copy(alpha = 0.2f)
        "kick" -> Color(0xFF53FC18).copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconRes = when (message.platform.lowercase()) {
        "twitch" -> R.drawable.ic_twitch_logo
        "kick" -> R.drawable.ic_kick_logo
        else -> null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        iconRes?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp, top = 4.dp),
                tint = Color.Unspecified
            )
        }

        Column(
            modifier = Modifier
                .background(backgroundColor, shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
                .weight(1f)
                .animateContentSize() // Animacja zmiany rozmiaru
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.user,
                    color = message.userColor?.let { Color(android.graphics.Color.parseColor(it)) }
                        ?: MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (message.timestamp.isNotEmpty()) {
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
