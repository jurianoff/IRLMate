package com.jurianoff.irlmate.data.kick

import com.jurianoff.irlmate.data.model.ChatMessage
import org.junit.Assert.*
import org.junit.Test

class PusherKickChatClientTest {

    @Test
    fun `should parse kick chat event json to ChatMessage`() {
        val json = """
            {
              "id": "48b77917-9cc6-4fd3-9a9c-e62dd89a95e2",
              "chatroom_id": 259821,
              "content": "I SAW THAT :) :) :)",
              "type": "message",
              "created_at": 1677379978,
              "sender": {
                "id": 4063326,
                "username": "kickuser",
                "slug": "kickuser",
                "identity": {
                  "color": "#72ACED",
                  "badges": [
                    { "type": "broadcaster", "text": "Broadcaster" }
                  ]
                }
              }
            }
        """.trimIndent()

        val result: ChatMessage = PusherKickChatClient.parseKickChatEvent(json)

        assertEquals("Kick", result.platform)
        assertEquals("kickuser", result.user)
        assertEquals("I SAW THAT :) :) :)", result.message)
        // timestamp i createdAt nie są tu sprawdzane, bo zależą od bieżącego czasu
        assertNull(result.userColor)
    }
}
