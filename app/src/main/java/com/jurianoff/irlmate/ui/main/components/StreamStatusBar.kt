package com.jurianoff.irlmate.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.R
import com.jurianoff.irlmate.data.platform.StreamStatus

@Composable
fun StreamStatusBar(
    statuses: List<StreamStatus>,
    vertical: Boolean = false
) {
    val modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val showVertical = vertical || isLandscape

    if (showVertical) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statuses.forEach { status ->
                when (status) {
                    is StreamStatus.Kick -> StatusBadge(
                        iconRes = R.drawable.ic_kick_logo,
                        isLive = status.data.isLive,
                        viewers = status.data.viewers,
                        platformColor = Color(0xFF53FC18)
                    )
                    is StreamStatus.Twitch -> StatusBadge(
                        iconRes = R.drawable.ic_twitch_logo,
                        isLive = status.data.isLive,
                        viewers = status.data.viewers,
                        platformColor = Color(0xFF9146FF)
                    )
                }
            }
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            statuses.forEach { status ->
                when (status) {
                    is StreamStatus.Kick -> StatusBadge(
                        iconRes = R.drawable.ic_kick_logo,
                        isLive = status.data.isLive,
                        viewers = status.data.viewers,
                        platformColor = Color(0xFF53FC18)
                    )
                    is StreamStatus.Twitch -> StatusBadge(
                        iconRes = R.drawable.ic_twitch_logo,
                        isLive = status.data.isLive,
                        viewers = status.data.viewers,
                        platformColor = Color(0xFF9146FF)
                    )
                }
            }
        }
    }
}
