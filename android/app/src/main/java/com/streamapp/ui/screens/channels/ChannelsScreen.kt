package com.streamapp.ui.screens.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamapp.data.local.CachedCategory
import com.streamapp.data.local.CachedChannel
import com.streamapp.ui.components.LoadingState
import com.streamapp.ui.components.OfflineState
import com.streamapp.ui.components.ServerErrorState
import com.streamapp.ui.theme.*
import com.streamapp.ui.viewmodel.ChannelsViewModel
import com.streamapp.util.ConnectivityUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelsScreen(
    categoryId: Int? = null,
    categoryName: String? = null,
    onCategoryClick: ((Int, String) -> Unit)? = null,
    onChannelClick: ((Int, String) -> Unit)? = null,
    onBack: () -> Unit,
    viewModel: ChannelsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(categoryId) {
        if (categoryId != null) {
            viewModel.loadChannels(categoryId)
        } else {
            viewModel.loadCategories()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = categoryName ?: "Categories",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextSecondary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Surface,
                titleContentColor = TextPrimary
            )
        )

        when {
            uiState.isLoading -> {
                LoadingState(message = "Loading...")
            }
            uiState.error != null -> {
                if (ConnectivityUtil.isOnline(context)) {
                    ServerErrorState(
                        message = uiState.error ?: "",
                        onRetry = {
                            if (categoryId != null) viewModel.loadChannels(categoryId)
                            else viewModel.loadCategories()
                        }
                    )
                } else {
                    OfflineState(
                        onRetry = {
                            if (categoryId != null) viewModel.loadChannels(categoryId)
                            else viewModel.loadCategories()
                        }
                    )
                }
            }
            categoryId == null && uiState.categories.isNotEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.categories, key = { it.id }) { category ->
                        GlassCategoryCard(
                            category = category,
                            onClick = { onCategoryClick?.invoke(category.id, category.name) }
                        )
                    }
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
                        Text("No channels yet", color = TextSecondary, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.channels, key = { it.id }) { channel ->
                        GlassChannelCard(
                            channel = channel,
                            onClick = { onChannelClick?.invoke(channel.id, channel.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassCategoryCard(
    category: CachedCategory,
    onClick: (() -> Unit)?
) {
    val iconMap = mapOf(
        "cricket" to Icons.Rounded.SportsCricket,
        "football" to Icons.Rounded.SportsSoccer,
        "sports" to Icons.Rounded.FitnessCenter,
        "bangla" to Icons.Rounded.Language,
        "live-tv" to Icons.Rounded.LiveTv,
        "entertainment" to Icons.Rounded.Movie,
        "news" to Icons.Rounded.Newspaper
    )
    val bgMap = mapOf(
        "cricket" to CardCricket,
        "football" to CardFootball,
        "sports" to CardSports,
        "bangla" to CardLive,
        "live-tv" to CardLive,
        "entertainment" to CardEntertainment,
        "news" to CardDefault
    )

    val cardBg = bgMap[category.slug] ?: CardDefault
    val icon = iconMap[category.slug] ?: Icons.Rounded.Category

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GlassSurface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassWhite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${category.channelCount} channels",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun GlassChannelCard(
    channel: CachedChannel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
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
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Primary.copy(alpha = 0.2f), Secondary.copy(alpha = 0.2f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Tv,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
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
                                style = MaterialTheme.typography.labelSmall,
                                color = LiveRed,
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
                    text = "Tap to watch",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
