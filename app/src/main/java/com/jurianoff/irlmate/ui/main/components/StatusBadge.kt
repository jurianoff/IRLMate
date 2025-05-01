package com.jurianoff.irlmate.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun StatusBadge(
    iconRes: Int,
    isLive: Boolean?,
    viewers: Int?,
    platformColor: Color
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    val textShadow = Shadow(
        color = Color.Black.copy(alpha = 0.4f),
        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
        blurRadius = 1f
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))

        val styleWithShadow = MaterialTheme.typography.bodySmall.copy(shadow = textShadow)

        if (isLive == null) {
            Text(
                text = "⏳ Ładowanie...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = styleWithShadow
            )
        } else {
            val statusText = if (isLive) "🟢 Online" else "🔴 Offline"
            val viewerText = if (isLive && viewers != null) " • 👥 $viewers" else ""
            val textColor = if (isLive) platformColor else MaterialTheme.colorScheme.error

            Text(
                text = "$statusText$viewerText",
                color = textColor,
                style = styleWithShadow
            )
        }
    }
}
