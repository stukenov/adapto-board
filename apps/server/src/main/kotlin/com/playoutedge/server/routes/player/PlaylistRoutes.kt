package com.playoutedge.server.routes.player

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.server.services.PlaylistService
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.server.plugins.AUTH_DEVICE
import com.playoutedge.server.plugins.deviceClaims
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistItemDto(
    val assetId: String,
    val url: String,
    val checksum: String?,
    val durationMs: Int?,
    val orderIndex: Int,
    val validFrom: String?,
    val validTo: String?,
    val daysOfWeek: Int?,
    val timeStart: String?,
    val timeEnd: String?
)

@Serializable
data class PlaylistManifestResponse(
    val scheduleVersionId: String,
    val version: Int,
    val items: List<PlaylistItemDto>,
    val fallbackAsset: PlaylistItemDto?
)

fun Route.playlistRoutes(playlistService: PlaylistService) {
    authenticate(AUTH_DEVICE) {
        route("/api/player") {
            // GET /api/player/playlist - Get playlist manifest
            get("/playlist") {
                val deviceClaims = call.deviceClaims ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing device claims"
                )

                val channelId = deviceClaims.channelId ?: throw ApiException.badRequest(
                    ErrorCode.DEVICE_NO_CHANNEL,
                    "Device has no assigned channel"
                )

                val tenantId = TenantId(deviceClaims.tenantId)

                val manifest = playlistService.getManifest(tenantId, channelId)
                    ?: throw ApiException.notFound(
                        ErrorCode.SCHEDULE_NOT_FOUND,
                        "No active schedule for channel"
                    )

                call.respond(PlaylistManifestResponse(
                    scheduleVersionId = manifest.scheduleVersionId.toString(),
                    version = manifest.version,
                    items = manifest.items.map { item ->
                        PlaylistItemDto(
                            assetId = item.assetId.toString(),
                            url = item.url,
                            checksum = item.checksum,
                            durationMs = item.durationMs,
                            orderIndex = item.orderIndex,
                            validFrom = item.validFrom?.toString(),
                            validTo = item.validTo?.toString(),
                            daysOfWeek = item.daysOfWeek,
                            timeStart = item.timeStart?.toString(),
                            timeEnd = item.timeEnd?.toString()
                        )
                    },
                    fallbackAsset = manifest.fallbackAsset?.let { item ->
                        PlaylistItemDto(
                            assetId = item.assetId.toString(),
                            url = item.url,
                            checksum = item.checksum,
                            durationMs = item.durationMs,
                            orderIndex = item.orderIndex,
                            validFrom = item.validFrom?.toString(),
                            validTo = item.validTo?.toString(),
                            daysOfWeek = item.daysOfWeek,
                            timeStart = item.timeStart?.toString(),
                            timeEnd = item.timeEnd?.toString()
                        )
                    }
                ))
            }
        }
    }
}
