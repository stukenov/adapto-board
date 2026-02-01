package com.playoutedge.player.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.playoutedge.player.playlist.PlaylistItem
import com.playoutedge.player.playlist.PlaylistManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Manages ExoPlayer for video playback with fallback hierarchy.
 */
class PlayerManager(
    private val context: Context,
    private val playlistManager: PlaylistManager,
    private val scope: CoroutineScope
) {
    private var exoPlayer: ExoPlayer? = null
    private var currentItem: PlaylistItem? = null
    private var playStartTime: Long = 0

    // Fallback hierarchy tracking
    private var consecutiveErrors = 0
    private var isInFallbackMode = false
    private val maxConsecutiveErrors = 3

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _playbackEvents = MutableSharedFlow<PlaybackEvent>()
    val playbackEvents: SharedFlow<PlaybackEvent> = _playbackEvents.asSharedFlow()

    // Fallback content priority (highest to lowest)
    private enum class FallbackLevel {
        PLAYLIST,           // Normal playlist playback
        CACHED_CONTENT,     // Locally cached content
        STATIC_FALLBACK,    // Static fallback image/video
        BLACK_SCREEN        // Last resort - black screen
    }
    private var currentFallbackLevel = FallbackLevel.PLAYLIST

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    currentItem?.let {
                        _playbackState.value = PlaybackState.Buffering(it.assetId)
                    }
                }
                Player.STATE_READY -> {
                    currentItem?.let { item ->
                        exoPlayer?.let { player ->
                            _playbackState.value = PlaybackState.Playing(
                                assetId = item.assetId,
                                position = player.currentPosition,
                                duration = player.duration
                            )
                        }
                    }
                }
                Player.STATE_ENDED -> {
                    onItemEnded()
                }
                Player.STATE_IDLE -> {
                    _playbackState.value = PlaybackState.Idle
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val errorCode = "PLAYBACK_ERROR_${error.errorCode}"
            val message = error.message ?: "Unknown playback error"

            consecutiveErrors++

            _playbackState.value = PlaybackState.Error(
                errorCode = errorCode,
                message = message,
                assetId = currentItem?.assetId
            )

            scope.launch {
                _playbackEvents.emit(PlaybackEvent.Error(
                    assetId = currentItem?.assetId,
                    code = errorCode,
                    message = message
                ))
            }

            // Check if we should enter fallback mode
            if (consecutiveErrors >= maxConsecutiveErrors) {
                enterFallbackMode()
            } else {
                // Skip to next on error
                playNext()
            }
        }
    }

    /**
     * Initialize player.
     */
    fun initialize() {
        if (exoPlayer != null) return

        exoPlayer = ExoPlayer.Builder(context)
            .build()
            .also { player ->
                player.addListener(playerListener)
                player.playWhenReady = true
                player.repeatMode = Player.REPEAT_MODE_OFF
            }
    }

    /**
     * Get the ExoPlayer instance for UI binding.
     */
    fun getPlayer(): ExoPlayer? = exoPlayer

    /**
     * Start playback loop.
     */
    fun startPlayback() {
        playNext()
    }

    /**
     * Play next item in playlist with fallback hierarchy.
     */
    fun playNext() {
        val nextItem = playlistManager.getNextItem()

        if (nextItem != null) {
            // Reset fallback state on successful playlist item
            if (currentFallbackLevel != FallbackLevel.PLAYLIST) {
                currentFallbackLevel = FallbackLevel.PLAYLIST
                isInFallbackMode = false
            }
            consecutiveErrors = 0
            playItem(nextItem)
            return
        }

        // No playlist items - enter fallback hierarchy
        enterFallbackMode()
    }

    /**
     * Enter fallback mode with hierarchical recovery.
     */
    private fun enterFallbackMode() {
        isInFallbackMode = true

        when (currentFallbackLevel) {
            FallbackLevel.PLAYLIST -> {
                // Try cached content next
                currentFallbackLevel = FallbackLevel.CACHED_CONTENT
                playCachedContent()
            }
            FallbackLevel.CACHED_CONTENT -> {
                // Try static fallback
                currentFallbackLevel = FallbackLevel.STATIC_FALLBACK
                playStaticFallback()
            }
            FallbackLevel.STATIC_FALLBACK -> {
                // Last resort - black screen
                currentFallbackLevel = FallbackLevel.BLACK_SCREEN
                showBlackScreen()
            }
            FallbackLevel.BLACK_SCREEN -> {
                // Already at lowest level, retry playlist periodically
                schedulePlaylistRetry()
            }
        }
    }

    /**
     * Try to play cached content.
     */
    private fun playCachedContent() {
        // TODO: Integrate with cache manager when implemented
        // For now, skip to next fallback level
        enterFallbackMode()
    }

    /**
     * Play static fallback content.
     */
    private fun playStaticFallback() {
        // Use bundled fallback asset or configured fallback URL
        // For now, move to black screen
        _playbackState.value = PlaybackState.Idle
        enterFallbackMode()
    }

    /**
     * Show black screen as last resort.
     */
    private fun showBlackScreen() {
        exoPlayer?.stop()
        _playbackState.value = PlaybackState.Idle
        schedulePlaylistRetry()
    }

    /**
     * Schedule periodic retry to check for playlist availability.
     */
    private fun schedulePlaylistRetry() {
        scope.launch {
            delay(30_000) // Retry every 30 seconds
            val nextItem = playlistManager.getNextItem()
            if (nextItem != null) {
                currentFallbackLevel = FallbackLevel.PLAYLIST
                isInFallbackMode = false
                playItem(nextItem)
            } else if (isInFallbackMode) {
                schedulePlaylistRetry()
            }
        }
    }

    /**
     * Play specific item.
     */
    fun playItem(item: PlaylistItem) {
        val player = exoPlayer ?: return

        // Record end of previous item
        currentItem?.let { prev ->
            val duration = System.currentTimeMillis() - playStartTime
            scope.launch {
                _playbackEvents.emit(PlaybackEvent.Completed(prev.assetId, duration))
            }
        }

        // Set new item
        currentItem = item
        playStartTime = System.currentTimeMillis()
        playlistManager.setCurrentItem(item)

        // Emit start event
        scope.launch {
            _playbackEvents.emit(PlaybackEvent.Started(item.assetId))
        }

        // Play
        val mediaItem = MediaItem.fromUri(item.url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    /**
     * Skip current item.
     */
    fun skip(reason: String = "user_skip") {
        currentItem?.let { item ->
            scope.launch {
                _playbackEvents.emit(PlaybackEvent.Skipped(item.assetId, reason))
            }
        }
        playNext()
    }

    /**
     * Pause playback.
     */
    fun pause() {
        exoPlayer?.pause()
    }

    /**
     * Resume playback.
     */
    fun resume() {
        exoPlayer?.play()
    }

    /**
     * Handle item ended.
     */
    private fun onItemEnded() {
        currentItem?.let { item ->
            val duration = System.currentTimeMillis() - playStartTime
            scope.launch {
                _playbackEvents.emit(PlaybackEvent.Completed(item.assetId, duration))
            }
        }
        playNext()
    }

    /**
     * Release player.
     */
    fun release() {
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }

    /**
     * Get current position.
     */
    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0

    /**
     * Get current duration.
     */
    fun getDuration(): Long = exoPlayer?.duration ?: 0
}
