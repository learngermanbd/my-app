package com.streamapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamapp.ui.theme.*

@Composable
fun OfflineState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Rounded.WifiOff,
                contentDescription = null,
                tint = Warning,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "No internet connection",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Your device appears to be offline.\nCheck Wi-Fi or mobile data.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Retry", color = Background, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ServerErrorState(
    message: String = "",
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Server unreachable",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (message.isNotBlank()) message
                else "Could not reach the server.\nCheck that the server is running.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Retry", color = Background, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun LoadingState(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Primary)
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
