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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.R
import com.jurianoff.irlmate.data.model.ChatMessage

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    var appeared by remember(message.id) { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(300, easing = LinearOutSlowInEasing)
    )
    val offsetAnim by animateDpAsState(
        targetValue = if (appeared) 0.dp else 12.dp,
        animationSpec = tween(300, easing = LinearOutSlowInEasing)
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
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}
