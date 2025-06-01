package com.jurianoff.irlmate.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.R
import com.jurianoff.irlmate.data.platform.StreamStatus
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun StreamStatusBar(
    statuses: List<StreamStatus>
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .background(backgroundColor, shape = RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        FlowRow(
            mainAxisSpacing = 12.dp,
            crossAxisSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            statuses.forEach { status ->
                val icon = when (status) {
                    is StreamStatus.Kick -> R.drawable.ic_kick_logo
                    is StreamStatus.Twitch -> R.drawable.ic_twitch_logo
                }
                val isLive = when (status) {
                    is StreamStatus.Kick -> status.data.isLive
                    is StreamStatus.Twitch -> status.data.isLive
                }
                val viewers = when (status) {
                    is StreamStatus.Kick -> status.data.viewers
                    is StreamStatus.Twitch -> status.data.viewers
                }

                StatusBadge(
                    iconRes = icon,
                    isLive = isLive,
                    viewers = viewers,
                    platformColor = Color.Unspecified,
                    modifier = Modifier
                        .height(40.dp)
                        .wrapContentWidth()
                        .defaultMinSize(minWidth = 156.dp)
                )
            }
        }
    }
}
