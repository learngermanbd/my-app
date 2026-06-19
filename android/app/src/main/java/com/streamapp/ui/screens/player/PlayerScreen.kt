package com.streamapp.ui.screens.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Rational
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.VideoSize
import androidx.media3.session.MediaSession
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.streamapp.ui.components.LoadingState
import com.streamapp.ui.components.OfflineState
import com.streamapp.ui.components.ServerErrorState
import com.streamapp.ui.theme.*
import com.streamapp.ui.viewmodel.PlayerViewModel
import com.streamapp.util.ConnectivityUtil
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
fun PlayerScreen(
    channelId: Int,
    channelName: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isLocked by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var showChannelList by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var playerRef by remember { mutableStateOf<ExoPlayer?>(null) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var isInPipMode by remember { mutableStateOf(false) }
    var aspectRatio by remember { mutableStateOf(Rational(16, 9)) }
    var seekPreview by remember { mutableStateOf<Pair<Float, Long>?>(null) }
    var showBrightnessOverlay by remember { mutableStateOf(false) }
    var brightnessLevel by remember { mutableStateOf(1f) }
    var showVolumeOverlay by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableStateOf(1f) }
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        activity?.let { act ->
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            val insetsController = WindowInsetsControllerCompat(act.window, act.window.decorView)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            playerRef?.stop()
            playerRef?.release()
            playerRef = null
            activity?.let { act ->
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                val insetsController = WindowInsetsControllerCompat(act.window, act.window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    LaunchedEffect(channelId) {
        viewModel.loadStream(channelId)
    }

    LaunchedEffect(showControls) {
        if (showControls && !isLocked) {
            delay(5000)
            if (!isLocked) showControls = false
        }
    }

    LaunchedEffect(playerRef) {
        while (playerRef != null) {
            currentPosition = playerRef!!.currentPosition
            duration = playerRef!!.duration
            delay(250)
        }
    }

    DisposableEffect(activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    isInPipMode = activity?.isInPictureInPictureMode ?: false
                    if (!isInPipMode) {
                        val insetsController = WindowInsetsControllerCompat(activity!!.window, activity!!.window.decorView)
                        insetsController.hide(WindowInsetsCompat.Type.systemBars())
                        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
            }
            (activity as? androidx.lifecycle.LifecycleOwner)?.lifecycle?.addObserver(observer)
            onDispose { (activity as? androidx.lifecycle.LifecycleOwner)?.lifecycle?.removeObserver(observer) }
        } else {
            onDispose { }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        if (uiState.activeUrl != null) {
            ExoPlayerView(
                url = uiState.activeUrl!!,
                isPlaying = uiState.isPlaying,
                onPlaying = { viewModel.onPlaying() },
                onBuffering = { isBuffering = it },
                onError = { viewModel.onError(it) },
                onPlayerReady = { playerRef = it },
                onVideoSizeChanged = { w, h ->
                    if (w > 0 && h > 0) aspectRatio = Rational(w, h)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Buffering spinner
            if (isBuffering && !isInPipMode) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Primary,
                            modifier = Modifier.size(52.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Buffering...", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            // Gesture overlay for seek/volume/brightness (only when unlocked)
            if (!isInPipMode && !isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    seekPreview?.let { (_, pos) ->
                                        playerRef?.seekTo(pos)
                                    }
                                    seekPreview = null
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    if (duration > 0) {
                                        val deltaMs = (dragAmount / 500f * duration).roundToLong()
                                        val newPos = (currentPosition + deltaMs).coerceIn(0, duration)
                                        seekPreview = Pair(dragAmount / 500f, newPos)
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    showBrightnessOverlay = false
                                    showVolumeOverlay = false
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    val screenHalf = size.width / 2f
                                    if (change.position.x < screenHalf) {
                                        // Left side: brightness
                                        brightnessLevel = (brightnessLevel - dragAmount / 300f).coerceIn(0f, 1f)
                                        showBrightnessOverlay = true
                                    } else {
                                        // Right side: volume
                                        volumeLevel = (volumeLevel - dragAmount / 300f).coerceIn(0f, 1f)
                                        showVolumeOverlay = true
                                    }
                                }
                            )
                        }
                        .clickable(enabled = !isLocked) {
                            showControls = !showControls
                        }
                )
            }

            // Seek preview overlay
            if (seekPreview != null && !isInPipMode) {
                val previewPos = seekPreview!!.second
                val progress = if (duration > 0) previewPos.toFloat() / duration.toFloat() else 0f
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.FastForward,
                            contentDescription = "Seek",
                            tint = TextPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Background.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = formatDuration(previewPos),
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(GlassWhite)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Primary)
                            )
                        }
                    }
                }
            }

            // Brightness overlay
            if (showBrightnessOverlay && !isInPipMode) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Surface(
                        modifier = Modifier.padding(start = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Background.copy(alpha = 0.75f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Rounded.BrightnessHigh,
                                contentDescription = "Brightness",
                                tint = Warning,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(GlassWhite)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(brightnessLevel)
                                        .align(Alignment.BottomCenter)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Warning)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "${(brightnessLevel * 100).roundToInt()}%",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Volume overlay
            if (showVolumeOverlay && !isInPipMode) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Surface(
                        modifier = Modifier.padding(end = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Background.copy(alpha = 0.75f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (volumeLevel > 0f) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                                contentDescription = "Volume",
                                tint = TextPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(GlassWhite)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(volumeLevel)
                                        .align(Alignment.BottomCenter)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(TextPrimary)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "${(volumeLevel * 100).roundToInt()}%",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Top bar controls
            AnimatedVisibility(
                visible = showControls || isLocked,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(
                        Brush.verticalGradient(listOf(Background.copy(alpha = 0.85f), Background.copy(alpha = 0f)))
                    ).padding(horizontal = 8.dp).statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp).background(GlassWhite, RoundedCornerShape(10.dp))) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(text = channelName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 200.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (isLocked) {
                            IconButton(onClick = { isLocked = false; showControls = true }, modifier = Modifier.size(36.dp).background(GlassWhite, RoundedCornerShape(10.dp))) {
                                Icon(Icons.Rounded.Lock, contentDescription = "Unlock", tint = Warning, modifier = Modifier.size(20.dp))
                            }
                        } else {
                            IconButton(onClick = { isLocked = true }, modifier = Modifier.size(36.dp).background(GlassWhite, RoundedCornerShape(10.dp))) {
                                Icon(Icons.Rounded.LockOpen, contentDescription = "Lock", tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { playerRef?.seekTo(playerRef!!.currentPosition - 10000) }, modifier = Modifier.size(36.dp).background(GlassWhite, RoundedCornerShape(10.dp))) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { showChannelList = !showChannelList }, modifier = Modifier.size(36.dp).background(GlassWhite, RoundedCornerShape(10.dp))) {
                                Icon(Icons.Rounded.List, contentDescription = "Channels", tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
                                    activity!!.enterPictureInPictureMode(
                                        PictureInPictureParams.Builder().setAspectRatio(aspectRatio).build()
                                    )
                                }
                            }, modifier = Modifier.size(36.dp).background(GlassWhite, RoundedCornerShape(10.dp))) {
                                Icon(Icons.Rounded.PictureInPicture, contentDescription = "PiP", tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { /* Settings */ }, modifier = Modifier.size(36.dp).background(GlassWhite, RoundedCornerShape(10.dp))) {
                                Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // Channel selector bottom sheet
            AnimatedVisibility(
                visible = showChannelList && !isLocked,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(
                    Brush.verticalGradient(listOf(Background.copy(alpha = 0f), Background.copy(alpha = 0.9f)))
                ).padding(vertical = 20.dp)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Choose Stream", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Surface(shape = RoundedCornerShape(6.dp), color = LiveRed) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(TextPrimary))
                                    Spacer(Modifier.width(4.dp))
                                    Text("LIVE", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                            items(uiState.links.take(8)) { link ->
                                Surface(
                                    modifier = Modifier.width(90.dp).clickable { viewModel.selectLink(link.url) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (link.url == uiState.activeUrl) Primary.copy(alpha = 0.25f) else SurfaceVariant.copy(alpha = 0.6f),
                                    shadowElevation = if (link.url == uiState.activeUrl) 4.dp else 0.dp
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(Primary.copy(alpha = 0.2f), Secondary.copy(alpha = 0.15f)))), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Rounded.Tv, contentDescription = null, tint = if (link.url == uiState.activeUrl) Primary else TextSecondary, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(text = link.label, color = if (link.url == uiState.activeUrl) Primary else TextSecondary, fontSize = 11.sp, fontWeight = if (link.url == uiState.activeUrl) FontWeight.Bold else FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom controls bar
            AnimatedVisibility(
                visible = showControls && !isLocked,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(
                    Brush.verticalGradient(listOf(Background.copy(alpha = 0f), Background.copy(alpha = 0.85f)))
                ).navigationBarsPadding()) {
                    PlayerControls(
                        isPlaying = uiState.isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        onPlayPause = { viewModel.togglePlay() },
                        onRewind = { playerRef?.seekTo((playerRef!!.currentPosition - 10000).coerceAtLeast(0)) },
                        onForward = { playerRef?.seekTo((playerRef!!.currentPosition + 30000).coerceAtMost(duration)) },
                        onPrev = { playerRef?.seekTo(0) },
                        onNext = { /* next channel */ },
                        onSeek = { pos -> playerRef?.seekTo(pos) }
                    )
                }
            }

            // Error banner for active stream issues
            if (uiState.error != null && uiState.activeUrl != null) {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 72.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = LiveRed.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(text = uiState.error!!, color = TextPrimary, fontSize = 13.sp)
                    }
                }
            }

            // Lock screen indicator (when locked)
            if (isLocked) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = "Locked",
                            tint = TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tap to unlock controls",
                            color = TextSecondary.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else if (uiState.isLoading) {
            LoadingState(message = "Loading stream...")
        } else if (uiState.error != null) {
            if (ConnectivityUtil.isOnline(context)) {
                ServerErrorState(
                    message = uiState.error ?: "",
                    onRetry = { viewModel.loadStream(channelId) }
                )
            } else {
                OfflineState(
                    onRetry = { viewModel.loadStream(channelId) }
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.VideocamOff, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No stream available", color = TextSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // Seek bar with thumb
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clickable(enabled = duration > 0) {
                    // Click anywhere on the seek bar to seek
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GlassWhite)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(2.dp))
                        .background(Primary)
                )
            }
            // Seek thumb — positioned via alignment + fillMaxWidth
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .wrapContentWidth(Alignment.End)
                    .padding(end = 6.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Primary)
                    .border(2.dp, TextPrimary, CircleShape)
            )
        }

        // Time display
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(formatDuration(currentPosition), color = TextSecondary, fontSize = 11.sp)
            Text(formatDuration(duration), color = TextSecondary, fontSize = 11.sp)
        }

        Spacer(Modifier.height(8.dp))

        // Playback controls
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = TextSecondary, modifier = Modifier.size(28.dp))
            }
            IconButton(onClick = onRewind) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Replay10, contentDescription = "Rewind 10s", tint = TextPrimary, modifier = Modifier.size(30.dp))
                }
            }
            Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = Primary.copy(alpha = 0.2f)) {
                IconButton(onClick = onPlayPause) {
                    Icon(
                        if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            IconButton(onClick = onForward) {
                Icon(Icons.Rounded.Forward30, contentDescription = "Forward 30s", tint = TextPrimary, modifier = Modifier.size(30.dp))
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = TextSecondary, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun ExoPlayerView(
    url: String,
    isPlaying: Boolean,
    onPlaying: () -> Unit,
    onBuffering: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onPlayerReady: (ExoPlayer) -> Unit,
    onVideoSizeChanged: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var playerRef by remember { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(isPlaying) {
        playerRef?.playWhenReady = isPlaying
    }

    var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }
    var mediaSessionRef by remember { mutableStateOf<MediaSession?>(null) }

    LaunchedEffect(url) {
        val p = playerRef ?: return@LaunchedEffect
        p.stop()
        p.clearMediaItems()
        p.setMediaItem(MediaItem.fromUri(url))
        p.prepare()
        p.playWhenReady = isPlaying
    }

    DisposableEffect(Unit) {
        onDispose {
            playerRef?.stop()
            playerRef?.release()
            playerRef = null
            playerViewRef?.player = null
            playerViewRef = null
            mediaSessionRef?.release()
            mediaSessionRef = null
        }
    }

    AndroidView(
        factory = { ctx ->
            val exoPlayer = ExoPlayer.Builder(ctx).build().apply {
                playerRef = this
                onPlayerReady(this)
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = isPlaying
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> { onPlaying(); onBuffering(false) }
                            Player.STATE_BUFFERING -> onBuffering(true)
                            Player.STATE_ENDED -> seekTo(0)
                        }
                    }
                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        onVideoSizeChanged(videoSize.width, videoSize.height)
                    }
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        onError(error.message ?: "Playback error")
                    }
                })
            }
            PlayerView(ctx).apply {
                playerViewRef = this
                player = exoPlayer
                mediaSessionRef = MediaSession.Builder(ctx, exoPlayer).build()
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            }
        },
        modifier = modifier
    )
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "00:00"
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}
