package com.playoutedge.player.playlist

import com.playoutedge.player.config.ConfigChange
import com.playoutedge.player.config.ConfigManager
import com.playoutedge.player.network.PlayerApiClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Manages playlist fetching and calculation.
 */
class PlaylistManager(
    private val apiClient: PlayerApiClient,
    private val playlistRepository: PlaylistRepository,
    private val configManager: ConfigManager,
    private val scope: CoroutineScope
) {
    private val calculator = PlaylistCalculator()

    private val _playlistState = MutableStateFlow<PlaylistState>(PlaylistState.Loading)
    val playlistState: StateFlow<PlaylistState> = _playlistState.asStateFlow()

    private val _currentItem = MutableStateFlow<PlaylistItem?>(null)
    val currentItem: StateFlow<PlaylistItem?> = _currentItem.asStateFlow()

    private var refreshJob: Job? = null

    init {
        // Listen for config changes
        scope.launch {
            configManager.configChanges.collect { change ->
                when (change) {
                    is ConfigChange.ChannelChanged,
                    is ConfigChange.ScheduleVersionChanged -> fetchPlaylist()
                    else -> {}
                }
            }
        }
    }

    /**
     * Start playlist management.
     */
    fun start() {
        scope.launch {
            // Load cached playlist
            val cached = playlistRepository.getManifest()
            if (cached != null) {
                val activeItems = calculator.getActiveItems(cached)
                _playlistState.value = PlaylistState.Loaded(cached, activeItems)
            }

            // Fetch fresh playlist
            fetchPlaylist()

            // Periodic time window recalculation
            startTimeWindowRefresh()
        }
    }

    /**
     * Fetch playlist from server.
     */
    suspend fun fetchPlaylist() {
        val result = apiClient.getPlaylist()

        result.fold(
            onSuccess = { manifest ->
                playlistRepository.saveManifest(manifest)
                val activeItems = calculator.getActiveItems(manifest)
                _playlistState.value = if (activeItems.isNotEmpty()) {
                    PlaylistState.Loaded(manifest, activeItems)
                } else {
                    PlaylistState.Empty
                }
            },
            onFailure = { error ->
                val lastKnown = playlistRepository.getManifest()
                _playlistState.value = PlaylistState.Error(
                    message = error.message ?: "Unknown error",
                    lastKnown = lastKnown
                )
            }
        )
    }

    /**
     * Get next item to play.
     */
    fun getNextItem(): PlaylistItem? {
        val state = playlistState.value
        if (state !is PlaylistState.Loaded) return null

        val next = calculator.getNextItem(state.activeItems, _currentItem.value)
        _currentItem.value = next
        return next
    }

    /**
     * Mark item as currently playing.
     */
    fun setCurrentItem(item: PlaylistItem) {
        _currentItem.value = item
    }

    /**
     * Get active items list.
     */
    fun getActiveItems(): List<PlaylistItem> {
        val state = playlistState.value
        return if (state is PlaylistState.Loaded) state.activeItems else emptyList()
    }

    /**
     * Refresh active items based on time windows.
     */
    private fun startTimeWindowRefresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            while (isActive) {
                delay(60_000) // Check every minute

                val state = playlistState.value
                if (state is PlaylistState.Loaded) {
                    val newActiveItems = calculator.getActiveItems(state.manifest)
                    if (newActiveItems != state.activeItems) {
                        _playlistState.value = state.copy(activeItems = newActiveItems)
                    }
                }
            }
        }
    }

    fun stop() {
        refreshJob?.cancel()
    }
}
