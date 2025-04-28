package com.jurianoff.irlmate.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.data.kick.KickStreamStatus
import com.jurianoff.irlmate.data.twitch.TwitchStreamStatus
import com.jurianoff.irlmate.R

@Composable
fun StreamStatusBar(kickStatus: KickStreamStatus?, twitchStatus: TwitchStreamStatus?) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatusBadge(
                iconRes = R.drawable.ic_kick_logo,
                isLive = kickStatus?.isLive,
                viewers = kickStatus?.viewers,
                platformColor = Color(0xFF53FC18)
            )

            StatusBadge(
                iconRes = R.drawable.ic_twitch_logo,
                isLive = twitchStatus?.isLive,
                viewers = twitchStatus?.viewers,
                platformColor = Color(0xFF9146FF)
            )
        }
    }
}
