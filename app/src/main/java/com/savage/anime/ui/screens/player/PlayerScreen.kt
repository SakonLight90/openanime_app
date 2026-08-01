package com.savage.anime.ui.screens.player

import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Rational

import android.view.TextureView
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.savage.anime.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.navigation.NavController
import com.savage.anime.utils.formatEpisodeNumber
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    navController: NavController,
    animeId: Int,
    episodeId: Int,
    viewModel: PlayerViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val sleepTimer by viewModel.sleepTimer.collectAsState()
    val context = LocalContext.current
    val activity = remember { context.findActivity() }
    val coroutineScope = rememberCoroutineScope()

    var showUi by remember { mutableStateOf(false) }
    var showCenterPlayPause by remember { mutableStateOf(false) }

    var showEpisodeList by remember { mutableStateOf(false) }
    var screenLocked by remember { mutableStateOf(false) }
    var showLockOverlay by remember { mutableStateOf(false) }
    var showSkipText by remember { mutableStateOf<String?>(null) }
    var totalSkipAmount by remember { mutableIntStateOf(0) }
    var skipForwardAfterNearEnd by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isNearEnd) {
        if (!uiState.isNearEnd) skipForwardAfterNearEnd = false
    }
    var playerViewReady by remember { mutableStateOf(false) }
    var pipResumeTrigger by remember { mutableIntStateOf(0) }
    val uiAlpha by animateFloatAsState(
        targetValue = if (showUi) 1f else 0f,
        label = "uiAlpha"
    )

    fun hideSystemUi() {
        activity?.window?.let { w ->
            w.insetsController?.hide(android.view.WindowInsets.Type.systemBars())
            w.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    @Suppress("DEPRECATION")
    fun applyCutoutMode() {
        activity?.window?.let { w ->
            w.setDecorFitsSystemWindows(false)
            w.attributes = w.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
            w.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK))
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    val isPip = activity?.isInPictureInPictureMode ?: false
                    if (isPip || PiPState.enteringPiP) {
                        PiPState.enteringPiP = false
                        viewModel.savePositionNow()
                    } else {
                        viewModel.onEnterBackground()
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    applyCutoutMode()
                    hideSystemUi()
                    activity?.requestedOrientation = if (screenLocked) ActivityInfo.SCREEN_ORIENTATION_LOCKED else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    if (PiPState.isPlayerActive) {
                        pipResumeTrigger++
                        if (!viewModel.isPlayerAlive()) {
                            viewModel.reloadPlayerForPip()
                        }
                    }
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            activity?.window?.insetsController?.show(android.view.WindowInsets.Type.systemBars())
        }
    }

    LaunchedEffect(uiState.streamUrl) {
        if (uiState.streamUrl != null) {
            delay(100)
            hideSystemUi()
        }
    }

    LaunchedEffect(animeId, episodeId) {
        PiPState.isPlayerActive = true
        viewModel.loadPlayer(animeId, episodeId)
        viewModel.loadAutoPlaySetting()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    DisposableEffect(Unit) {
        applyCutoutMode()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(showUi, uiState.isPlaying) {
        if (showUi && uiState.isPlaying) {
            delay(4000)
            showUi = false
            hideSystemUi()
        }
    }

    LaunchedEffect(showCenterPlayPause) {
        if (showCenterPlayPause) {
            delay(2000)
            showCenterPlayPause = false
        }
    }

    LaunchedEffect(showLockOverlay) {
        if (showLockOverlay) {
            delay(4000)
            showLockOverlay = false
        }
    }

    LaunchedEffect(showSkipText) {
        if (showSkipText != null) {
            delay(1200)
            showSkipText = null
            totalSkipAmount = 0
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            PiPState.isPlayerActive = false
            viewModel.savePosition()
        }
    }

    val pipReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == "PIP_PLAY_PAUSE") {
                    viewModel.togglePlayPause()
                    activity?.let { act ->
                        if (act.isInPictureInPictureMode) {
                            val isPlaying = viewModel.uiState.value.isPlaying
                            val pipIntent = Intent("PIP_PLAY_PAUSE").setPackage(ctx.packageName)
                            val pendingIntent = PendingIntent.getBroadcast(
                                ctx, 0, pipIntent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            act.setPictureInPictureParams(
                                PictureInPictureParams.Builder()
                                    .setAspectRatio(Rational(16, 9))
                                    .setActions(
                                        listOf(
                                            RemoteAction(
                                                Icon.createWithResource(ctx,
                                                    if (isPlaying) android.R.drawable.ic_media_pause
                                                    else android.R.drawable.ic_media_play
                                                ),
                                                if (isPlaying) "Pausa" else "Play",
                                                if (isPlaying) "Metti in pausa" else "Riproduci",
                                                pendingIntent
                                            )
                                        )
                                    )
                                    .build()
                            )
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val filter = IntentFilter("PIP_PLAY_PAUSE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(pipReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(pipReceiver, filter)
        }
        onDispose {
            context.unregisterReceiver(pipReceiver)
        }
    }

    BackHandler {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel.savePosition()
        navController.popBackStack()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            when {

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = Color.White,
                            fontSize = 18.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(onClick = {
                            if (uiState.episode == null) {
                                viewModel.retryLoadPlayer()
                            } else {
                                viewModel.retryStream()
                            }
                        }) {
                            Text(stringResource(R.string.player_retry))
                        }
                    }
                }

                else -> {

                    val textureViewRef = remember { mutableStateOf<TextureView?>(null) }

                    LaunchedEffect(pipResumeTrigger) {
                        val tv = textureViewRef.value ?: return@LaunchedEffect
                        viewModel.getExoPlayer()?.setVideoTextureView(tv)
                    }

                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned {
                                if (uiState.episode != null && !playerViewReady) {
                                    playerViewReady = true
                                }
                            },
                        factory = { ctx ->
                            val frame = AspectRatioFrameLayout(ctx)
                            val tv = TextureView(ctx)
                            textureViewRef.value = tv
                            frame.addView(tv, FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            ))
                            frame
                        },
                        update = { view ->
                            view.setResizeMode(uiState.resizeMode)
                            if (uiState.videoAspectRatio > 0f) {
                                view.setAspectRatio(uiState.videoAspectRatio)
                            }
                            view.requestLayout()
                            if (view.childCount > 0) {
                                val tv = view.getChildAt(0) as TextureView
                                viewModel.getExoPlayer()?.setVideoTextureView(tv)
                            }
                        }
                    )

                    LaunchedEffect(playerViewReady, uiState.streamUrl) {
                        if (playerViewReady && uiState.streamUrl == null && uiState.episode != null) {
                            viewModel.loadStream()
                        }
                    }

                    if (uiState.isLoading && uiState.streamUrl == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(screenLocked) {
                                if (screenLocked) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        coroutineScope.launch { showLockOverlay = true }
                                    }
                                } else {
                                    var lastTapTime = 0L
                                    var lastTapX = 0f
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        val centerX = size.width / 2f
                                        val centerY = size.height / 2f
                                        awaitPointerEvent()
                                        val now = System.currentTimeMillis()
                                        if (now - lastTapTime < 300L && abs(down.position.x - lastTapX) < 200f) {
                                            totalSkipAmount += 10
                                            val dir = if (lastTapX < size.width / 2f) "«" else "»"
                                            showSkipText = "$dir $totalSkipAmount" + "s"
                                            if (lastTapX < size.width / 2f) { viewModel.skipBackward(10) } else { viewModel.skipForward(10); if (uiState.isNearEnd) skipForwardAfterNearEnd = true }
                                            lastTapTime = now
                                        } else {
                                            totalSkipAmount = 0
                                            lastTapTime = now
                                            lastTapX = down.position.x
                                            val isCenterTap = abs(down.position.x - centerX) < 140f && abs(down.position.y - centerY) < 140f
                                            if (isCenterTap) {
                                                val wasPlaying = uiState.isPlaying
                                                viewModel.togglePlayPause()
                                                showUi = wasPlaying
                                                showCenterPlayPause = true
                                                hideSystemUi()
                                            } else {
                                                showUi = !showUi
                                                hideSystemUi()
                                            }
                                        }
                                    }
                                }
                            }
                    )

                    if (showUi) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp)
                                .align(Alignment.TopCenter)
                                .alpha(uiAlpha)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xE6000000), Color(0x00000000))
                                    )
                                )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    viewModel.savePosition()
                                    navController.popBackStack()
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = Color.White)
                                }

                                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                                    Text(
                                        text = uiState.episode?.anime?.title.orEmpty(),
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    if (uiState.episode != null) {
                                        Text(
                                            text = "Ep. ${formatEpisodeNumber(uiState.episode!!.number)}",
                                            color = Color(0xFFCCCCCC),
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        )
                                    }
                                }

                                IconButton(onClick = { screenLocked = true; showUi = false; showLockOverlay = true; activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED; hideSystemUi() }) {
                                    Icon(Icons.Default.Lock, contentDescription = "Blocca", tint = Color.White)
                                }
                                IconButton(onClick = {
                                    viewModel.savePositionOnly()
                                    enterPiP(context, activity, uiState.isPlaying)
                                    hideSystemUi()
                                }) {
                                    Icon(Icons.Default.PictureInPictureAlt, contentDescription = "PiP", tint = Color.White)
                                }
                            }
                        }
                    }

                    if (showCenterPlayPause) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(96.dp)
                                .background(Color(0x88000000), CircleShape)
                                .clickable {
                                    viewModel.togglePlayPause()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    showSkipText?.let { text ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(Color(0xAA000000), RoundedCornerShape(12.dp))
                                .padding(horizontal = 28.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = text,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }



                    if (screenLocked && showLockOverlay) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x33000000))
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { screenLocked = false; showUi = true; showLockOverlay = false; activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE }
                            ) {
                                Icon(
                                    Icons.Default.LockOpen,
                                    contentDescription = "Sblocca",
                                    tint = Color.White,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }
                    }

                    if (showUi) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(172.dp)
                                .align(Alignment.BottomCenter)
                                .alpha(uiAlpha)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x00000000), Color(0xE6000000))
                                    )
                                )
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatTime(uiState.playerPosition),
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )

                                    val sliderValue = if (uiState.playerDuration > 0)
                                        (uiState.playerPosition.toFloat() / uiState.playerDuration).coerceIn(0f, 1f)
                                    else 0f
                                    var showSliderValue by remember { mutableStateOf(sliderValue) }
                                    var isDragging by remember { mutableStateOf(false) }
                                    LaunchedEffect(sliderValue) {
                                        if (!isDragging) showSliderValue = sliderValue
                                    }
                                    Slider(
                                        value = showSliderValue,
                                        onValueChange = {
                                            isDragging = true
                                            showSliderValue = it
                                        },
                                        onValueChangeFinished = {
                                            isDragging = false
                                            val targetMs = (showSliderValue * uiState.playerDuration).toLong()
                                            viewModel.seekTo(targetMs)
                                        },
                                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.primary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = Color(0x66FFFFFF)
                                        )
                                    )

                                    Text(
                                        text = "-${formatTime(maxOf(0L, uiState.playerDuration - uiState.playerPosition))}",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    IconButton(onClick = { showEpisodeList = true; hideSystemUi() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.List,
                                            contentDescription = "Episodi",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    IconButton(onClick = { viewModel.skipBackward(); hideSystemUi() }) {
                                        Icon(
                                            Icons.Default.SkipPrevious,
                                            contentDescription = "Indietro 10s",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    IconButton(onClick = { viewModel.togglePlayPause(); hideSystemUi() }) {
                                        Icon(
                                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }

                                    IconButton(onClick = { viewModel.skipForward(); hideSystemUi() }) {
                                        Icon(
                                            Icons.Default.SkipNext,
                                            contentDescription = "Avanti 10s",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    var showSpeedMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(onClick = { showSpeedMenu = true; hideSystemUi() }) {
                                            Text(
                                                "${uiState.playbackSpeed}x",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showSpeedMenu,
                                            onDismissRequest = { showSpeedMenu = false; hideSystemUi() }
                                        ) {
                                            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            "${speed}x",
                                                            color = if (uiState.playbackSpeed == speed) MaterialTheme.colorScheme.primary else Color.White
                                                        )
                                                    },
                                                    onClick = {
                                                        viewModel.setPlaybackSpeed(speed)
                                                        showSpeedMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    var showSleepTimerMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(onClick = { showSleepTimerMenu = true; hideSystemUi() }) {
                                            Text(
                                                if (sleepTimer.isActive) "${sleepTimer.remainingSeconds / 60}m" else "\u23F0",
                                                fontSize = 14.sp,
                                                color = if (sleepTimer.isActive) MaterialTheme.colorScheme.primary else Color.White
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showSleepTimerMenu,
                                            onDismissRequest = { showSleepTimerMenu = false; hideSystemUi() }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Disattiva", color = if (!sleepTimer.isActive) MaterialTheme.colorScheme.primary else Color.White) },
                                                onClick = { viewModel.cancelSleepTimer(); showSleepTimerMenu = false }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("30 min", color = if (sleepTimer.isActive && sleepTimer.totalSeconds == 1800) MaterialTheme.colorScheme.primary else Color.White) },
                                                onClick = { viewModel.startSleepTimer(30); showSleepTimerMenu = false }
                                            )
                                        }
                                    }

                                    val resizeModes = listOf(
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT,
                                        AspectRatioFrameLayout.RESIZE_MODE_FILL,
                                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    )
                                    val resizeLabels = listOf("Adatta", "Riempi", "Zoom")
                                    var showResizeMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(onClick = { showResizeMenu = true; hideSystemUi() }) {
                                            Icon(
                                                Icons.Default.AspectRatio,
                                                contentDescription = "Ridimensiona",
                                                tint = Color.White
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showResizeMenu,
                                            onDismissRequest = { showResizeMenu = false; hideSystemUi() }
                                        ) {
                                            resizeModes.forEachIndexed { index, mode ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            resizeLabels[index],
                                                            color = if (uiState.resizeMode == mode) MaterialTheme.colorScheme.primary else Color.White
                                                        )
                                                    },
                                                    onClick = {
                                                        viewModel.setResizeMode(mode)
                                                        showResizeMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(Unit) {
                        snapshotFlow { uiState.autoNavigateToEpisode }
                            .filterNotNull()
                            .first()
                            .let { nextId ->
                                viewModel.savePosition()
                                navController.navigate("player/$animeId/$nextId") {
                                    popUpTo("player/$animeId/$episodeId") { inclusive = true }
                                }
                            }
                    }

                    if (uiState.isNearEnd) {
                        val nextEpisode = viewModel.getNextEpisode()
                        if (nextEpisode != null) {
                            var autoPlayCountdown by remember { mutableStateOf(5) }
                            val autoPlayEnabled = viewModel.isAutoPlayEnabled()

                            LaunchedEffect(autoPlayCountdown, skipForwardAfterNearEnd) {
                                if (skipForwardAfterNearEnd) return@LaunchedEffect
                                if (autoPlayEnabled && autoPlayCountdown > 0) {
                                    delay(1000)
                                    autoPlayCountdown--
                                } else if (autoPlayEnabled && autoPlayCountdown <= 0) {
                                    viewModel.savePosition()
                                    navController.navigate(
                                        "player/$animeId/${nextEpisode.id}"
                                    ) {
                                        popUpTo("player/$animeId/$episodeId") { inclusive = true }
                                    }
                                }
                            }

                            val bottomOffset = if (showUi) 180.dp else 0.dp
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = 16.dp + bottomOffset)
                                    .clickable {
                                        viewModel.savePosition()
                                        navController.navigate(
                                            "player/$animeId/${nextEpisode.id}"
                                        ) {
                                            popUpTo("player/$animeId/$episodeId") { inclusive = true }
                                        }
                                        hideSystemUi()
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Prossimo episodio",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    if (autoPlayEnabled && autoPlayCountdown > 0 && !skipForwardAfterNearEnd) {
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "($autoPlayCountdown)",
                                            color = Color(0xFFFFCC00),
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.SkipNext,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

if (showEpisodeList) {
                val episodes = uiState.anime?.episodes ?: emptyList()
                val currentEpId = episodes.indexOfFirst { it.id == episodeId }
                val sheetState = rememberModalBottomSheetState()
                LaunchedEffect(showEpisodeList) {
                    if (showEpisodeList) sheetState.expand()
                }
                ModalBottomSheet(
                    onDismissRequest = { showEpisodeList = false; hideSystemUi() },
                    containerColor = Color(0xFF1A1A1A),
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) },
                    sheetState = sheetState
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        item {
                            Text(
                                text = "Episodi",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        items(episodes) { ep ->
                            val isCurrent = ep.id == episodeId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!isCurrent) {
                                            showEpisodeList = false
                                            hideSystemUi()
                                            viewModel.savePosition()
                                            navController.navigate("player/$animeId/${ep.id}") {
                                                popUpTo("player/$animeId/$episodeId") { inclusive = true }
                                            }
                                        } else {
                                            showEpisodeList = false
                                            hideSystemUi()
                                        }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ep. ${formatEpisodeNumber(ep.number)}",
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else Color(0xFFCCCCCC),
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = ep.anime?.title.orEmpty(),
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else Color(0xFF999999),
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                                if (isCurrent) {
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        text = "▶",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        item { Spacer(Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val hours = totalSec / 3600
    val minutes = (totalSec / 60) % 60
    val secs = totalSec % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, secs)
    else "%d:%02d".format(minutes, secs)
}

private fun enterPiP(context: Context, activity: Activity?, isPlaying: Boolean) {
    PiPState.enteringPiP = true
    val intent = Intent("PIP_PLAY_PAUSE").setPackage(context.packageName)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val action = RemoteAction(
        Icon.createWithResource(context,
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        ),
        if (isPlaying) "Pausa" else "Play",
        if (isPlaying) "Metti in pausa" else "Riproduci",
        pendingIntent
    )
    val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setSeamlessResizeEnabled(true)
            .setActions(listOf(action))
            .build()
    } else {
        PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setActions(listOf(action))
            .build()
    }
    activity?.enterPictureInPictureMode(params)
    Handler(Looper.getMainLooper()).postDelayed({
        if (activity?.isInPictureInPictureMode != true) {
            PiPState.enteringPiP = false
        }
    }, 500)
}
