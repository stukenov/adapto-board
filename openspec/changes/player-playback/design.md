# Player Playback — Design

## Context

Android TV player должен обеспечивать 24/7 воспроизведение с инвариантом "никогда чёрный экран". Работа в плохих сетях и с ненадёжным контентом.

## Goals / Non-Goals

**Goals:**
- Непрерывный playback loop
- Graceful error recovery (skip and continue)
- Offline playback из cache
- Fallback hierarchy
- Playback events для as-run

**Non-Goals:**
- Complex transitions (crossfade, etc.)
- Live streaming
- DRM (v1)

## Decisions

### Decision 1: ExoPlayer/Media3 Setup

```kotlin
@Singleton
class PlaybackManager @Inject constructor(
    private val context: Context,
    private val cacheManager: CacheManager,
    private val playlistManager: PlaylistManager,
    private val eventEmitter: PlaybackEventEmitter
) {
    private var exoPlayer: ExoPlayer? = null

    fun initialize() {
        exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context)
                    .setDataSourceFactory(cacheManager.dataSourceFactory)
            )
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
                addListener(playbackListener)
            }
    }
}
```

### Decision 2: Playback Loop Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    PlaybackManager                            │
│  ┌────────────────────────────────────────────────────────┐  │
│  │                  State Machine                          │  │
│  │                                                         │  │
│  │   IDLE ──▶ LOADING ──▶ PLAYING ──▶ ERROR ──▶ RECOVERY  │  │
│  │    ▲                       │          │         │       │  │
│  │    └───────────────────────┴──────────┴─────────┘       │  │
│  └────────────────────────────────────────────────────────┘  │
│                              │                                │
│         ┌────────────────────┼────────────────────┐          │
│         ▼                    ▼                    ▼          │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐   │
│  │  ExoPlayer   │    │   Playlist   │    │    Event     │   │
│  │              │    │   Navigator  │    │   Emitter    │   │
│  └──────────────┘    └──────────────┘    └──────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

### Decision 3: Error Recovery Strategy

```kotlin
private val playbackListener = object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        Log.e(TAG, "Playback error: ${error.message}")
        eventEmitter.emit(PlaybackEvent.Error(currentItem, error))

        // Skip to next item
        val nextIndex = (currentIndex + 1) % playlist.size
        if (nextIndex != currentIndex) {
            playItem(nextIndex)
        } else {
            // All items failed - show fallback
            showFallback()
        }
    }
}
```

**Recovery order:**
1. Skip failed item → try next
2. If all items fail → show fallback screen
3. Periodically retry failed items

### Decision 4: Fallback Hierarchy Implementation

```kotlin
class FallbackManager(
    private val context: Context,
    private val cacheManager: CacheManager
) {
    suspend fun getFallbackSource(): MediaSource? {
        // 1. Try server-provided fallback from last config
        cacheManager.getCachedFallback()?.let { return it }

        // 2. Try any cached working asset
        cacheManager.getAnyCachedAsset()?.let { return it }

        // 3. Use embedded fallback
        return createLocalFallbackSource()
    }

    private fun createLocalFallbackSource(): MediaSource {
        // Static image/animation from assets
        return ProgressiveMediaSource.Factory(
            AssetDataSource.Factory(context)
        ).createMediaSource(
            MediaItem.fromUri("asset:///fallback.png")
        )
    }
}
```

### Decision 5: Playback Events

```kotlin
sealed class PlaybackEvent {
    data class Start(
        val assetId: String,
        val scheduleVersionId: String,
        val timestamp: Instant
    ) : PlaybackEvent()

    data class End(
        val assetId: String,
        val durationPlayed: Long,
        val completedNaturally: Boolean,
        val timestamp: Instant
    ) : PlaybackEvent()

    data class Error(
        val assetId: String?,
        val errorCode: String,
        val errorMessage: String,
        val timestamp: Instant
    ) : PlaybackEvent()
}
```

Events собираются в `AsrunCollector` для batch upload.

### Decision 6: Playlist Navigation

```kotlin
class PlaylistNavigator(
    private val timeWindowCalculator: TimeWindowCalculator
) {
    fun getActiveItems(playlist: Playlist, now: Instant): List<PlaylistItem> {
        return playlist.items
            .filter { timeWindowCalculator.isActive(it, now) }
            .sortedBy { it.orderIndex }
    }

    fun getNextItem(current: PlaylistItem, activeItems: List<PlaylistItem>): PlaylistItem {
        val currentIndex = activeItems.indexOf(current)
        val nextIndex = (currentIndex + 1) % activeItems.size
        return activeItems[nextIndex]
    }
}
```

## Trade-offs

- **Skip vs Retry on error:** Skip immediately for UX; retry in background
- **Time window filtering:** Client-side для responsiveness; server authoritative
- **Embedded fallback:** Guarantees never black screen; increases APK size slightly
