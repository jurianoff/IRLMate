package com.jurianoff.irlmate.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun StatusBadge(
    iconRes: Int,
    isLive: Boolean?,
    viewers: Int?,
    platformColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.DarkGray, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))

        if (isLive == null) {
            Text(
                text = "⏳ Ładowanie...",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            val statusText = if (isLive) "🟢 Online" else "🔴 Offline"
            val viewerText = if (isLive && viewers != null) " • 👥 $viewers" else ""
            val textColor = if (isLive) Color(0xFF00FF00) else Color(0xFFFF5555)

            Text(
                text = "$statusText$viewerText",
                color = textColor,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
