package com.jurianoff.irlmate.data.twitch

import org.junit.Assert.*
import org.junit.Test

class TwitchStatusCheckerTest {

    @Test
    fun `online stream parsed correctly`() {
        val json = """
            {
              "data": [
                {
                  "id": "12345678",
                  "user_id": "987654321",
                  "viewer_count": 321,
                  "type": "live"
                }
              ]
            }
        """.trimIndent()

        val status = TwitchStatusChecker.parseTwitchStreamStatus(json)
        assertTrue(status.isLive)
        assertEquals(321, status.viewers)
    }

    @Test
    fun `offline stream parsed correctly`() {
        val json = """
            {
              "data": []
            }
        """.trimIndent()

        val status = TwitchStatusChecker.parseTwitchStreamStatus(json)
        assertFalse(status.isLive)
        assertNull(status.viewers)
    }

    @Test
    fun `malformed json handled safely`() {
        val json = """
            {
              "foo": "bar"
            }
        """.trimIndent()

        val status = TwitchStatusChecker.parseTwitchStreamStatus(json)
        assertFalse(status.isLive)
        assertNull(status.viewers)
    }

    @Test
    fun `empty json handled safely`() {
        val json = ""
        val status = TwitchStatusChecker.parseTwitchStreamStatus(json)
        assertFalse(status.isLive)
        assertNull(status.viewers)
    }

    @Test
    fun `broken json is handled safely`() {
        val json = "{ data: [ { viewer_count: 123 " // brak nawiasów
        val status = TwitchStatusChecker.parseTwitchStreamStatus(json)
        assertFalse(status.isLive)
        assertNull(status.viewers)
    }

    @Test
    fun `stream data missing viewer count`() {
        val json = """
            {
              "data": [
                {
                  "id": "9999",
                  "user_id": "8888",
                  "type": "live"
                }
              ]
            }
        """.trimIndent()

        val status = TwitchStatusChecker.parseTwitchStreamStatus(json)
        assertTrue(status.isLive)
        assertEquals(0, status.viewers) // albo assertNull, zależnie od Twojej obsługi
    }
}
