package com.jurianoff.irlmate.ui.main.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.jurianoff.irlmate.R
import com.jurianoff.irlmate.data.model.ChatMessage
import com.jurianoff.irlmate.data.model.MessagePart

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    var appeared by remember(message.id) { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(300, easing = LinearOutSlowInEasing), label = ""
    )
    val offsetAnim by animateDpAsState(
        targetValue = if (appeared) 0.dp else 12.dp,
        animationSpec = tween(300, easing = LinearOutSlowInEasing), label = ""
    )

    LaunchedEffect(Unit) { appeared = true }

    MessageRow(
        message = message,
        modifier = modifier
            .alpha(alphaAnim)
            .padding(top = offsetAnim)
    )
}

@Composable
private fun MessageRow(message: ChatMessage, modifier: Modifier) {
    val backgroundColor = when (message.platform.lowercase()) {
        "twitch" -> Color(0xFF9146FF)
        "kick"   -> Color(0xFF4CAF50)
        else     -> MaterialTheme.colorScheme.surfaceVariant
    }
    val iconRes = when (message.platform.lowercase()) {
        "twitch" -> R.drawable.ic_twitch_logo
        "kick"   -> R.drawable.ic_kick_logo
        else     -> null
    }
    val textColor = Color.White

    // >>>> ImageLoader dla animowanych GIF/WebP
    val context = LocalContext.current
    val animatedImageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(true)
            .build()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        iconRes?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp, top = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(12.dp)
                .weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.user,
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(2.dp))

            // WYŚWIETLANIE WIADOMOŚCI Z EMOTKAMI I TEKSTEM INLINE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                val parts = message.parts ?: listOf(MessagePart.Text(message.message))
                parts.forEach { part ->
                    when (part) {
                        is MessagePart.Text -> {
                            if (part.text.isNotEmpty()) {
                                Text(
                                    text = part.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor
                                )
                            }
                        }
                        is MessagePart.Emote -> {
                            EmoteImage(
                                url = part.url,
                                fallbackUrl = part.fallbackUrl,
                                alt = part.alt,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(horizontal = 1.dp),
                                imageLoader = animatedImageLoader
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmoteImage(
    url: String,
    fallbackUrl: String? = null,
    alt: String = "",
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader
) {
    var currentUrl by remember { mutableStateOf(url) }
    AsyncImage(
        model = currentUrl,
        contentDescription = alt,
        modifier = modifier,
        imageLoader = imageLoader,
        onError = {
            if (fallbackUrl != null && currentUrl == url) currentUrl = fallbackUrl
        }
    )
}
