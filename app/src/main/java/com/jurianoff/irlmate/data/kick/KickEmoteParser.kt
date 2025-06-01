package com.jurianoff.irlmate.data.kick

import com.jurianoff.irlmate.data.model.MessagePart

fun parseKickInlineEmotes(message: String): List<MessagePart> {
    val regex = Regex("""\[emote:(\d+):([^\]]+)]""")
    val parts = mutableListOf<MessagePart>()
    var lastEnd = 0

    regex.findAll(message).forEach { match ->
        val start = match.range.first
        val end = match.range.last + 1

        if (start > lastEnd) {
            parts += MessagePart.Text(message.substring(lastEnd, start))
        }
        val emoteId = match.groupValues[1]
        val emoteName = match.groupValues[2]
        val url = "https://files.kick.com/emotes/$emoteId/fullsize"
        parts += MessagePart.Emote(url, "[$emoteName]")
        lastEnd = end
    }
    if (lastEnd < message.length) {
        parts += MessagePart.Text(message.substring(lastEnd))
    }
    return parts
}
