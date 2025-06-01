package com.jurianoff.irlmate.data.model

sealed class MessagePart {
    data class Text(val text: String) : MessagePart()
    data class Emote(val url: String, val alt: String = "", val fallbackUrl: String? = null) : MessagePart()
}
