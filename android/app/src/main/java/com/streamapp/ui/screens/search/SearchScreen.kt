package com.streamapp.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
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
import com.streamapp.ui.components.LoadingState
import com.streamapp.ui.components.OfflineState
import com.streamapp.ui.components.ServerErrorState
import com.streamapp.ui.theme.*
import com.streamapp.ui.viewmodel.SearchViewModel
import com.streamapp.util.ConnectivityUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onChannelClick: (Int, String) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        // Search bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Surface,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextSecondary)
                }

                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        viewModel.search(it)
                    },
                    placeholder = { Text("Search channels...", color = TextTertiary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Primary,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedContainerColor = SurfaceVariant,
                        unfocusedContainerColor = SurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = TextTertiary)
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = {
                                searchText = ""
                                viewModel.search("")
                            }) {
                                Icon(Icons.Filled.Clear, "Clear", tint = TextTertiary)
                            }
                        }
                    }
                )
            }
        }

        // Results
        when {
            uiState.isLoading -> {
                LoadingState(message = "Searching...")
            }
            uiState.error != null -> {
                if (ConnectivityUtil.isOnline(context)) {
                    ServerErrorState(
                        message = uiState.error ?: "",
                        onRetry = { viewModel.search(searchText) }
                    )
                } else {
                    OfflineState(
                        onRetry = { viewModel.search(searchText) }
                    )
                }
            }
            uiState.results.isEmpty() && uiState.hasSearched -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.SearchOff,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No results found", color = TextSecondary, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text("Try a different search term", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            !uiState.hasSearched -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ManageSearch,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Search for your favorite channels", color = TextSecondary, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text("Football, Cricket, News & more", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.results, key = { it.id }) { channel ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChannelClick(channel.id, channel.name) },
                            shape = RoundedCornerShape(14.dp),
                            color = GlassSurface
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Primary.copy(alpha = 0.2f), Secondary.copy(alpha = 0.2f))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Tv, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = channel.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (channel.categoryName != null) {
                                        Text(
                                            text = channel.categoryName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
