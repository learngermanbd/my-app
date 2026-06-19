package com.streamapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamapp.ui.theme.*

@Composable
fun BlockedAppScreen(
    message: String,
    downloadUrl: String,
    packageName: String,
    onExit: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.linearGradient(listOf(LiveRed, LiveRed.copy(alpha = 0.6f))),
                            RoundedCornerShape(36.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Security,
                        contentDescription = null,
                        tint = Background,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    "Unauthorized App",
                    color = LiveRed,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    message,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                if (downloadUrl.isNotBlank()) {
                    Button(
                        onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)))
                            } catch (_: Exception) { }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Download Original App", color = Background, fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(onClick = onExit) {
                    Text("Exit App", color = TextSecondary)
                }
            }
        }
    }
}
