package com.jurianoff.irlmate.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

class KickLoginActivity : ComponentActivity() {

    private val loginUrl = "https://ah2d6m1qy4.execute-api.eu-central-1.amazonaws.com/auth/kick/start"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true // niezbędne dla logowania Kick
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false

                    // 🔐 Przechwytujemy redirect_uri typu irlmate://...
                    if (url.startsWith("irlmate://auth/kick/callback")) {
                        val intent = Intent(
                            this@KickLoginActivity,
                            KickAuthRedirectActivity::class.java
                        ).apply {
                            data = request.url
                        }
                        startActivity(intent)
                        finish()
                        return true
                    }

                    // ❗ Blokujemy przekierowanie do przeglądarki zewnętrznej
                    if (url.startsWith("http") || url.startsWith("https")) {
                        return false // otwieraj dalej w WebView
                    }

                    // 👇 Zablokuj inne nietypowe schematy (np. mailto:, tel:)
                    return true
                }
            }
            loadUrl(loginUrl)
        }

        setContentView(webView)
    }
}
