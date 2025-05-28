package com.jurianoff.irlmate.data.twitch

import org.junit.Assert
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
        Assert.assertTrue(status.isLive)
        Assert.assertEquals(321, status.viewers)
    }

    @Test
    fun `offline stream parsed correctly`() {
        val json = """
            {
              "data": []
            }
        """.trimIndent()

        val status = TwitchStatusChecker.parseTwitchStreamStatus(json)
        Assert.assertFalse(status.isLive)
        Assert.assertNull(status.viewers)
    }

    @Test
    fun `malformed json handled safely`() {
        val json = """
            {
              "foo": "bar"
            }
        """.trimIndent()

        val status = TwitchStatusChecker.parseTwitchStreamStatus(json)
        Assert.assertFalse(status.isLive)
        Assert.assertNull(status.viewers)
    }
}