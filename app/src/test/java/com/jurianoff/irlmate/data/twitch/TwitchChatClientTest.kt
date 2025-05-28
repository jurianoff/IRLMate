package com.jurianoff.irlmate.data.twitch

import com.jurianoff.irlmate.data.model.ChatMessage
import org.junit.Assert.*
import org.junit.Test

class TwitchChatClientTest {

    @Test
    fun `should parse twitch IRC message to ChatMessage`() {
        val ircLine = ":testuser!testuser@testuser.tmi.twitch.tv PRIVMSG #example :Hello Twitch world!"

        val result: ChatMessage? = TwitchChatClient.parseTwitchIrcMessage(ircLine)

        assertNotNull(result)
        result!!
        assertEquals("Twitch", result.platform)
        assertEquals("testuser", result.user)
        assertEquals("Hello Twitch world!", result.message)
        assertNull(result.userColor)
        // timestamp i createdAt nie są sprawdzane (zależą od czasu)
    }

    @Test
    fun `should return null for non-message IRC line`() {
        val ircLine = ":tmi.twitch.tv NOTICE * :Improperly formatted auth"
        val result = TwitchChatClient.parseTwitchIrcMessage(ircLine)
        assertNull(result)
    }
}
