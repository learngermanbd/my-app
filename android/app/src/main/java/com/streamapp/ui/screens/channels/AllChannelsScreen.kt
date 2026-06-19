package com.streamapp.ui.screens.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamapp.data.local.CachedChannel
import com.streamapp.ui.components.LoadingState
import com.streamapp.ui.components.OfflineState
import com.streamapp.ui.components.ServerErrorState
import com.streamapp.ui.theme.*
import com.streamapp.ui.viewmodel.AllChannelsViewModel
import com.streamapp.util.ConnectivityUtil

@Composable
fun AllChannelsScreen(
    onChannelClick: (Int, String) -> Unit,
    viewModel: AllChannelsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Primary)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "All Channels",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "${uiState.channels.size} channels available",
            color = TextTertiary,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))

        when {
            uiState.isLoading -> {
                LoadingState(message = "Loading channels...")
            }
            uiState.error != null -> {
                if (ConnectivityUtil.isOnline(context)) {
                    ServerErrorState(
                        message = uiState.error ?: "",
                        onRetry = { viewModel.loadAllChannels() }
                    )
                } else {
                    OfflineState(
                        onRetry = { viewModel.loadAllChannels() }
                    )
                }
            }
            uiState.channels.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.LiveTv,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No channels available", color = TextSecondary, fontSize = 16.sp)
                    }
                }
            }
            else -> {
                // Group channels by category
                val grouped = uiState.channels.groupBy { it.categoryName ?: "Other" }
                val sortedKeys = grouped.keys.sorted()

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    sortedKeys.forEach { categoryName ->
                        val channels = grouped[categoryName] ?: return@forEach
                        item {
                            Text(
                                text = categoryName,
                                color = Primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(channels, key = { it.id }) { channel ->
                            ChannelListItem(
                                channel = channel,
                                onClick = { onChannelClick(channel.id, channel.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelListItem(
    channel: CachedChannel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(GlassSurface)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel logo placeholder with gradient
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Primary.copy(alpha = 0.2f), Secondary.copy(alpha = 0.2f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!channel.logo.isNullOrEmpty()) {
                    // Would use AsyncImage for real logo, fallback to icon
                    Icon(
                        Icons.Rounded.Tv,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        Icons.Rounded.Tv,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (channel.isLive == 1) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = LiveRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = " LIVE ",
                                color = LiveRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (channel.featured == 1) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Featured",
                            tint = Warning,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = channel.categoryName ?: "",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
