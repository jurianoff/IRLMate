package com.jurianoff.irlmate.ui.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jurianoff.irlmate.R

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0D0D0D), // ciemny dolny róg
                        Color(0xFF181818),
                        Color(0xFF222222),
                        Color(0xFF2C2C2C)  // jaśniejszy górny róg
                    ),
                    start = Offset.Infinite,             // bottom-end
                    end = Offset.Zero                    // top-start
                )
            )
            .padding(24.dp)
    ) {
        // Centrum: logo + nazwa + ładowanie
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.irlmate_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = Color.White)
        }

        // Lewy dolny róg: EARLY ACCESS
        Text(
            text = "early access",
            fontSize = 12.sp,
            color = Color.LightGray,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )

        // Prawy dolny róg: logo kanału + podpis
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_channel_logo),
                contentDescription = "channel logo",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "by jurianoff",
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
    }
}
