package com.playoutedge.player.telemetry

import kotlinx.serialization.Serializable

/**
 * Device state for heartbeat.
 */
enum class DeviceState {
    PLAYING,
    IDLE,
    ERROR,
    BUFFERING,
    OFFLINE
}

/**
 * Heartbeat payload.
 */
@Serializable
data class HeartbeatPayload(
    val deviceId: String,
    val state: String,
    val currentAssetId: String? = null,
    val currentPosition: Long? = null,
    val errors: List<ErrorInfo> = emptyList(),
    val appVersion: String,
    val uptime: Long
)

/**
 * Error information for telemetry.
 */
@Serializable
data class ErrorInfo(
    val code: String,
    val message: String,
    val timestamp: Long,
    val assetId: String? = null
)

/**
 * As-run event.
 */
@Serializable
data class AsrunEvent(
    val eventType: String,
    val assetId: String? = null,
    val channelId: String? = null,
    val scheduleVersionId: String? = null,
    val timestamp: Long,
    val durationMs: Long? = null,
    val details: Map<String, String>? = null
)

/**
 * Event types.
 */
object AsrunEventType {
    const val PLAY_START = "PLAY_START"
    const val PLAY_END = "PLAY_END"
    const val SKIP = "SKIP"
    const val ERROR = "ERROR"
    const val HEARTBEAT = "HEARTBEAT"
}
