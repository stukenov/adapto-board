package com.playoutedge.player.playback

/**
 * Playback state.
 */
sealed class PlaybackState {
    data object Idle : PlaybackState()

    data class Playing(
        val assetId: String,
        val position: Long,
        val duration: Long
    ) : PlaybackState()

    data class Buffering(
        val assetId: String
    ) : PlaybackState()

    data class Error(
        val errorCode: String,
        val message: String,
        val assetId: String? = null
    ) : PlaybackState()

    data object Ended : PlaybackState()
}

/**
 * Playback event.
 */
sealed class PlaybackEvent {
    data class Started(val assetId: String) : PlaybackEvent()
    data class Completed(val assetId: String, val durationMs: Long) : PlaybackEvent()
    data class Skipped(val assetId: String, val reason: String) : PlaybackEvent()
    data class Error(val assetId: String?, val code: String, val message: String) : PlaybackEvent()
}
