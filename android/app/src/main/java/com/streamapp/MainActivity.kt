package com.streamapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.streamapp.data.repository.StreamRepository
import com.streamapp.ui.navigation.AppNavGraph
import com.streamapp.ui.navigation.NavRoutes
import com.streamapp.ui.components.SplashLoadingScreen
import com.streamapp.ui.screens.BlockedAppScreen
import com.streamapp.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppCheckState(
    val checking: Boolean = true,
    val needsUpdate: Boolean = false,
    val currentVersion: String = "",
    val minVersion: String = "",
    val forceExit: Boolean = false,
    val blocked: Boolean = false,
    val blockMessage: String = "",
    val downloadUrl: String = ""
)

@HiltViewModel
class AppCheckViewModel @Inject constructor(
    private val repository: StreamRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppCheckState())
    val state: StateFlow<AppCheckState> = _state.asStateFlow()

    init { checkVersion() }

    fun checkVersion() {
        viewModelScope.launch {
            _state.update { it.copy(checking = true) }
            try {
                val result = repository.getConfig()
                result.onSuccess { config ->
                    val current = BuildConfig.VERSION_NAME
                    val minVer = config.minVersion
                    val needsUpdate = compareVersions(minVer, current) > 0
                    _state.update { it.copy(checking = false, needsUpdate = needsUpdate, currentVersion = current, minVersion = minVer) }
                }.onFailure { _state.update { it.copy(checking = false) } }
            } catch (_: Exception) { _state.update { it.copy(checking = false) } }
        }
    }

    fun onSecurityResult(blocked: Boolean, message: String, downloadUrl: String) {
        _state.update { it.copy(blocked = blocked, blockMessage = message, downloadUrl = downloadUrl, checking = false) }
    }

    fun onExit() { _state.update { it.copy(forceExit = true) } }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1 - p2
        }
        return 0
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StreamAppTheme {
                val viewModel: AppCheckViewModel = hiltViewModel()
                val state by viewModel.state.collectAsState()
                val context = androidx.compose.ui.platform.LocalContext.current

                // Run security checks on startup
                LaunchedEffect(Unit) {
                    try {
                        // Layer 1: Check package name
                        if (!com.streamapp.security.IdentityValidator.checkPackageName(context)) {
                            viewModel.onSecurityResult(true, "Invalid app package.", "")
                            return@LaunchedEffect
                        }

                        // Layer 2: Runtime integrity checks
                        if (!com.streamapp.security.RuntimeIntegrityCheck.checkSignatureFiles()) {
                            viewModel.onSecurityResult(true, "Security tools detected. Please download the official app.", "")
                            return@LaunchedEffect
                        }

                        // Layer 3: Server-side verification
                        try {
                            val nonce = java.util.UUID.randomUUID().toString()
                            val certHash = com.streamapp.security.SignatureValidator.getSigningCertHash(context) ?: "unknown"
                            val api = createStreamApi()
                            val response = api.securityCheck(
                                com.streamapp.data.api.SecurityCheckRequest(
                                    packageName = context.packageName,
                                    certHash = certHash,
                                    versionName = com.streamapp.BuildConfig.VERSION_NAME,
                                    nonce = nonce
                                )
                            )
                            if (response.isSuccessful) {
                                val sec = response.body()
                                if (sec != null && sec.blocked) {
                                    viewModel.onSecurityResult(true, sec.message ?: "App has been modified.", sec.downloadUrl ?: "")
                                    return@LaunchedEffect
                                }
                            }
                        } catch (_: Exception) { }
                    } catch (_: Exception) { }
                }

                if (state.blocked) {
                    BlockedAppScreen(
                        message = state.blockMessage,
                        downloadUrl = state.downloadUrl,
                        packageName = context.packageName,
                        onExit = { viewModel.onExit() }
                    )
                } else if (state.forceExit) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Closing app...", color = TextSecondary)
                    }
                    LaunchedEffect(Unit) { finish() }
                } else if (state.needsUpdate) {
                    UpdateRequiredDialog(
                        currentVersion = state.currentVersion,
                        minVersion = state.minVersion,
                        onUpdate = {
                            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))) }
                            catch (_: Exception) { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))) }
                        },
                        onExit = { viewModel.onExit() }
                    )
                } else if (!state.checking) {
                    MainAppScreen()
                } else {
                    SplashLoadingScreen(
                        versionName = BuildConfig.VERSION_NAME
                    )
                }
            }
        }
    }
}

// --- Drawer Menu Items ---
data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val isToggle: Boolean = false,
    val badge: String? = null
)

@Composable
private fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    val bottomBarRoutes = listOf(NavRoutes.HOME, NavRoutes.CATEGORIES, NavRoutes.CHANNELS_TAB, NavRoutes.SEARCH)
    val showBottomBar = currentRoute in bottomBarRoutes
    var crashLogEnabled by remember { mutableStateOf(true) }
    var showNoticeDialog by remember { mutableStateOf<String?>(null) }
    var showCopyrightDialog by remember { mutableStateOf<String?>(null) }
    var showNetworkPlayerDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var networkPlayerOption by remember { mutableStateOf("Auto") }
    var videoQualityOption by remember { mutableStateOf("Auto") }

    val drawerItems = listOf(
        DrawerItem("Network Player", Icons.Rounded.Wifi, badge = "HLS"),
        DrawerItem("Playlists", Icons.Rounded.PlaylistPlay),
        DrawerItem("Floating Player", Icons.Rounded.PictureInPicture),
        DrawerItem("Video Quality", Icons.Rounded.HighQuality),
        DrawerItem("Crash Log", Icons.Rounded.BugReport, isToggle = true),
        DrawerItem("Notice", Icons.Rounded.Campaign),
        DrawerItem("Join Us", Icons.Rounded.GroupAdd),
        DrawerItem("Copyright", Icons.Rounded.Copyright),
        DrawerItem("Share", Icons.Rounded.Share),
        DrawerItem("Email", Icons.Rounded.Mail),
        DrawerItem("Update App", Icons.Rounded.SystemUpdateAlt),
        DrawerItem("Exit", Icons.Rounded.ExitToApp)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = DrawerScrim,
        gesturesEnabled = currentRoute != NavRoutes.PLAYER,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .background(DrawerBackground),
                drawerContainerColor = androidx.compose.ui.graphics.Color.Transparent
            ) {
                Column(modifier = Modifier.fillMaxHeight()) {
                    // Drawer header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("S", color = Background, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Sportzfy", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Live Sports", color = Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(horizontal = 20.dp))

                    // Drawer items
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        drawerItems.forEach { item ->
                            DrawerRowItem(
                                item = item,
                                isChecked = crashLogEnabled,
                                onToggle = { crashLogEnabled = it },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    when (item.label) {
                                        "Network Player" -> showNetworkPlayerDialog = true
                                        "Video Quality" -> showVideoQualityDialog = true
                                        "Playlists" -> toast(context, "Playlists — coming soon")
                                        "Floating Player" -> toast(context, "Floating Player — coming soon")
                                        "Notice" -> {
                                            scope.launch {
                                                showNoticeDialog = "Loading..."
                                                try {
                                                    val api = createStreamApi()
                                                    val resp = api.getLegal()
                                                    showNoticeDialog = if (resp.isSuccessful) resp.body()?.notice ?: "No notices."
                                                    else "Could not load notices."
                                                } catch (_: Exception) {
                                                    showNoticeDialog = "Could not load notices. Check your connection."
                                                }
                                            }
                                        }
                                        "Copyright" -> {
                                            scope.launch {
                                                showCopyrightDialog = "Loading..."
                                                try {
                                                    val api = createStreamApi()
                                                    val resp = api.getLegal()
                                                    showCopyrightDialog = if (resp.isSuccessful) resp.body()?.copyright ?: "No copyright info."
                                                    else "Could not load copyright info."
                                                } catch (_: Exception) {
                                                    showCopyrightDialog = "Could not load copyright info. Check your connection."
                                                }
                                            }
                                        }
                                        "Share" -> {
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "Check out StreamApp: https://play.google.com/store/apps/details?id=${context.packageName}")
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Share App"))
                                        }
                                        "Email" -> {
                                            try {
                                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                    data = Uri.parse("mailto:support@streamapp.com")
                                                }
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                toast(context, "support@streamapp.com")
                                            }
                                        }
                                        "Update App" -> {
                                            try {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")))
                                            } catch (_: Exception) {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                                            }
                                        }
                                        "Exit" -> activity?.finishAffinity()
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Bottom info
                    HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(horizontal = 20.dp))
                    Box(modifier = Modifier.padding(24.dp)) {
                        Column {
                            Text("App Config", color = TextTertiary, fontSize = 11.sp)
                            Text("Crash Log: ${if (crashLogEnabled) "ON" else "OFF"}", color = TextSecondary, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            val serverUrl = BuildConfig.API_BASE_URL.removeSuffix("/").removePrefix("http://").removePrefix("https://")
                            Text("Server: $serverUrl", color = TextTertiary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    StreamAppBottomBar(
                        currentRoute = currentRoute,
                        onTabSelected = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                AppNavGraph(
                    navController = navController,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
        }
    }

    // Network Player dialog
    if (showNetworkPlayerDialog) {
        AlertDialog(
            onDismissRequest = { showNetworkPlayerDialog = false },
            containerColor = Surface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Network Player", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("Auto", "External Player", "VLC", "MX Player").forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { networkPlayerOption = option; showNetworkPlayerDialog = false }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = networkPlayerOption == option,
                                onClick = { networkPlayerOption = option; showNetworkPlayerDialog = false },
                                colors = RadioButtonDefaults.colors(selectedColor = Primary)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(option, color = TextPrimary, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNetworkPlayerDialog = false }) {
                    Text("Done", color = Primary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Video Quality dialog
    if (showVideoQualityDialog) {
        AlertDialog(
            onDismissRequest = { showVideoQualityDialog = false },
            containerColor = Surface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Video Quality", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("Auto", "144p", "240p", "360p", "480p", "720p", "1080p").forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { videoQualityOption = option; showVideoQualityDialog = false }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = videoQualityOption == option,
                                onClick = { videoQualityOption = option; showVideoQualityDialog = false },
                                colors = RadioButtonDefaults.colors(selectedColor = Primary)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(option, color = TextPrimary, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVideoQualityDialog = false }) {
                    Text("Done", color = Primary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Notifications dialog
    if (showNotificationsDialog) {
        var notifLive by remember { mutableStateOf(true) }
        var notifChannels by remember { mutableStateOf(true) }
        var notifUpdates by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            containerColor = Surface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Notifications", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(
                        "Live Events" to notifLive,
                        "New Channels" to notifChannels,
                        "App Updates" to notifUpdates
                    ).forEach { (label, state) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = state,
                                onCheckedChange = { checked ->
                                    when (label) {
                                        "Live Events" -> notifLive = checked
                                        "New Channels" -> notifChannels = checked
                                        "App Updates" -> notifUpdates = checked
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Primary, checkmarkColor = Background)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(label, color = TextPrimary, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Done", color = Primary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (showNoticeDialog != null) {
        AlertDialog(
            onDismissRequest = { showNoticeDialog = null },
            containerColor = Surface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Notice", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        showNoticeDialog ?: "",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showNoticeDialog = null }) {
                    Text("OK", color = Primary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (showCopyrightDialog != null) {
        AlertDialog(
            onDismissRequest = { showCopyrightDialog = null },
            containerColor = Surface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Copyright", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        showCopyrightDialog ?: "",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCopyrightDialog = null }) {
                    Text("OK", color = Primary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }


}

@Composable
private fun DrawerRowItem(
    item: DrawerItem,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    var toggleState by remember { mutableStateOf(isChecked) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clickable {
                if (!item.isToggle) onClick()
            },
        shape = RoundedCornerShape(12.dp),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                item.label,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (item.badge != null) {
                Surface(shape = RoundedCornerShape(6.dp), color = Primary.copy(alpha = 0.15f)) {
                    Text(item.badge, color = Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            if (item.isToggle) {
                Switch(
                    checked = toggleState,
                    onCheckedChange = { toggleState = it; onToggle(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = Primary, checkedThumbColor = TextPrimary)
                )
            }
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
private fun StreamAppBottomBar(currentRoute: String?, onTabSelected: (String) -> Unit) {
    val items = listOf(
        BottomNavItem(NavRoutes.HOME, "Home", Icons.Rounded.Home),
        BottomNavItem(NavRoutes.CATEGORIES, "Categories", Icons.Rounded.GridView),
        BottomNavItem(NavRoutes.CHANNELS_TAB, "Channels", Icons.Rounded.LiveTv)
    )

    Surface(modifier = Modifier.fillMaxWidth(), color = BottomNavBackground, shadowElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Primary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { onTabSelected(item.route) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (selected) Primary else TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) Primary else TextTertiary,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UpdateRequiredDialog(currentVersion: String, minVersion: String, onUpdate: () -> Unit, onExit: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = Surface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text("Update Required", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("A new version is required to continue.", textAlign = TextAlign.Center, color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                Surface(color = SurfaceVariant, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Your version: v$currentVersion", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        Text("Required: v$minVersion", style = MaterialTheme.typography.bodySmall, color = LiveRed)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onUpdate, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text("Update Now", color = Background, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onExit) { Text("Exit App", color = TextSecondary) }
        }
    )
}

private fun toast(context: android.content.Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

private fun createStreamApi(): com.streamapp.data.api.StreamApi {
    val client = okhttp3.OkHttpClient.Builder()
        .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl(com.streamapp.BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()
    return retrofit.create(com.streamapp.data.api.StreamApi::class.java)
}
