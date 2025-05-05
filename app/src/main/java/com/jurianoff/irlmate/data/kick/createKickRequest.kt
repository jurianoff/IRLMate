package com.jurianoff.irlmate.data.kick

import okhttp3.Request
import com.jurianoff.irlmate.ui.settings.KickSession

/**
 * Tworzy żądanie do API Kick dodając nagłówek Authorization: Bearer <token>,
 * jeżeli użytkownik jest zalogowany i token jest w pamięci.
 */
fun createKickRequest(url: String): Request {
    val token = KickSession.accessToken   // może być null
    return Request.Builder()
        .url(url)
        .addHeader("Accept", "application/json")
        .apply {
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }
        }
        .build()
}
