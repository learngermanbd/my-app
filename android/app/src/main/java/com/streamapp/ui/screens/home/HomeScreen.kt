package com.streamapp.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamapp.data.api.SportsMatch
import com.streamapp.data.local.CachedCategory
import com.streamapp.data.local.CachedChannel
import com.streamapp.ui.components.LoadingState
import com.streamapp.ui.components.OfflineState
import com.streamapp.ui.components.ServerErrorState
import com.streamapp.ui.theme.*
import com.streamapp.ui.util.getCountryFlag
import com.streamapp.ui.util.getTeamCountryCode
import com.streamapp.ui.viewmodel.HomeViewModel
import com.streamapp.util.ConnectivityUtil
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onCategoryClick: (Int, String) -> Unit,
    onChannelClick: (Int, String) -> Unit,
    onViewAllCategories: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        HomeTopBar(onMenuClick = onMenuClick)

        if (uiState.isLoading) {
            LoadingState(message = "Loading...")
        } else if (uiState.error != null) {
            if (ConnectivityUtil.isOnline(context)) {
                ServerErrorState(
                    message = uiState.error ?: "",
                    onRetry = { viewModel.loadHome() }
                )
            } else {
                OfflineState(
                    onRetry = { viewModel.loadHome() }
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Marquee banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Download Official Latest App. Don't get Scammed by Scam websites.",
                            color = TextTertiary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Category selector row
                item {
                    CategoryChipRow(
                        categories = uiState.categories,
                        selectedId = uiState.selectedCategoryId,
                        onCategoryClick = { id -> viewModel.selectCategory(id) },
                        onAllSelected = { viewModel.selectCategory(null) }
                    )
                }

                // Tab row
                item {
                    MatchTabRow(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }

                // Match cards (if matches exist)
                val filteredMatches = when (uiState.selectedTab) {
                    1 -> uiState.matches.filter { it.status == "live" }
                    2 -> uiState.matches.filter { it.status == "upcoming" }
                    else -> uiState.matches
                }

                if (filteredMatches.isNotEmpty()) {
                    items(filteredMatches) { match ->
                        MatchCard(
                            match = match,
                            onClick = { match.channelId?.let { cid -> onChannelClick(cid, match.title) } }
                        )
                    }
                }

                // Fallback: channel list when no matches
                val showChannels = filteredMatches.isEmpty()
                if (showChannels) {
                    val filteredChannels = buildList {
                        val source = when (uiState.selectedTab) {
                            1 -> uiState.liveChannels
                            else -> uiState.allChannels
                        }
                        addAll(
                            if (uiState.selectedCategoryId != null) {
                                source.filter { it.categoryId == uiState.selectedCategoryId }
                            } else {
                                source
                            }
                        )
                    }

                    if (filteredChannels.isNotEmpty()) {
                        items(filteredChannels, key = { it.id }) { channel ->
                            GlassChannelCard(
                                channel = channel,
                                onClick = { onChannelClick(channel.id, channel.name) }
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.VideocamOff, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(12.dp))
                                    Text("No content available", color = TextSecondary, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(onMenuClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(Background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Rounded.Menu, contentDescription = "Menu", tint = TextPrimary, modifier = Modifier.size(24.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("S", style = MaterialTheme.typography.titleLarge, color = Background, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Sportzfy",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Live Sports",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.BookmarkBorder, contentDescription = "Favorites", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.Login, contentDescription = "Login", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryChipRow(
    categories: List<CachedCategory>,
    selectedId: Int?,
    onCategoryClick: (Int) -> Unit,
    onAllSelected: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            LargeCategoryChip(
                name = "All",
                badgeCount = null,
                isSelected = selectedId == null,
                onClick = onAllSelected
            )
        }
        items(categories, key = { it.id }) { category ->
            LargeCategoryChip(
                name = category.name,
                badgeCount = null,
                isSelected = selectedId == category.id,
                onClick = { onCategoryClick(category.id) }
            )
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun LargeCategoryChip(
    name: String,
    badgeCount: Int?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Primary.copy(alpha = 0.15f) else SurfaceVariant)
                    .then(
                        if (isSelected) Modifier.border(2.dp, Primary, CircleShape)
                        else Modifier.border(1.dp, GlassBorder, CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (name.lowercase()) {
                    "cricket" -> Icon(Icons.Rounded.SportsCricket, contentDescription = null, tint = if (isSelected) Primary else TextSecondary, modifier = Modifier.size(32.dp))
                    "football" -> Icon(Icons.Rounded.SportsSoccer, contentDescription = null, tint = if (isSelected) Primary else TextSecondary, modifier = Modifier.size(32.dp))
                    "all" -> Icon(Icons.Rounded.GridView, contentDescription = null, tint = if (isSelected) Primary else TextSecondary, modifier = Modifier.size(32.dp))
                    else -> Icon(Icons.Rounded.Sports, contentDescription = null, tint = if (isSelected) Primary else TextSecondary, modifier = Modifier.size(32.dp))
                }
            }
            if (badgeCount != null && badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(LiveRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$badgeCount",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = name,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun MatchTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Recent", "Live", "Upcoming")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Primary.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onTabSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (index == 1) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Primary else LiveRed),
                        )
                        Spacer(Modifier.width(5.dp))
                    }
                    Text(
                        text = title,
                        color = if (isSelected) Primary else TabInactive,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun MatchCard(
    match: SportsMatch,
    onClick: () -> Unit
) {
    val isLive = match.status == "live"
    val isUpcoming = match.status == "upcoming"
    val titleStr = match.title.orEmpty()
    val leagueIcon = when {
        titleStr.contains("Cricket", ignoreCase = true) -> "🏏"
        titleStr.contains("Football", ignoreCase = true) || titleStr.contains("Soccer", ignoreCase = true) -> "⚽"
        titleStr.contains("Tennis", ignoreCase = true) -> "🎾"
        titleStr.contains("Basketball", ignoreCase = true) -> "🏀"
        else -> "🏆"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(leagueIcon, fontSize = 16.sp)
                    Text(
                        text = titleStr.ifEmpty { "Live Match" },
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = match.startTime?.let {
                            if (isLive) {
                                val parts = it.split("T")
                                if (parts.size > 1) parts[1].take(8) else it
                            } else {
                                formatTime(it)
                            }
                        } ?: if (isLive) "00:00" else if (isUpcoming) "TBD" else "Recent",
                        color = if (isLive) LiveRed else if (isUpcoming) Warning else TextTertiary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLive) {
                        Surface(shape = RoundedCornerShape(4.dp), color = LiveRed) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(TextPrimary))
                                Spacer(Modifier.width(5.dp))
                                Text("LIVE", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TeamBlock(name = match.team1Name.orEmpty().ifEmpty { "Team 1" }, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VS", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                TeamBlock(name = match.team2Name.orEmpty().ifEmpty { "Team 2" }, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(10.dp))

            if (isLive) {
                Box(modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(GlassWhite)) {
                    Box(modifier = Modifier.fillMaxWidth(0.6f).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(LiveRed))
                }
            } else if (isUpcoming) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = Warning, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = match.startTime?.let { formatCountdown(it) } ?: "Starting soon",
                        color = Warning,
                        fontSize = 11.sp
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Visibility, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Match completed", color = TextTertiary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun TeamBlock(name: String, modifier: Modifier = Modifier) {
    val countryCode = getTeamCountryCode(name)
    val flagEmoji = getCountryFlag(countryCode)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp).clip(CircleShape),
            color = SurfaceVariant,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = flagEmoji, fontSize = 22.sp)
            }
        }
        Text(
            text = name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
        )
    }
}

private fun formatTime(timeStr: String): String {
    return try {
        timeStr.substringAfter("T").take(5)
    } catch (_: Exception) { timeStr }
}

private fun formatCountdown(timeStr: String): String {
    if (timeStr.length < 16) return timeStr
    return try { "Starts ${timeStr.take(10)}" } catch (_: Exception) { timeStr }
}

@Composable
private fun GlassChannelCard(
    channel: CachedChannel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
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
