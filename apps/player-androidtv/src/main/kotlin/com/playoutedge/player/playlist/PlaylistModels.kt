package com.playoutedge.player.playlist

import kotlinx.serialization.Serializable

/**
 * Playlist manifest from server.
 */
@Serializable
data class PlaylistManifest(
    val scheduleVersionId: String,
    val channelId: String,
    val items: List<PlaylistItem>,
    val fallbackItem: PlaylistItem? = null,
    val generatedAt: Long
)

/**
 * Single playlist item.
 */
@Serializable
data class PlaylistItem(
    val assetId: String,
    val name: String,
    val url: String,
    val checksum: String,
    val durationMs: Long,
    val orderIndex: Int,
    val timeWindow: TimeWindow? = null
)

/**
 * Time window for scheduled playback.
 */
@Serializable
data class TimeWindow(
    val validFrom: Long? = null,
    val validTo: Long? = null,
    val daysOfWeek: List<Int>? = null, // 1=Monday, 7=Sunday
    val timeStartMinutes: Int? = null, // Minutes from midnight
    val timeEndMinutes: Int? = null
)

/**
 * Playlist state.
 */
sealed class PlaylistState {
    data object Loading : PlaylistState()
    data class Loaded(val manifest: PlaylistManifest, val activeItems: List<PlaylistItem>) : PlaylistState()
    data class Error(val message: String, val lastKnown: PlaylistManifest?) : PlaylistState()
    data object Empty : PlaylistState()
}

/**
 * Calculated play queue item.
 */
data class PlayQueueItem(
    val item: PlaylistItem,
    val isCached: Boolean = false
)
