package com.jurianoff.irlmate.ui.settings

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class KickSessionTest {

    @Before
    fun setUp() {
        // Upewniamy się, że każda funkcja startuje od "czystego" stanu
        KickSession.accessToken = null
        KickSession.expiresInSeconds = 7200
        KickSession.tokenReceivedAt = 0
        KickSession.accessToken = null
        KickSession.userId = null
        KickSession.username = null
        KickSession.channelId = null
        KickSession.chatroomId = null
    }

    @After
    fun tearDown() {
        // Sprzątanie po testach (ważne przy singletonach!)
        KickSession.accessToken = null
        KickSession.expiresInSeconds = 7200
        KickSession.tokenReceivedAt = 0
        KickSession.accessToken = null
        KickSession.userId = null
        KickSession.username = null
        KickSession.channelId = null
        KickSession.chatroomId = null
    }

    @Test
    fun `token invalid if no accessToken`() {
        KickSession.accessToken = null
        KickSession.expiresInSeconds = 7200
        KickSession.tokenReceivedAt = System.currentTimeMillis()
        Assert.assertFalse(KickSession.isAccessTokenValid())
    }

    @Test
    fun `token invalid if expiresInSeconds is zero`() {
        KickSession.accessToken = "abc"
        KickSession.expiresInSeconds = 0
        KickSession.tokenReceivedAt = System.currentTimeMillis()
        Assert.assertFalse(KickSession.isAccessTokenValid())
    }

    @Test
    fun `token invalid if tokenReceivedAt is zero`() {
        KickSession.accessToken = "abc"
        KickSession.expiresInSeconds = 7200
        KickSession.tokenReceivedAt = 0
        Assert.assertFalse(KickSession.isAccessTokenValid())
    }

    @Test
    fun `token valid when not expired`() {
        KickSession.accessToken = "abc"
        KickSession.expiresInSeconds = 7200
        // Ustawiamy czas na 10 minut temu
        KickSession.tokenReceivedAt = System.currentTimeMillis() - 10 * 60 * 1000
        Assert.assertTrue(KickSession.isAccessTokenValid())
    }

    @Test
    fun `token invalid when expired (after buffer)`() {
        KickSession.accessToken = "abc"
        KickSession.expiresInSeconds = 1
        // Ustawiamy czas na 2 minuty temu, token ważny przez 1 sekundę (w praktyce 60 sekund bufora)
        KickSession.tokenReceivedAt = System.currentTimeMillis() - 120 * 1000
        Assert.assertFalse(KickSession.isAccessTokenValid())
    }

    @Test
    fun `isLoggedIn should return false if accessToken is null`() {
        KickSession.accessToken = null
        KickSession.userId = "123"
        KickSession.username = "user"
        KickSession.channelId = "chan"
        KickSession.chatroomId = "chat"
        Assert.assertFalse(KickSession.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return false if userId is null`() {
        KickSession.accessToken = "token"
        KickSession.userId = null
        KickSession.username = "user"
        KickSession.channelId = "chan"
        KickSession.chatroomId = "chat"
        Assert.assertFalse(KickSession.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return false if username is null`() {
        KickSession.accessToken = "token"
        KickSession.userId = "123"
        KickSession.username = null
        KickSession.channelId = "chan"
        KickSession.chatroomId = "chat"
        Assert.assertFalse(KickSession.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return false if channelId is null`() {
        KickSession.accessToken = "token"
        KickSession.userId = "123"
        KickSession.username = "user"
        KickSession.channelId = null
        KickSession.chatroomId = "chat"
        Assert.assertFalse(KickSession.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return false if chatroomId is null`() {
        KickSession.accessToken = "token"
        KickSession.userId = "123"
        KickSession.username = "user"
        KickSession.channelId = "chan"
        KickSession.chatroomId = null
        Assert.assertFalse(KickSession.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return false if any field is empty string`() {
        KickSession.accessToken = ""
        KickSession.userId = "123"
        KickSession.username = "user"
        KickSession.channelId = "chan"
        KickSession.chatroomId = "chat"
        Assert.assertFalse(KickSession.isLoggedIn())

        KickSession.accessToken = "token"
        KickSession.userId = ""
        Assert.assertFalse(KickSession.isLoggedIn())

        KickSession.userId = "123"
        KickSession.username = ""
        Assert.assertFalse(KickSession.isLoggedIn())

        KickSession.username = "user"
        KickSession.channelId = ""
        Assert.assertFalse(KickSession.isLoggedIn())

        KickSession.channelId = "chan"
        KickSession.chatroomId = ""
        Assert.assertFalse(KickSession.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return true if all fields are set and not empty`() {
        KickSession.accessToken = "token"
        KickSession.userId = "123"
        KickSession.username = "user"
        KickSession.channelId = "chan"
        KickSession.chatroomId = "chat"
        Assert.assertTrue(KickSession.isLoggedIn())
    }
}