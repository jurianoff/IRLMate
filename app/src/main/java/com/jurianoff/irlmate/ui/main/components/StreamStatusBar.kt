package com.jurianoff.irlmate.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.R
import com.jurianoff.irlmate.data.kick.KickStreamStatus
import com.jurianoff.irlmate.data.twitch.TwitchStreamStatus

@Composable
fun StreamStatusBar(
    kickStatus: KickStreamStatus?,
    twitchStatus: TwitchStreamStatus?,
    vertical: Boolean = false
) {
    val modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val showVertical = vertical || isLandscape   // w poziomie → kolumna

    if (showVertical) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusBadge(
                iconRes = R.drawable.ic_kick_logo,
                isLive   = kickStatus?.isLive,
                viewers  = kickStatus?.viewers,
                platformColor = Color(0xFF53FC18)
            )
            StatusBadge(
                iconRes = R.drawable.ic_twitch_logo,
                isLive   = twitchStatus?.isLive,
                viewers  = twitchStatus?.viewers,
                platformColor = Color(0xFF9146FF)
            )
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusBadge(
                iconRes = R.drawable.ic_kick_logo,
                isLive   = kickStatus?.isLive,
                viewers  = kickStatus?.viewers,
                platformColor = Color(0xFF53FC18)
            )
            StatusBadge(
                iconRes = R.drawable.ic_twitch_logo,
                isLive   = twitchStatus?.isLive,
                viewers  = twitchStatus?.viewers,
                platformColor = Color(0xFF9146FF)
            )
        }
    }
}
