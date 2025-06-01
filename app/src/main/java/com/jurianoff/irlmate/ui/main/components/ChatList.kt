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

    /* Czy AKTUALNIE jesteśmy na dole */
    val isAtBottom by remember {
        derivedStateOf {
            val layout = listState.layoutInfo
            val last   = layout.visibleItemsInfo.lastOrNull()?.index
            last != null && last == layout.totalItemsCount - 1
        }
    }

    var hasNew   by remember { mutableStateOf(false) }
    var lastSize by remember { mutableStateOf(0) }

    /* Autoscroll po starcie */
    LaunchedEffect(Unit) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex, scrollOffset = -50)
        }
    }

    /* Autoscroll przy dopływie nowych wiadomości */
    LaunchedEffect(messages.size) {
        if (messages.size > lastSize) {

            val layout = listState.layoutInfo
            val visibleLastIndex = layout.visibleItemsInfo.lastOrNull()?.index ?: -1
            /*  byłem „blisko dołu”, jeśli widać 1 z ostatnich 3 starych elementów */
            val wasNearBottom = visibleLastIndex >= lastSize - 3

            if (wasNearBottom) {
                scope.launch {
                    listState.animateScrollToItem(messages.lastIndex, scrollOffset = -50)
                }
            } else {
                hasNew = true
            }
            lastSize = messages.size
        }
    }

    /* Reset znacznika NEW MESSAGES */
    LaunchedEffect(isAtBottom) { if (isAtBottom) hasNew = false }

    /* FAB widoczny, gdy nie widać ≥3 ostatnich wiadomości */
    val showFab by remember {
        derivedStateOf {
            val visibleLastIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val totalCount       = listState.layoutInfo.totalItemsCount
            if (visibleLastIndex == null || totalCount == 0) false
            else (totalCount - 1 - visibleLastIndex) >= 3
        }
    }

    Box(modifier.fillMaxSize()) {

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 0.dp)
        ) {
            itemsIndexed(messages, key = { _, m -> m.id }) { _, m ->
                ChatMessageItem(m)
            }
        }

        if (showFab) {
            Column(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                /* Etykieta NEW MESSAGES */
                AnimatedVisibility(
                    visible = hasNew,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit  = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    Surface(
                        color = Color(0xFF2C2C2C),
                        shape = MaterialTheme.shapes.medium,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            stringResource(R.string.new_messages),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style    = MaterialTheme.typography.labelMedium,
                            color    = Color(0xFFFF9800)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                /* FAB przewijający do dołu */
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(messages.lastIndex, scrollOffset = -50)
                            hasNew = false
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(end = 12.dp, bottom = 16.dp) // 👈 estetyczne oddalenie od rogu
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
