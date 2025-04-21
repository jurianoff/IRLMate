package com.example.streamchat

data class ChatMessage(
    val platform: String,
    val user: String,
    val message: String,
    val userColor: String? = null, // np. "#DEB2FF"
    val timestamp: String = "" // Domyślnie pusty, jeśli nie podano
)


