package com.playoutedge.player

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.media3.ui.PlayerView
import com.playoutedge.player.config.ConfigState
import com.playoutedge.player.enrollment.EnrollmentScreen
import com.playoutedge.player.enrollment.EnrollmentState
import com.playoutedge.player.enrollment.EnrollmentViewModel
import com.playoutedge.player.kiosk.KioskService
import com.playoutedge.player.overlay.OverlayContainer
import com.playoutedge.player.playback.PlaybackEvent
import com.playoutedge.player.playlist.PlaylistState
import com.playoutedge.player.status.StatusScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var app: PlayerApp
    private lateinit var enrollmentViewModel: EnrollmentViewModel

    // Long press tracking for status screen
    private var lastKeyDownTime = 0L
    private val longPressThreshold = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = playerApp
        enrollmentViewModel = EnrollmentViewModel(
            app.playerApiClient,
            app.tokenStorage,
            app.appVersion
        )

        // Start kiosk service
        KioskService.start(this)

        // Initialize player
        app.playerManager.initialize()

        setContent {
            PlayerContent()
        }

        // Setup playback event handling
        lifecycleScope.launch {
            app.playerManager.playbackEvents.collectLatest { event ->
                when (event) {
                    is PlaybackEvent.Started -> {
                        val config = app.configManager.getCurrentConfig()
                        app.asrunCollector.recordPlayStart(
                            assetId = event.assetId,
                            channelId = config?.channelId,
                            scheduleVersionId = config?.scheduleVersionId
                        )
                    }
                    is PlaybackEvent.Completed -> {
                        val config = app.configManager.getCurrentConfig()
                        app.asrunCollector.recordPlayEnd(
                            assetId = event.assetId,
                            channelId = config?.channelId,
                            durationMs = event.durationMs
                        )
                    }
                    is PlaybackEvent.Skipped -> {
                        val config = app.configManager.getCurrentConfig()
                        app.asrunCollector.recordSkip(
                            assetId = event.assetId,
                            channelId = config?.channelId,
                            reason = event.reason
                        )
                    }
                    is PlaybackEvent.Error -> {
                        app.asrunCollector.recordError(
                            assetId = event.assetId,
                            errorCode = event.code,
                            message = event.message
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun PlayerContent() {
        var isEnrolled by remember { mutableStateOf(false) }
        var showStatusScreen by remember { mutableStateOf(false) }

        // Check enrollment status
        LaunchedEffect(Unit) {
            isEnrolled = app.tokenStorage.isEnrolled()

            if (isEnrolled) {
                startServices()
            }
        }

        // Collect enrollment state changes
        val enrollmentState by enrollmentViewModel.state.collectAsState()
        val code by enrollmentViewModel.code.collectAsState()

        // Collect other states
        val configState by app.configManager.configState.collectAsState()
        val playlistState by app.playlistManager.playlistState.collectAsState()
        val playbackState by app.playerManager.playbackState.collectAsState()
        val overlayState by app.overlaySseClient.overlayState.collectAsState()
        val networkState by app.networkMonitor.networkState.collectAsState(
            initial = app.networkMonitor.getCurrentNetworkState()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            when {
                !isEnrolled -> {
                    EnrollmentScreen(
                        state = enrollmentState,
                        code = code,
                        onCodeChange = { enrollmentViewModel.updateCode(it) },
                        onSubmit = { enrollmentViewModel.submitCode() },
                        onRetry = { enrollmentViewModel.retry() },
                        onComplete = {
                            isEnrolled = true
                            startServices()
                        }
                    )
                }

                showStatusScreen -> {
                    val config = (configState as? ConfigState.Loaded)?.config
                    val playlist = (playlistState as? PlaylistState.Loaded)?.manifest

                    StatusScreen(
                        deviceId = config?.deviceId,
                        config = config,
                        networkState = networkState,
                        playbackState = playbackState,
                        playlist = playlist,
                        appVersion = app.appVersion,
                        onDismiss = { showStatusScreen = false }
                    )
                }

                else -> {
                    // Main playback view
                    PlaybackView()

                    // Overlay on top
                    OverlayContainer(state = overlayState)
                }
            }
        }
    }

    @Composable
    private fun PlaybackView() {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = app.playerManager.getPlayer()
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    private fun startServices() {
        // Start config polling
        app.configManager.startPolling()

        // Start playlist management
        app.playlistManager.start()

        // Start telemetry
        app.heartbeatManager.start()
        app.asrunCollector.start()

        // Start playback
        lifecycleScope.launch {
            app.configManager.configState.collectLatest { state ->
                if (state is ConfigState.Loaded && state.config.channelId != null) {
                    // Start playback when channel is assigned
                    if (app.playlistManager.playlistState.value is PlaylistState.Loaded) {
                        app.playerManager.startPlayback()
                    }

                    // Connect overlay if enabled
                    if (state.config.overlayEnabled) {
                        app.overlaySseClient.connect(state.config.channelId)
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Track long press on menu button for status screen
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_INFO) {
            if (lastKeyDownTime == 0L) {
                lastKeyDownTime = System.currentTimeMillis()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_INFO) {
            val pressDuration = System.currentTimeMillis() - lastKeyDownTime
            lastKeyDownTime = 0L

            if (pressDuration >= longPressThreshold) {
                // Show status screen
                // Would trigger recomposition
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        app.configManager.stopPolling()
        app.playlistManager.stop()
        app.heartbeatManager.stop()
        app.asrunCollector.stop()
        app.overlaySseClient.disconnect()
        app.playerManager.release()
        KioskService.stop(this)
        super.onDestroy()
    }

    // Block back button in kiosk mode
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - kiosk mode
    }
}
