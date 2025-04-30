package com.jurianoff.irlmate.ui.main.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.data.model.ChatMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatList(
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope     = rememberCoroutineScope()

    /*  jesteśmy na dole, gdy ostatni widoczny indeks jest
        co najwyżej 1 pozycję przed końcem listy               */
    val isAtBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisible != null && lastVisible >= messages.lastIndex - 1
        }
    }

    /*  Auto-scroll po dopisaniu nowej wiadomości  */
    LaunchedEffect(messages.size) {
        if (isAtBottom) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        itemsIndexed(
            items = messages,
            key = { _, msg -> msg.id }   // stały klucz = brak „gubienia” wierszy
        ) { _, message ->
            ChatMessageItem(
                message = message,
                modifier = Modifier.animateItem()
            )
        }
    }
}
