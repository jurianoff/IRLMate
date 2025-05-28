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
}
