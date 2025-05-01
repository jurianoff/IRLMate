package com.jurianoff.irlmate.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.R
import com.jurianoff.irlmate.data.model.ChatMessage

@Composable
fun ChatMessageItem(message: ChatMessage, modifier: Modifier = Modifier) {

    // Stałe kolory tła – zależne od platformy, nie od motywu
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
    val shadowEffect = Shadow(
        color = Color.Black.copy(alpha = 0.6f),
        offset = Offset(1f, 1f),
        blurRadius = 1f
    )

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
                    color = textColor,
                    style = MaterialTheme.typography.titleSmall.copy(shadow = shadowEffect)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (message.timestamp.isNotEmpty()) {
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium.copy(shadow = shadowEffect),
                color = textColor
            )
        }
    }
}
