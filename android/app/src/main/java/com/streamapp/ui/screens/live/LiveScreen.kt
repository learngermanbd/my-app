package com.streamapp.ui.screens.live

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.streamapp.data.api.LiveEvent
import com.streamapp.data.local.CachedChannel
import com.streamapp.ui.components.LoadingState
import com.streamapp.ui.components.OfflineState
import com.streamapp.ui.components.ServerErrorState
import com.streamapp.ui.theme.*
import com.streamapp.ui.viewmodel.LiveViewModel
import com.streamapp.util.ConnectivityUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScreen(
    onChannelClick: (Int, String) -> Unit,
    onBack: () -> Unit,
    viewModel: LiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadLiveData()
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(LiveRed)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Live Now", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextSecondary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
        )

        when {
            uiState.isLoading -> {
                LoadingState(message = "Loading live events...")
            }
            uiState.error != null -> {
                if (ConnectivityUtil.isOnline(context)) {
                    ServerErrorState(
                        message = uiState.error ?: "",
                        onRetry = { viewModel.loadLiveData() }
                    )
                } else {
                    OfflineState(
                        onRetry = { viewModel.loadLiveData() }
                    )
                }
            }
            uiState.events.isEmpty() && uiState.channels.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.LiveTv,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No live events right now",
                            color = TextSecondary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Check back when matches are live",
                            color = TextTertiary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (uiState.events.isNotEmpty()) {
                        item {
                            Text(
                                "Events",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(uiState.events, key = { "evt_${it.id}" }) { event ->
                            LiveEventCard(
                                event = event,
                                onClick = {
                                    event.channelId?.let { cid ->
                                        onChannelClick(cid, event.title)
                                    }
                                }
                            )
                        }
                        if (uiState.channels.isNotEmpty()) {
                            item { Spacer(Modifier.height(4.dp)) }
                        }
                    }

                    if (uiState.channels.isNotEmpty()) {
                        item {
                            Text(
                                "Channels",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(uiState.channels, key = { "ch_${it.id}" }) { channel ->
                            LiveChannelCard(
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
private fun LiveEventCard(
    event: LiveEvent,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Primary.copy(alpha = 0.2f), Secondary.copy(alpha = 0.1f))
                )
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Primary.copy(alpha = 0.3f), Primary.copy(alpha = 0.1f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.SportsEsports,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (event.isLive == 1) LiveRed else Warning)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (event.isLive == 1) "LIVE" else "Upcoming",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (event.isLive == 1) LiveRed else Warning,
                        fontWeight = FontWeight.Medium
                    )
                    if (event.channelName != null) {
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = event.channelName!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            }

            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = Primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun LiveChannelCard(
    channel: CachedChannel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(LiveRed.copy(alpha = 0.25f), LiveRed.copy(alpha = 0.1f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Tv,
                    contentDescription = null,
                    tint = LiveRed,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(LiveRed)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Live",
                        style = MaterialTheme.typography.bodySmall,
                        color = LiveRed,
                        fontWeight = FontWeight.Medium
                    )
                    if (channel.categoryName != null) {
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = channel.categoryName!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            }

            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = LiveRed,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
