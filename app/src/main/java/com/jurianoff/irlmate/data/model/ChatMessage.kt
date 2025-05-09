package com.jurianoff.irlmate.data.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val platform: String,
    val user: String,
    val message: String,
    val userColor: String? = null,
    val createdAt: Long = System.currentTimeMillis(), // używane do sortowania
    val timestamp: String = "" // tylko do wyświetlania (np. "13:17")
)
