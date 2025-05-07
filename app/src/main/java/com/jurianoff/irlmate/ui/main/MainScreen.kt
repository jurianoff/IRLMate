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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jurianoff.irlmate.R
import com.jurianoff.irlmate.data.platform.KickPlatform
import com.jurianoff.irlmate.data.platform.StreamStatus
import com.jurianoff.irlmate.data.platform.TwitchPlatform
import com.jurianoff.irlmate.ui.main.components.ChatList
import com.jurianoff.irlmate.ui.main.components.StreamStatusBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val messages = viewModel.messages

    // Lista aktywnych platform
    val activePlatforms = remember {
        listOf(
            KickPlatform(),
            TwitchPlatform()
        ).filter { it.isLoggedIn && it.isEnabled }
    }

    // Statusy streamów
    var streamStatuses by remember { mutableStateOf<Map<String, StreamStatus>>(emptyMap()) }

    LaunchedEffect(Unit) {
        while (true) {
            val newStatuses = mutableMapOf<String, StreamStatus>()
            for (platform in activePlatforms) {
                val status = platform.getStreamStatus()
                if (status != null) {
                    newStatuses[platform.name] = status
                }
            }
            streamStatuses = newStatuses
            delay(10_000)
        }
    }

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
        if (activePlatforms.isEmpty()) {
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
                        StreamStatusBar(
                            statuses = streamStatuses.values.toList(),
                            vertical = true
                        )
                    }
                    ChatList(
                        messages = messages,
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight()
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    StreamStatusBar(
                        statuses = streamStatuses.values.toList()
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
