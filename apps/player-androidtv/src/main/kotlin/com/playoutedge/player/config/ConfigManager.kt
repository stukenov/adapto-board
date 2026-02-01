package com.playoutedge.player.config

import com.playoutedge.player.network.PlayerApiClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.min

/**
 * Manages config polling and updates.
 */
class ConfigManager(
    private val apiClient: PlayerApiClient,
    private val configRepository: ConfigRepository,
    private val scope: CoroutineScope
) {
    private val _configState = MutableStateFlow<ConfigState>(ConfigState.Loading)
    val configState: StateFlow<ConfigState> = _configState.asStateFlow()

    private val _configChanges = MutableSharedFlow<ConfigChange>()
    val configChanges: SharedFlow<ConfigChange> = _configChanges.asSharedFlow()

    private var pollingJob: Job? = null
    private var currentBackoffMs = INITIAL_BACKOFF_MS

    companion object {
        private const val INITIAL_BACKOFF_MS = 5_000L
        private const val MAX_BACKOFF_MS = 60_000L
    }

    /**
     * Start config polling.
     */
    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = scope.launch {
            // Load cached config first
            val cached = configRepository.getConfig()
            if (cached != null) {
                _configState.value = ConfigState.Loaded(cached)
            }

            // Start polling loop
            while (isActive) {
                fetchConfig()

                val delayMs = when (val state = _configState.value) {
                    is ConfigState.Loaded -> state.config.pollIntervalSec * 1000L
                    is ConfigState.Error -> currentBackoffMs
                    else -> INITIAL_BACKOFF_MS
                }

                delay(delayMs)
            }
        }
    }

    /**
     * Stop config polling.
     */
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Fetch config immediately.
     */
    suspend fun fetchConfig() {
        val result = apiClient.getConfig()

        result.fold(
            onSuccess = { newConfig ->
                val oldConfig = (configState.value as? ConfigState.Loaded)?.config

                // Save to storage
                configRepository.saveConfig(newConfig)

                // Update state
                _configState.value = ConfigState.Loaded(newConfig)

                // Reset backoff
                currentBackoffMs = INITIAL_BACKOFF_MS

                // Detect and emit changes
                val change = configRepository.detectChanges(oldConfig, newConfig)
                if (change != ConfigChange.NoChange) {
                    _configChanges.emit(change)
                }
            },
            onFailure = { error ->
                val lastKnown = configRepository.getConfig()
                _configState.value = ConfigState.Error(
                    message = error.message ?: "Unknown error",
                    lastKnownConfig = lastKnown
                )

                // Increase backoff
                currentBackoffMs = min(currentBackoffMs * 2, MAX_BACKOFF_MS)
            }
        )
    }

    /**
     * Get current config or null.
     */
    fun getCurrentConfig(): PlayerConfig? = when (val state = configState.value) {
        is ConfigState.Loaded -> state.config
        is ConfigState.Error -> state.lastKnownConfig
        else -> null
    }
}
