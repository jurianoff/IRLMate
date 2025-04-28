package com.jurianoff.irlmate.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.R
import com.jurianoff.irlmate.data.model.ChatMessage
import androidx.compose.material3.Icon
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun ChatMessageItem(message: ChatMessage, modifier: Modifier = Modifier) {
    val isLightTheme = !isSystemInDarkTheme()

    val backgroundColor = when (message.platform.lowercase()) {
        "twitch" -> if (isLightTheme) Color(0xFFE3D7FF) else Color(0xFF5B3EFF)
        "kick" -> if (isLightTheme) Color(0xFFD9FFD5) else Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }


    val iconRes = when (message.platform.lowercase()) {
        "twitch" -> R.drawable.ic_twitch_logo
        "kick" -> R.drawable.ic_kick_logo
        else -> null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        iconRes?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp, top = 4.dp),
                tint = Color.Unspecified
            )
        }

        Column(
            modifier = Modifier
                .background(backgroundColor, shape = RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(12.dp)
                .weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.user,
                    color = message.userColor?.let { Color(android.graphics.Color.parseColor(it)) }
                        ?: MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (message.timestamp.isNotEmpty()) {
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
