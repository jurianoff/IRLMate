package com.jurianoff.irlmate.ui.settings

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TwitchSessionTest {

    @Before
    fun setUp() {
        TwitchSession.accessToken = null
        TwitchSession.expiresInSeconds = 3600
        TwitchSession.tokenReceivedAt = 0
        TwitchSession.accessToken = null
        TwitchSession.userId = null
        TwitchSession.username = null
    }

    @After
    fun tearDown() {
        TwitchSession.accessToken = null
        TwitchSession.expiresInSeconds = 3600
        TwitchSession.tokenReceivedAt = 0
        TwitchSession.accessToken = null
        TwitchSession.userId = null
        TwitchSession.username = null
    }

    @Test
    fun `token invalid if no accessToken`() {
        TwitchSession.accessToken = null
        TwitchSession.expiresInSeconds = 3600
        TwitchSession.tokenReceivedAt = System.currentTimeMillis()
        Assert.assertFalse(TwitchSession.isAccessTokenValid())
    }

    @Test
    fun `token invalid if expiresInSeconds is zero`() {
        TwitchSession.accessToken = "abc"
        TwitchSession.expiresInSeconds = 0
        TwitchSession.tokenReceivedAt = System.currentTimeMillis()
        Assert.assertFalse(TwitchSession.isAccessTokenValid())
    }

    @Test
    fun `token invalid if tokenReceivedAt is zero`() {
        TwitchSession.accessToken = "abc"
        TwitchSession.expiresInSeconds = 3600
        TwitchSession.tokenReceivedAt = 0
        Assert.assertFalse(TwitchSession.isAccessTokenValid())
    }

    @Test
    fun `token valid when not expired`() {
        TwitchSession.accessToken = "abc"
        TwitchSession.expiresInSeconds = 3600
        // 10 minut temu
        TwitchSession.tokenReceivedAt = System.currentTimeMillis() - 10 * 60 * 1000
        Assert.assertTrue(TwitchSession.isAccessTokenValid())
    }

    @Test
    fun `token invalid when expired (after buffer)`() {
        TwitchSession.accessToken = "abc"
        TwitchSession.expiresInSeconds = 1
        TwitchSession.tokenReceivedAt = System.currentTimeMillis() - 120 * 1000
        Assert.assertFalse(TwitchSession.isAccessTokenValid())
    }
    @Test
    fun `isLoggedIn should return false if accessToken is null`() {
        TwitchSession.accessToken = null
        TwitchSession.userId = "123"
        TwitchSession.username = "user"
        Assert.assertFalse(TwitchSession.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return false if userId is null`() {
        TwitchSession.accessToken = "token"
        TwitchSession.userId = null
        TwitchSession.username = "user"
        Assert.assertFalse(TwitchSession.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return false if username is null`() {
        TwitchSession.accessToken = "token"
        TwitchSession.userId = "123"
        TwitchSession.username = null
        Assert.assertFalse(TwitchSession.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return false if any field is empty string`() {
        TwitchSession.accessToken = ""
        TwitchSession.userId = "123"
        TwitchSession.username = "user"
        Assert.assertFalse(TwitchSession.isLoggedIn())

        TwitchSession.accessToken = "token"
        TwitchSession.userId = ""
        Assert.assertFalse(TwitchSession.isLoggedIn())

        TwitchSession.userId = "123"
        TwitchSession.username = ""
        Assert.assertFalse(TwitchSession.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return true if all fields are set and not empty`() {
        TwitchSession.accessToken = "token"
        TwitchSession.userId = "123"
        TwitchSession.username = "user"
        Assert.assertTrue(TwitchSession.isLoggedIn())
    }

}