package com.jurianoff.irlmate.data.kick

import org.junit.Assert.*
import org.junit.Test

class KickStatusCheckerTest {

    @Test
    fun `online stream parsed correctly`() {
        val json = """
            {
              "livestream": {
                "is_live": true,
                "viewer_count": 842
              }
            }
        """.trimIndent()

        val status = KickStatusChecker.parseKickStreamStatus(json)
        assertTrue(status.isLive)
        assertEquals(842, status.viewers)
    }

    @Test
    fun `offline stream parsed correctly`() {
        val json = """
            {
              "livestream": null
            }
        """.trimIndent()

        val status = KickStatusChecker.parseKickStreamStatus(json)
        assertFalse(status.isLive)
        assertNull(status.viewers)
    }

    @Test
    fun `malformed json handled safely`() {
        val json = """
            {
              "bad": 42
            }
        """.trimIndent()

        val status = KickStatusChecker.parseKickStreamStatus(json)
        assertFalse(status.isLive)
        assertNull(status.viewers)
    }

    @Test
    fun `empty json handled safely`() {
        val json = ""
        val status = KickStatusChecker.parseKickStreamStatus(json)
        assertFalse(status.isLive)
        assertNull(status.viewers)
    }

    @Test
    fun `livestream object missing fields`() {
        val json = """
            {
              "livestream": {
                "is_live": true
              }
            }
        """.trimIndent()

        val status = KickStatusChecker.parseKickStreamStatus(json)
        assertTrue(status.isLive)
        // viewer_count jest brakujący -> default: 0
        assertEquals(0, status.viewers)
    }

    @Test
    fun `viewer_count is null`() {
        val json = """
            {
              "livestream": {
                "is_live": true,
                "viewer_count": null
              }
            }
        """.trimIndent()

        val status = KickStatusChecker.parseKickStreamStatus(json)
        assertTrue(status.isLive)
        assertEquals(0, status.viewers) // zależnie od Twojej obsługi - możesz zmienić na assertNull
    }

    @Test
    fun `broken json is handled safely`() {
        val json = "{ livestream: { is_live: true, viewer_count: 100 " // brakuje nawiasów
        val status = KickStatusChecker.parseKickStreamStatus(json)
        assertFalse(status.isLive)
        assertNull(status.viewers)
    }
}
