package com.jurianoff.irlmate.ui.main

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.jurianoff.irlmate.R
import com.jurianoff.irlmate.ui.chat.KickChatViewModel
import com.jurianoff.irlmate.ui.chat.TwitchChatViewModel
import com.jurianoff.irlmate.ui.main.components.ChatList
import com.jurianoff.irlmate.ui.main.components.StreamStatusBar

// ---- FACTORY DO KICK ----
class KickChatViewModelFactory(
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return KickChatViewModel(context) as T
    }
}

// ---- FACTORY DO TWITCH ----
class TwitchChatViewModelFactory(
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return TwitchChatViewModel(context) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current

    val kickViewModel: KickChatViewModel = viewModel(
        factory = remember { KickChatViewModelFactory(context) }
    )
    val twitchViewModel: TwitchChatViewModel = viewModel(
        factory = remember { TwitchChatViewModelFactory(context) }
    )
    val aggregatedViewModel: AggregatedChatViewModel = viewModel(
        factory = AggregatedChatViewModelFactory(kickViewModel, twitchViewModel)
    )

    val messages by aggregatedViewModel.messages.collectAsState()
    val streamStatuses by aggregatedViewModel.streamStatuses.collectAsState()

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.irlmate_logo),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier
                                .height(48.dp)
                                .padding(end = 12.dp)
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 1f
                                )
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (streamStatuses.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_active_platforms))
            }
        } else {
            if (isLandscape) {
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
                        Text(
                            text = stringResource(R.string.status_panel_description),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 0.dp, bottom = 0.dp, top = 2.dp)
                        )
                        StreamStatusBar(
                            statuses = streamStatuses.values.toList()
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = stringResource(R.string.chat_panel_description),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 12.dp, bottom = 0.dp, top = 2.dp)
                        )
                        ChatList(
                            messages = messages,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    Text(
                        text = stringResource(R.string.status_panel_description),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp, bottom = 0.dp, top = 2.dp)
                    )
                    StreamStatusBar(
                        statuses = streamStatuses.values.toList()
                    )
                    Text(
                        text = stringResource(R.string.chat_panel_description),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp, bottom = 0.dp, top = 2.dp)
                    )
                    ChatList(
                        messages = messages,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
