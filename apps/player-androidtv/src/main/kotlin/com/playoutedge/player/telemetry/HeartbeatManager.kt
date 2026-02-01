package com.playoutedge.player.telemetry

import android.os.SystemClock
import com.playoutedge.player.config.ConfigManager
import com.playoutedge.player.network.PlayerApiClient
import com.playoutedge.player.network.TokenStorage
import com.playoutedge.player.playback.PlaybackState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Manages periodic heartbeat to server.
 */
class HeartbeatManager(
    private val apiClient: PlayerApiClient,
    private val configManager: ConfigManager,
    private val tokenStorage: TokenStorage,
    private val scope: CoroutineScope,
    private val appVersion: String
) {
    private var heartbeatJob: Job? = null
    private val startTime = SystemClock.elapsedRealtime()

    private val _currentState = MutableStateFlow(DeviceState.IDLE)
    private val _currentAssetId = MutableStateFlow<String?>(null)
    private val _currentPosition = MutableStateFlow<Long?>(null)
    private val _recentErrors = MutableStateFlow<List<ErrorInfo>>(emptyList())

    /**
     * Start heartbeat loop.
     */
    fun start() {
        if (heartbeatJob?.isActive == true) return

        heartbeatJob = scope.launch {
            while (isActive) {
                sendHeartbeat()

                val intervalMs = configManager.getCurrentConfig()?.heartbeatIntervalSec?.times(1000L)
                    ?: 30_000L

                delay(intervalMs)
            }
        }
    }

    /**
     * Stop heartbeat.
     */
    fun stop() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    /**
     * Update current playback state.
     */
    fun updatePlaybackState(state: PlaybackState) {
        when (state) {
            is PlaybackState.Playing -> {
                _currentState.value = DeviceState.PLAYING
                _currentAssetId.value = state.assetId
                _currentPosition.value = state.position
            }
            is PlaybackState.Buffering -> {
                _currentState.value = DeviceState.BUFFERING
            }
            is PlaybackState.Idle -> {
                _currentState.value = DeviceState.IDLE
                _currentAssetId.value = null
            }
            is PlaybackState.Ended -> {
                _currentState.value = DeviceState.IDLE
                _currentAssetId.value = null
            }
            is PlaybackState.Error -> {
                _currentState.value = DeviceState.ERROR
                addError(ErrorInfo(
                    code = state.errorCode,
                    message = state.message,
                    timestamp = System.currentTimeMillis(),
                    assetId = _currentAssetId.value
                ))
            }
        }
    }

    /**
     * Add error to recent errors.
     */
    fun addError(error: ErrorInfo) {
        val current = _recentErrors.value.toMutableList()
        current.add(0, error)
        // Keep last 10 errors
        _recentErrors.value = current.take(10)
    }

    /**
     * Clear errors.
     */
    fun clearErrors() {
        _recentErrors.value = emptyList()
    }

    private suspend fun sendHeartbeat() {
        val deviceId = tokenStorage.getDeviceId() ?: return

        val payload = HeartbeatPayload(
            deviceId = deviceId,
            state = _currentState.value.name,
            currentAssetId = _currentAssetId.value,
            currentPosition = _currentPosition.value,
            errors = _recentErrors.value,
            appVersion = appVersion,
            uptime = SystemClock.elapsedRealtime() - startTime
        )

        val result = apiClient.sendHeartbeat(payload)
        result.onSuccess {
            // Clear errors on successful heartbeat
            clearErrors()
        }
    }
}
