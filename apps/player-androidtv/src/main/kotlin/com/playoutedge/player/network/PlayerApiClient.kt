package com.playoutedge.player.network

import com.playoutedge.player.config.PlayerConfig
import com.playoutedge.player.playlist.PlaylistManifest
import com.playoutedge.player.telemetry.AsrunEvent
import com.playoutedge.player.telemetry.HeartbeatPayload
import kotlinx.serialization.Serializable

/**
 * Player-specific API client.
 */
class PlayerApiClient(private val apiClient: ApiClient) {

    // === Enrollment ===

    suspend fun enroll(request: EnrollRequest): Result<EnrollResponse> =
        apiClient.postPublic("/api/device/enroll", request)

    suspend fun refreshToken(refreshToken: String): Result<TokenResponse> =
        apiClient.postPublic("/api/device/refresh", RefreshRequest(refreshToken))

    // === Config ===

    suspend fun getConfig(): Result<PlayerConfig> =
        apiClient.get("/api/player/config")

    // === Playlist ===

    suspend fun getPlaylist(): Result<PlaylistManifest> =
        apiClient.get("/api/player/playlist")

    // === Telemetry ===

    suspend fun sendHeartbeat(payload: HeartbeatPayload): Result<HeartbeatResponse> =
        apiClient.post("/api/player/heartbeat", payload)

    suspend fun sendAsrunBatch(events: List<AsrunEvent>): Result<AsrunBatchResponse> =
        apiClient.post("/api/player/asrun", AsrunBatchRequest(events))

    // === Overlay ===

    suspend fun getOverlayStream(channelId: String) =
        apiClient.getStream("/api/player/overlay/stream?channelId=$channelId")
}

// === Request/Response DTOs ===

@Serializable
data class EnrollRequest(
    val code: String,
    val deviceInfo: DeviceInfo
)

@Serializable
data class DeviceInfo(
    val model: String,
    val androidVersion: String,
    val appVersion: String
)

@Serializable
data class EnrollResponse(
    val deviceId: String,
    val tenantId: String,
    val accessToken: String,
    val refreshToken: String,
    val channelId: String? = null
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class HeartbeatResponse(
    val ok: Boolean,
    val nextPollMs: Long? = null
)

@Serializable
data class AsrunBatchRequest(
    val events: List<AsrunEvent>
)

@Serializable
data class AsrunBatchResponse(
    val accepted: Int
)
