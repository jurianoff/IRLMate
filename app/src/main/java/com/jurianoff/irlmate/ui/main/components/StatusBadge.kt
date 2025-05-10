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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.R
import androidx.compose.ui.geometry.Offset

@Composable
fun StatusBadge(
    iconRes: Int,
    isLive: Boolean?,
    viewers: Int?,
    platformColor: Color,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh

    val textColor = when (isLive) {
        true -> MaterialTheme.colorScheme.onSurface
        false -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val textShadow = Shadow(
        color = Color.Black.copy(alpha = 0.3f),
        offset = Offset(1f, 1f),
        blurRadius = 0.8f
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .shadow(2.dp, shape = RoundedCornerShape(16.dp))
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))

        val statusText = when (isLive) {
            null -> stringResource(R.string.status_loading)
            true -> stringResource(R.string.status_online)
            false -> stringResource(R.string.status_offline)
        }

        val viewerText = if (isLive == true && viewers != null) {
            " · " + stringResource(R.string.viewers, viewers)
        } else ""

        Text(
            text = statusText + viewerText,
            color = textColor,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                shadow = textShadow
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

