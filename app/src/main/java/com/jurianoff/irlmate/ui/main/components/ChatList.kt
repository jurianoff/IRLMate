package com.jurianoff.irlmate.ui.main.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.R
import com.jurianoff.irlmate.data.model.ChatMessage
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatList(
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val isAtBottom by remember {
        derivedStateOf {
            val layout = listState.layoutInfo
            val lastVisibleIndex = layout.visibleItemsInfo.lastOrNull()?.index
            lastVisibleIndex != null && lastVisibleIndex >= layout.totalItemsCount - 2
        }
    }

    var hasNewMessages by remember { mutableStateOf(false) }
    var lastMessageCount by remember { mutableStateOf(0) }

    // Autoscroll na start jeśli lista niepusta
    LaunchedEffect(Unit) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.scrollToItem(messages.lastIndex)
            }
        }
    }

    // Scroll do nowej wiadomości jeśli jesteśmy na dole
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            if (messages.size > lastMessageCount) {
                if (isAtBottom) {
                    scope.launch {
                        listState.animateScrollToItem(messages.lastIndex)
                    }
                } else {
                    hasNewMessages = true
                }
            }
            lastMessageCount = messages.size
        }
    }

    // Resetuj znacznik nowych wiadomości gdy jesteśmy przy końcu
    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            hasNewMessages = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            itemsIndexed(
                items = messages,
                key = { _, msg -> msg.id }
            ) { _, message ->
                ChatMessageItem(message = message)
            }
        }

        if (messages.isNotEmpty() && !isAtBottom) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    horizontalAlignment = Alignment.End
                ) {
                    AnimatedVisibility(
                        visible = hasNewMessages,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        Surface(
                            color = Color(0xFF2C2C2C),
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 0.dp,
                            shadowElevation = 4.dp,
                            modifier = Modifier.shadow(4.dp, shape = MaterialTheme.shapes.medium)
                        ) {
                            Text(
                                text = stringResource(R.string.new_messages),
                                color = Color(0xFFFF9800),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                listState.animateScrollToItem(messages.lastIndex)
                                hasNewMessages = false
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(end = 12.dp, bottom = 16.dp)
                            .shadow(12.dp, shape = FloatingActionButtonDefaults.shape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = stringResource(R.string.scroll_down)
                        )
                    }
                }
            }
        }
    }
}
