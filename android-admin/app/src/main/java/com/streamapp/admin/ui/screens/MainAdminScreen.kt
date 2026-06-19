package com.streamapp.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamapp.admin.data.*
import com.streamapp.admin.ui.theme.*
import kotlinx.coroutines.launch

data class TabItem(val id: String, val label: String, val icon: ImageVector)
val tabs = listOf(
    TabItem("dashboard", "Dashboard", Icons.Rounded.Dashboard),
    TabItem("categories", "Categories", Icons.Rounded.Category),
    TabItem("channels", "Channels", Icons.Rounded.LiveTv),
    TabItem("links", "Links", Icons.Rounded.Link),
    TabItem("events", "Events", Icons.Rounded.Event),
    TabItem("config", "Config", Icons.Rounded.Settings),
    TabItem("m3u", "M3U", Icons.Rounded.FileUpload),
    TabItem("schedules", "Schedules", Icons.Rounded.Schedule),
    TabItem("crashlogs", "Crash Logs", Icons.Rounded.BugReport),
    TabItem("legal", "Legal", Icons.Rounded.Description),
    TabItem("security", "Security", Icons.Rounded.Shield)
)

@Composable
fun MainAdminScreen(
    api: AdminApi, serverUrl: String, currentTab: String,
    onTabChange: (String) -> Unit, onDisconnect: () -> Unit,
    onServerUrlChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbar) { data -> Snackbar(snackbarData = data, containerColor = Surface) } },
        topBar = {
            Surface(color = Surface, shadowElevation = 0.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("StreamApp Admin", color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(serverUrl.removePrefix("http://").removePrefix("https://"), color = TextTertiary, fontSize = 10.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onDisconnect, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.PowerSettingsNew, "Disconnect", tint = LiveRed, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tab row
            LazyRow(
                modifier = Modifier.fillMaxWidth().background(Surface),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(tabs) { tab ->
                    val selected = currentTab == tab.id
                    Surface(
                        modifier = Modifier.clickable { onTabChange(tab.id) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) PrimaryDim else Color.Transparent
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(tab.icon, tab.label, tint = if (selected) Primary else TextTertiary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(tab.label, color = if (selected) Primary else TextSecondary, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
            HorizontalDivider(color = GlassBorder)

            // Content
            Box(modifier = Modifier.fillMaxSize().background(Background)) {
                when (currentTab) {
                    "dashboard" -> DashboardTab(api, snackbar, scope)
                    "categories" -> CategoriesTab(api, snackbar, scope)
                    "channels" -> ChannelsTab(api, snackbar, scope)
                    "links" -> LinksTab(api, snackbar, scope)
                    "events" -> EventsTab(api, snackbar, scope)
                    "config" -> ConfigTab(api, snackbar, scope)
                    "m3u" -> M3uTab(api, snackbar, scope)
                    "schedules" -> SchedulesTab(api, snackbar, scope)
                    "crashlogs" -> CrashLogsTab(api, snackbar, scope)
                    "legal" -> LegalTab(api, snackbar, scope)
                    "security" -> SecurityTab(api, snackbar, scope)
                }
            }
        }
    }
}

// --- Shared Components ---
@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun ConfirmDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = Surface,
        titleContentColor = TextPrimary, textContentColor = TextSecondary,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete", color = LiveRed, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}

// --- Reusable Dialog Components ---
@Composable
fun FormDialog(title: String, fields: @Composable () -> Unit, onSave: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = Surface,
        titleContentColor = TextPrimary, textContentColor = TextSecondary,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Column { fields() } },
        confirmButton = { TextButton(onClick = onSave) { Text("Save", color = Primary, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}

// ===== DASHBOARD =====
@Composable
fun DashboardTab(api: AdminApi, snackbar: SnackbarHostState, scope: kotlinx.coroutines.CoroutineScope) {
    var stats by remember { mutableStateOf<Stats?>(null) }
    var channels by remember { mutableStateOf<List<AdminChannel>>(emptyList()) }
    var analytics by remember { mutableStateOf<List<AnalyticsEvent>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var checkingHealth by remember { mutableStateOf(false) }

    suspend fun loadDashboard() {
        loading = true
        try { stats = api.getStats().body(); channels = api.getAdminChannels().body() ?: emptyList(); analytics = api.getAnalytics().body() ?: emptyList() } catch (_: Exception) {}
        loading = false
    }

    LaunchedEffect(Unit) { loadDashboard() }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (loading) item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
        else {
            item {
                val s = stats
                if (s != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            StatCard("${s.categories}", "Categories", Modifier.weight(1f))
                            StatCard("${s.channels}", "Channels", Modifier.weight(1f))
                            StatCard("${s.streamLinks}", "Links", Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            StatCard("${s.healthyLinks}/${s.streamLinks}", "Healthy", Modifier.weight(1f))
                            StatCard("${s.analyticsEvents}", "Events", Modifier.weight(1f))
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(4.dp)) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { scope.launch { loadDashboard() } },
                        colors = ButtonDefaults.buttonColors(containerColor = Surface2),
                        shape = RoundedCornerShape(8.dp)
                    ) { Icon(Icons.Rounded.Refresh, "Refresh", modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Refresh", fontSize = 12.sp) }
                    Button(
                        onClick = {
                            checkingHealth = true; scope.launch {
                                try { val r = api.checkHealth().body(); snackbar.showSnackbar("Checked: ${r?.healthy} healthy, ${r?.dead} dead") } catch (_: Exception) {}
                                checkingHealth = false; loadDashboard()
                            }
                        },
                        enabled = !checkingHealth,
                        colors = ButtonDefaults.buttonColors(containerColor = Surface2),
                        shape = RoundedCornerShape(8.dp)
                    ) { if (checkingHealth) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Primary)
                        else { Icon(Icons.Rounded.Favorite, "Favorite", modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Health Check", fontSize = 12.sp) } }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Recent Channels", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        channels.take(15).forEach { ch ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(ch.name, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text(ch.categoryName ?: "", color = TextTertiary, fontSize = 10.sp, modifier = Modifier.width(80.dp))
                                Text("${ch.healthyLinks}/${ch.totalLinks}", color = if (ch.healthyLinks > 0) Primary else LiveRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = GlassBorder)
                        }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Analytics Events", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        analytics.forEach { ev ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(ev.eventName, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("${ev.count}", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = GlassBorder)
                        }
                    }
                }
            }
        }
    }
}

// ===== CATEGORIES =====
@Composable
fun CategoriesTab(api: AdminApi, snackbar: SnackbarHostState, scope: kotlinx.coroutines.CoroutineScope) {
    var cats by remember { mutableStateOf<List<Category>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var editCat by remember { mutableStateOf<Category?>(null) }
    var catName by remember { mutableStateOf("") }; var catSlug by remember { mutableStateOf("") }
    var catIcon by remember { mutableStateOf("") }; var catOrder by remember { mutableStateOf("0") }

    LaunchedEffect(Unit) { try { cats = api.getCategories().body() ?: emptyList() } catch (_: Exception) {}; loading = false }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Categories (${cats.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(onClick = { editCat = null; catName = ""; catSlug = ""; catIcon = ""; catOrder = "0"; showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Rounded.Add, "Add", Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Add", fontSize = 12.sp) }
            }
        }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
        else items(cats) { cat ->
            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cat.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("slug: ${cat.slug}  •  ${cat.channelCount} channels", color = TextTertiary, fontSize = 11.sp)
                    }
                    IconButton(onClick = { editCat = cat; catName = cat.name; catSlug = cat.slug; catIcon = cat.iconUrl ?: ""; catOrder = "${cat.sortOrder}"; showDialog = true }) { Icon(Icons.Rounded.Edit, "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = { scope.launch { try { api.deleteCategory(cat.id); cats = api.getCategories().body() ?: emptyList(); snackbar.showSnackbar("Deleted ${cat.name}") } catch (e: Exception) { snackbar.showSnackbar("Error: ${e.message}") } } }) { Icon(Icons.Rounded.Delete, "Delete", tint = LiveRed, modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
    if (showDialog) FormDialog(title = if (editCat == null) "Add Category" else "Edit Category",
        fields = {
            OutlinedTextField(catName, { catName = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(catSlug, { catSlug = it }, label = { Text("Slug") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(catIcon, { catIcon = it }, label = { Text("Icon") }, singleLine = true, modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                OutlinedTextField(catOrder, { catOrder = it }, label = { Text("Order") }, singleLine = true, modifier = Modifier.width(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
            }
        },
        onSave = { scope.launch {
            try {
                val body = mapOf("name" to catName, "slug" to catSlug, "icon_url" to catIcon, "sort_order" to (catOrder.toIntOrNull() ?: 0))
                if (editCat == null) api.createCategory(body) else api.updateCategory(editCat!!.id, body)
                cats = api.getCategories().body() ?: emptyList(); showDialog = false; snackbar.showSnackbar("Saved")
            } catch (e: Exception) { snackbar.showSnackbar("Error: ${e.message}") }
        } },
        onDismiss = { showDialog = false }
    )
}

// ===== CHANNELS =====
@Composable
fun ChannelsTab(api: AdminApi, snackbar: SnackbarHostState, scope: kotlinx.coroutines.CoroutineScope) {
    var channels by remember { mutableStateOf<List<AdminChannel>>(emptyList()) }
    var cats by remember { mutableStateOf<List<Category>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }; var search by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }; var editCh by remember { mutableStateOf<AdminChannel?>(null) }
    var chName by remember { mutableStateOf("") }; var chCat by remember { mutableStateOf(0) }
    var chLive by remember { mutableStateOf(false) }; var chFeatured by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { try { channels = api.getAdminChannels().body() ?: emptyList(); cats = api.getCategories().body() ?: emptyList() } catch (_: Exception) {}; loading = false }
    val filtered = remember(channels, search) { channels.filter { search.isBlank() || it.name.contains(search, ignoreCase = true) } }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(search, { search = it }, placeholder = { Text("Search channels...", color = TextTertiary) }, singleLine = true,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Primary),
                    textStyle = TextStyle(fontSize = 13.sp)
                )
                Button(onClick = { editCh = null; chName = ""; chCat = cats.firstOrNull()?.id ?: 0; chLive = false; chFeatured = false; showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Rounded.Add, "Add", Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Add", fontSize = 12.sp) }
            }
        }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
        else items(filtered) { ch ->
            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(ch.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            if (ch.isLive == 1) { Spacer(Modifier.width(6.dp)); Text("LIVE", color = LiveRed, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                            if (ch.featured == 1) { Spacer(Modifier.width(4.dp)); Icon(Icons.Rounded.Star, "Featured", tint = Warning, modifier = Modifier.size(12.dp)) }
                        }
                        Text("${ch.categoryName}  •  ${ch.healthyLinks}/${ch.totalLinks} healthy", color = TextTertiary, fontSize = 10.sp)
                    }
                    IconButton(onClick = { editCh = ch; chName = ch.name; chCat = ch.categoryId; chLive = ch.isLive == 1; chFeatured = ch.featured == 1; showDialog = true }) { Icon(Icons.Rounded.Edit, "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = { scope.launch { try { api.deleteChannel(ch.id); channels = api.getAdminChannels().body() ?: emptyList(); snackbar.showSnackbar("Deleted") } catch (e: Exception) { snackbar.showSnackbar("Error") } } }) { Icon(Icons.Rounded.Delete, "Delete", tint = LiveRed, modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
    if (showDialog) FormDialog(title = if (editCh == null) "Add Channel" else "Edit Channel",
        fields = {
            OutlinedTextField(chName, { chName = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
            Spacer(Modifier.height(8.dp))
            cats.forEach { cat ->
                Row(modifier = Modifier.clickable { chCat = cat.id }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = chCat == cat.id, onClick = { chCat = cat.id }, colors = RadioButtonDefaults.colors(selectedColor = Primary))
                    Spacer(Modifier.width(8.dp)); Text(cat.name, color = TextPrimary, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(chLive, { chLive = it }, colors = CheckboxDefaults.colors(checkedColor = Primary)); Text("Live", color = TextPrimary, fontSize = 13.sp) }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(chFeatured, { chFeatured = it }, colors = CheckboxDefaults.colors(checkedColor = Primary)); Text("Featured", color = TextPrimary, fontSize = 13.sp) }
            }
        },
        onSave = { scope.launch {
            try {
                val body = mapOf("category_id" to chCat, "name" to chName, "is_live" to (if (chLive) 1 else 0), "featured" to (if (chFeatured) 1 else 0))
                if (editCh == null) api.createChannel(body) else api.updateChannel(editCh!!.id, body)
                channels = api.getAdminChannels().body() ?: emptyList(); showDialog = false; snackbar.showSnackbar("Saved")
            } catch (e: Exception) { snackbar.showSnackbar("Error: ${e.message}") }
        } },
        onDismiss = { showDialog = false }
    )
}

// ===== LINKS =====
@Composable
fun LinksTab(api: AdminApi, snackbar: SnackbarHostState, scope: kotlinx.coroutines.CoroutineScope) {
    var channels by remember { mutableStateOf<List<AdminChannel>>(emptyList()) }
    var links by remember { mutableStateOf<List<StreamLink>>(emptyList()) }
    var selectedCh by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { try { channels = api.getAdminChannels().body() ?: emptyList() } catch (_: Exception) {}; loading = false }
    LaunchedEffect(selectedCh) { if (selectedCh != null) try { links = api.getLinks(selectedCh!!).body() ?: emptyList() } catch (_: Exception) {} else links = emptyList() }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Stream Links", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                channels.forEach { ch ->
                    Surface(modifier = Modifier.clickable { selectedCh = ch.id },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedCh == ch.id) PrimaryDim else Surface2) {
                        Text(ch.name, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = if (selectedCh == ch.id) Primary else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        if (selectedCh != null) {
            val ch = channels.find { it.id == selectedCh }
            item {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("${ch?.name ?: ""} — ${links.size} links", color = TextSecondary, fontSize = 12.sp)
                    Button(onClick = { scope.launch { try { val r = api.checkHealth().body(); snackbar.showSnackbar("Checked: ${r?.healthy} healthy, ${r?.dead} dead"); links = api.getLinks(selectedCh!!).body() ?: emptyList() } catch (e: Exception) {} } },
                        colors = ButtonDefaults.buttonColors(containerColor = Surface2), shape = RoundedCornerShape(8.dp)) { Text("Health Check", fontSize = 11.sp) }
                }
            }
            items(links) { link ->
                Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(link.url, color = TextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(link.label, color = TextTertiary, fontSize = 10.sp)
                                Text(link.quality, color = TextTertiary, fontSize = 10.sp)
                                Text(if (link.isHealthy == 1) "Healthy" else "Dead", color = if (link.isHealthy == 1) Primary else LiveRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(if (link.isActive == 1) "Active" else "Disabled", color = if (link.isActive == 1) TextSecondary else TextTertiary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===== EVENTS =====
@Composable
fun EventsTab(api: AdminApi, snackbar: SnackbarHostState, scope: kotlinx.coroutines.CoroutineScope) {
    var events by remember { mutableStateOf<List<LiveEvent>>(emptyList()) }; var loading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }; var editEv by remember { mutableStateOf<LiveEvent?>(null) }
    var evTitle by remember { mutableStateOf("") }; var evLive by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { try { events = api.getLive().body()?.events ?: emptyList() } catch (_: Exception) {}; loading = false }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Live Events (${events.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(onClick = { editEv = null; evTitle = ""; evLive = true; showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Rounded.Add, "Add", Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Add", fontSize = 12.sp) }
            }
        }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
        else items(events) { ev ->
            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(ev.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            if (ev.isLive == 1) { Spacer(Modifier.width(6.dp)); Text("LIVE", color = LiveRed, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                        }
                        Text("${ev.channelName ?: "No channel"}  •  ${ev.startsAt ?: ""}", color = TextTertiary, fontSize = 10.sp)
                    }
                    IconButton(onClick = { editEv = ev; evTitle = ev.title; evLive = ev.isLive == 1; showDialog = true }) { Icon(Icons.Rounded.Edit, "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = { scope.launch { try { api.deleteEvent(ev.id); events = api.getLive().body()?.events ?: emptyList(); snackbar.showSnackbar("Deleted") } catch (e: Exception) {} } }) { Icon(Icons.Rounded.Delete, "Delete", tint = LiveRed, modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
    if (showDialog) FormDialog(title = if (editEv == null) "Add Event" else "Edit Event",
        fields = {
            OutlinedTextField(evTitle, { evTitle = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(evLive, { evLive = it }, colors = CheckboxDefaults.colors(checkedColor = Primary)); Text("Is Live", color = TextPrimary, fontSize = 13.sp) }
        },
        onSave = { scope.launch {
            try {
                val body = mapOf("title" to evTitle, "is_live" to (if (evLive) 1 else 0))
                if (editEv == null) api.createEvent(body) else api.updateEvent(editEv!!.id, body)
                events = api.getLive().body()?.events ?: emptyList(); showDialog = false; snackbar.showSnackbar("Saved")
            } catch (e: Exception) { snackbar.showSnackbar("Error: ${e.message}") }
        } },
        onDismiss = { showDialog = false }
    )
}

// ===== CONFIG =====
@Composable
fun ConfigTab(api: AdminApi, snackbar: SnackbarHostState, scope: kotlinx.coroutines.CoroutineScope) {
    var config by remember { mutableStateOf<List<ConfigItem>>(emptyList()) }; var loading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }; var cfgKey by remember { mutableStateOf("") }; var cfgVal by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { try { config = api.getConfig().body() ?: emptyList() } catch (_: Exception) {}; loading = false }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Configuration", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(onClick = { cfgKey = ""; cfgVal = ""; showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Rounded.Add, "Add", Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Add", fontSize = 12.sp) }
            }
        }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
        else items(config) { item ->
            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.key, color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(140.dp))
                    Text(item.value, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    IconButton(onClick = { cfgKey = item.key; cfgVal = item.value; showDialog = true }) { Icon(Icons.Rounded.Edit, "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = { scope.launch { try { api.deleteConfig(item.key); config = api.getConfig().body() ?: emptyList() } catch (_: Exception) {} } }) { Icon(Icons.Rounded.Delete, "Delete", tint = LiveRed, modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
    if (showDialog) FormDialog(title = "Config Key",
        fields = {
            OutlinedTextField(cfgKey, { cfgKey = it }, label = { Text("Key") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(cfgVal, { cfgVal = it }, label = { Text("Value") }, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
        },
        onSave = { scope.launch { try { api.upsertConfig(mapOf("key" to cfgKey, "value" to cfgVal)); config = api.getConfig().body() ?: emptyList(); showDialog = false; snackbar.showSnackbar("Saved") } catch (e: Exception) { snackbar.showSnackbar("Error") } } },
        onDismiss = { showDialog = false }
    )
}

// ===== M3U IMPORT =====
@Composable
fun M3uTab(api: AdminApi, snackbar: SnackbarHostState, scope: kotlinx.coroutines.CoroutineScope) {
    var mode by remember { mutableStateOf("url") }; var m3uUrl by remember { mutableStateOf("") }
    var m3uText by remember { mutableStateOf("") }; var importing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<M3uResult?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("M3U Import", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(modifier = Modifier.clickable { mode = "url" }, shape = RoundedCornerShape(8.dp), color = if (mode == "url") PrimaryDim else Surface2) {
                Text("From URL", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = if (mode == "url") Primary else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Surface(modifier = Modifier.clickable { mode = "text" }, shape = RoundedCornerShape(8.dp), color = if (mode == "text") PrimaryDim else Surface2) {
                Text("Paste Text", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = if (mode == "text") Primary else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        if (mode == "url") {
            OutlinedTextField(m3uUrl, { m3uUrl = it }, label = { Text("M3U URL") }, placeholder = { Text("https://example.com/playlist.m3u") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
            Button(onClick = { importing = true; scope.launch {
                try { val r = api.importM3u(mapOf("url" to m3uUrl)); result = r.body(); if (r.isSuccessful) snackbar.showSnackbar("Imported!") else snackbar.showSnackbar("Failed") } catch (e: Exception) { snackbar.showSnackbar("Error: ${e.message}") }; importing = false
            } }, enabled = !importing && m3uUrl.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp)) {
                if (importing) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Background) else Text("Import from URL", color = Background)
            }
        } else {
            OutlinedTextField(m3uText, { m3uText = it }, label = { Text("M3U Content") }, modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
            Button(onClick = { importing = true; scope.launch {
                try { val r = api.importM3uText(mapOf("content" to m3uText)); result = r.body(); if (r.isSuccessful) snackbar.showSnackbar("Imported!") else snackbar.showSnackbar("Failed") } catch (e: Exception) { snackbar.showSnackbar("Error: ${e.message}") }; importing = false
            } }, enabled = !importing && m3uText.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp)) {
                if (importing) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Background) else Text("Import from Text", color = Background)
            }
        }
        if (result != null) {
            val r = result!!
            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Result", fontWeight = FontWeight.Bold, color = Primary, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("${r.totalParsed}", "Parsed", Modifier.weight(1f))
                        StatCard("${r.stats.channelsCreated}", "New Channels", Modifier.weight(1f))
                        StatCard("${r.stats.linksAdded}", "Links Added", Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ===== SCHEDULES =====
@Composable
fun SchedulesTab(api: AdminApi, snackbar: SnackbarHostState, scope: kotlinx.coroutines.CoroutineScope) {
    var schedules by remember { mutableStateOf<List<Schedule>>(emptyList()) }; var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { try { schedules = api.getSchedules().body() ?: emptyList() } catch (_: Exception) {}; loading = false }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (loading) item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
        else items(schedules) { s ->
            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Every ${s.intervalHours}h  •  ${s.lastRunAt ?: "Never run"}", color = TextTertiary, fontSize = 10.sp)
                        Text(s.url, color = TextTertiary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(if (s.isActive == 1) "Active" else "Paused", color = if (s.isActive == 1) Primary else TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { scope.launch { try { api.runSchedule(s.id); snackbar.showSnackbar("Schedule triggered") } catch (_: Exception) {} } }) { Icon(Icons.Rounded.PlayArrow, "Run", tint = Primary, modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = { scope.launch { try { api.deleteSchedule(s.id); schedules = api.getSchedules().body() ?: emptyList(); snackbar.showSnackbar("Deleted") } catch (_: Exception) {} } }) { Icon(Icons.Rounded.Delete, "Delete", tint = LiveRed, modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
}

// ===== CRASH LOGS =====
@Composable
fun CrashLogsTab(api: AdminApi, snackbar: SnackbarHostState, scope: kotlinx.coroutines.CoroutineScope) {
    var logs by remember { mutableStateOf<CrashResponse?>(null) }; var loading by remember { mutableStateOf(true) }
    var selectedLog by remember { mutableStateOf<CrashLog?>(null) }

    LaunchedEffect(Unit) { try { logs = api.getCrashLogs(50).body() } catch (_: Exception) {}; loading = false }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Crash Logs (${logs?.total ?: 0})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                IconButton(onClick = { scope.launch { try { logs = api.getCrashLogs(50).body() } catch (_: Exception) {} } }) { Icon(Icons.Rounded.Refresh, "Refresh", tint = TextSecondary) }
            }
        }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
        else (logs?.logs ?: emptyList()).forEach { log ->
            val params = log.params ?: emptyMap<Any, Any>()
            val paramsMap = if (params is Map<*, *>) params else emptyMap<Any, Any>()
            val msg = (paramsMap["message"] as? String) ?: log.eventName
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().clickable { selectedLog = log }) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${log.createdAt ?: ""}", color = TextTertiary, fontSize = 10.sp)
                            Text(msg, color = TextPrimary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text(log.deviceId ?: "", color = TextTertiary, fontSize = 10.sp)
                        Icon(Icons.Rounded.ChevronRight, "More", tint = TextTertiary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
    if (selectedLog != null) {
        val log = selectedLog!!
        val params = log.params ?: emptyMap<Any, Any>()
        val paramsMap = if (params is Map<*, *>) params else emptyMap<Any, Any>()
        AlertDialog(onDismissRequest = { selectedLog = null }, containerColor = Surface,
            titleContentColor = TextPrimary, textContentColor = TextSecondary,
            title = { Text("Crash Details", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Time: ${log.createdAt ?: "N/A"}", fontSize = 12.sp)
                    Text("Device: ${log.deviceId ?: "N/A"}", fontSize = 12.sp)
                    Text("Event: ${log.eventName}", fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Message: ${paramsMap["message"] ?: "N/A"}", fontSize = 11.sp, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Surface(color = Surface2, shape = RoundedCornerShape(8.dp)) {
                        val stack = (paramsMap["stack"] as? String) ?: "No stack trace"
                        Text(stack, modifier = Modifier.padding(8.dp), fontSize = 9.sp, color = TextTertiary,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedLog = null }) { Text("Close", color = Primary) } }
        )
    }
}

// ===== LEGAL (Notice & Copyright) =====
@Composable
fun LegalTab(api: AdminApi, snackbar: SnackbarHostState, scope: kotlinx.coroutines.CoroutineScope) {
    var noticeText by remember { mutableStateOf("") }
    var copyrightText by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var activeField by remember { mutableStateOf("notice") }

    LaunchedEffect(Unit) {
        try {
            val config = api.getConfig().body() ?: emptyList()
            noticeText = config.find { it.key == "legal_notice" }?.value ?: ""
            copyrightText = config.find { it.key == "legal_copyright" }?.value ?: ""
        } catch (_: Exception) {}
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Legal Content", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text("Edit the Notice and Copyright text shown in the app drawer.", color = TextTertiary, fontSize = 11.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(modifier = Modifier.clickable { activeField = "notice" },
                shape = RoundedCornerShape(8.dp),
                color = if (activeField == "notice") PrimaryDim else Surface2) {
                Text("Notice", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = if (activeField == "notice") Primary else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Surface(modifier = Modifier.clickable { activeField = "copyright" },
                shape = RoundedCornerShape(8.dp),
                color = if (activeField == "copyright") PrimaryDim else Surface2) {
                Text("Copyright", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = if (activeField == "copyright") Primary else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (loading) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            if (activeField == "notice") {
                OutlinedTextField(
                    value = noticeText,
                    onValueChange = { noticeText = it },
                    label = { Text("Notice Text") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    )
                )
                Button(
                    onClick = {
                        saving = true; scope.launch {
                            try {
                                api.upsertConfig(mapOf("key" to "legal_notice", "value" to noticeText))
                                snackbar.showSnackbar("Notice saved")
                            } catch (e: Exception) {
                                snackbar.showSnackbar("Error: ${e.message}")
                            }; saving = false
                        }
                    },
                    enabled = !saving,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (saving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Background)
                    else Text("Save Notice", color = Background)
                }
            } else {
                OutlinedTextField(
                    value = copyrightText,
                    onValueChange = { copyrightText = it },
                    label = { Text("Copyright Text") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    )
                )
                Button(
                    onClick = {
                        saving = true; scope.launch {
                            try {
                                api.upsertConfig(mapOf("key" to "legal_copyright", "value" to copyrightText))
                                snackbar.showSnackbar("Copyright saved")
                            } catch (e: Exception) {
                                snackbar.showSnackbar("Error: ${e.message}")
                            }; saving = false
                        }
                    },
                    enabled = !saving,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (saving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Background)
                    else Text("Save Copyright", color = Background)
                }
            }
        }
    }
}

// ===== SECURITY =====
@Composable
fun SecurityTab(api: AdminApi, snackbar: SnackbarHostState, scope: kotlinx.coroutines.CoroutineScope) {
    var config by remember { mutableStateOf<List<ConfigItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    var securityEnabled by remember { mutableStateOf(true) }
    var expectedPkg by remember { mutableStateOf("") }
    var expectedHash by remember { mutableStateOf("") }
    var blockMessage by remember { mutableStateOf("") }
    var downloadUrl by remember { mutableStateOf("") }

    suspend fun loadConfig() {
        loading = true
        try {
            config = api.getConfig().body() ?: emptyList()
            securityEnabled = config.find { it.key == "security_enabled" }?.value != "false"
            expectedPkg = config.find { it.key == "security_expected_package" }?.value ?: "com.streamapp"
            expectedHash = config.find { it.key == "security_expected_hash" }?.value ?: ""
            blockMessage = config.find { it.key == "security_block_message" }?.value ?: "This app has been modified. Please download the original from the official source."
            downloadUrl = config.find { it.key == "security_download_url" }?.value ?: ""
        } catch (_: Exception) {}
        loading = false
    }

    LaunchedEffect(Unit) { loadConfig() }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("App Security", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(
            "Configure integrity verification for the Android app. " +
                    "When security is enabled, the app must pass multi-layer checks on startup or it will be blocked.",
            color = TextTertiary, fontSize = 11.sp, lineHeight = 16.sp
        )

        if (loading) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Security Enabled", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(if (securityEnabled) "Integrity checks are active" else "All apps can connect without verification", color = TextTertiary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = securityEnabled,
                        onCheckedChange = { securityEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Primary, checkedThumbColor = TextPrimary)
                    )
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Expected Package Name", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("The app's package name must match this to pass the first integrity layer.", color = TextTertiary, fontSize = 10.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expectedPkg,
                        onValueChange = { expectedPkg = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Expected Signing Certificate Hash", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("SHA-256 hash of the app's signing certificate. Leave empty to skip this check.", color = TextTertiary, fontSize = 10.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expectedHash,
                        onValueChange = { expectedHash = it },
                        singleLine = true,
                        placeholder = { Text("e.g. a1b2c3d4...", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Block Message", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Shown when the app fails integrity checks.", color = TextTertiary, fontSize = 10.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = blockMessage,
                        onValueChange = { blockMessage = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Download URL", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Link shown on the blocked screen for users to download the original app.", color = TextTertiary, fontSize = 10.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = downloadUrl,
                        onValueChange = { downloadUrl = it },
                        singleLine = true,
                        placeholder = { Text("https://example.com/download", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary, unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }

            Button(
                onClick = {
                    saving = true; scope.launch {
                        try {
                            api.upsertConfig(mapOf("key" to "security_enabled", "value" to securityEnabled.toString()))
                            api.upsertConfig(mapOf("key" to "security_expected_package", "value" to expectedPkg))
                            api.upsertConfig(mapOf("key" to "security_expected_hash", "value" to expectedHash))
                            api.upsertConfig(mapOf("key" to "security_block_message", "value" to blockMessage))
                            api.upsertConfig(mapOf("key" to "security_download_url", "value" to downloadUrl))
                            snackbar.showSnackbar("Security settings saved")
                        } catch (e: Exception) {
                            snackbar.showSnackbar("Error: ${e.message}")
                        }; saving = false
                    }
                },
                enabled = !saving,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (saving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Background)
                else Text("Save Security Settings", color = Background, fontWeight = FontWeight.Bold)
            }

            Card(colors = CardDefaults.cardColors(containerColor = Surface2), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("How it works", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "1. App checks its package name matches the expected value\n" +
                                "2. App checks for root/hooking tools on the device\n" +
                                "3. App sends its cert hash + package name to the server\n" +
                                "4. Server verifies against these settings\n" +
                                "5. If any check fails, app shows the blocked screen",
                        color = TextTertiary, fontSize = 11.sp, lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
