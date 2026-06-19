package com.streamapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamapp.ui.theme.*

@Composable
fun SplashLoadingScreen(
    versionName: String = "",
    serverAddress: String = "192.168.0.191:3000"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, delayMillis = 200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, delayMillis = 400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo glow
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .alpha(glowAlpha)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(GradientStart, GradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "S",
                        color = Background,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // App name
            Text(
                "Sportzfy",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(6.dp))

            // Tagline
            Text(
                "Live Sports",
                color = Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(48.dp))

            // Animated loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .alpha(dotAlpha1)
                        .clip(CircleShape)
                        .background(Primary)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .alpha(dotAlpha2)
                        .clip(CircleShape)
                        .background(Primary)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .alpha(dotAlpha3)
                        .clip(CircleShape)
                        .background(Secondary)
                )
            }
        }

        // Bottom info
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (versionName.isNotBlank()) {
                Text(
                    "v$versionName",
                    color = TextTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Connecting to",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
                Text(
                    serverAddress,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
