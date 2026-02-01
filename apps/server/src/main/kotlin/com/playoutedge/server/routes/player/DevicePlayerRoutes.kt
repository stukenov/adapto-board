package com.playoutedge.server.routes.player

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.domain.enums.ActionStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.server.plugins.AUTH_DEVICE
import com.playoutedge.server.plugins.deviceClaims
import com.playoutedge.server.services.CompatibilityService
import com.playoutedge.server.services.DeviceActionService
import java.util.UUID
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class PendingAction(
    val id: String,
    val action: String,
    val params: JsonObject?
)

@Serializable
data class DeviceConfigResponse(
    val deviceId: String,
    val channelId: String?,
    val heartbeatIntervalSeconds: Int,
    val configPollIntervalSeconds: Int,
    val playlistPollIntervalSeconds: Int,
    val pendingActions: List<PendingAction> = emptyList(),
    // Version compatibility
    val serverVersion: String? = null,
    val apiVersion: String? = null,
    val minSupportedPlayerVersion: String? = null,
    val recommendedPlayerVersion: String? = null,
    val forceUpdateRequired: Boolean = false,
    val forceUpdateDeadline: String? = null,
    // Feature flags
    val features: Map<String, Boolean> = emptyMap(),
    val deprecationWarnings: List<String> = emptyList()
)

@Serializable
data class AckActionRequest(
    val status: String
)

@Serializable
data class AckActionResponse(
    val acknowledged: Boolean
)

@Serializable
data class HeartbeatRequest(
    val currentAssetId: String? = null,
    val playerState: String? = null,
    val appVersion: String? = null
)

@Serializable
data class HeartbeatResponse(
    val acknowledged: Boolean,
    val serverTime: String
)

fun Route.devicePlayerRoutes(
    deviceRepo: DeviceRepository,
    deviceActionService: DeviceActionService,
    compatibilityService: CompatibilityService = CompatibilityService()
) {
    authenticate(AUTH_DEVICE) {
        route("/api/player") {
            // GET /api/player/config - Get device configuration
            get("/config") {
                val deviceClaims = call.deviceClaims ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing device claims"
                )

                val tenantId = TenantId(deviceClaims.tenantId)
                val device = deviceRepo.findById(tenantId, deviceClaims.subject)
                    ?: throw ApiException.notFound(ErrorCode.DEVICE_NOT_FOUND, "Device not found")

                val pendingActions = deviceActionService.getPendingActions(tenantId, deviceClaims.subject)
                val versionInfo = compatibilityService.getVersionInfo()
                val featureFlags = compatibilityService.getFeatureFlags(tenantId.value)

                call.respond(DeviceConfigResponse(
                    deviceId = device.id.value.toString(),
                    channelId = device.assignedChannelId?.toString(),
                    heartbeatIntervalSeconds = 30,
                    configPollIntervalSeconds = 300,
                    playlistPollIntervalSeconds = 60,
                    pendingActions = pendingActions.map { action ->
                        PendingAction(
                            id = action.id.value.toString(),
                            action = action.action.name,
                            params = action.paramsJson
                        )
                    },
                    serverVersion = versionInfo.serverVersion,
                    apiVersion = versionInfo.apiVersion,
                    minSupportedPlayerVersion = versionInfo.minSupportedPlayerVersion,
                    recommendedPlayerVersion = versionInfo.recommendedPlayerVersion,
                    forceUpdateRequired = versionInfo.forceUpdateRequired,
                    forceUpdateDeadline = versionInfo.forceUpdateDeadline,
                    features = featureFlags.flags,
                    deprecationWarnings = versionInfo.deprecationWarnings
                ))
            }

            // POST /api/player/heartbeat - Report heartbeat
            post("/heartbeat") {
                val deviceClaims = call.deviceClaims ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing device claims"
                )

                val request = call.receive<HeartbeatRequest>()
                val tenantId = TenantId(deviceClaims.tenantId)

                val device = deviceRepo.updateHeartbeat(
                    tenantId,
                    deviceClaims.subject,
                    request.appVersion
                ) ?: throw ApiException.notFound(ErrorCode.DEVICE_NOT_FOUND, "Device not found")

                call.respond(HeartbeatResponse(
                    acknowledged = true,
                    serverTime = kotlinx.datetime.Clock.System.now().toString()
                ))
            }

            // POST /api/player/actions/{id}/ack - Acknowledge action
            post("/actions/{id}/ack") {
                val deviceClaims = call.deviceClaims ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing device claims"
                )

                val actionId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid action ID")

                val request = call.receive<AckActionRequest>()
                val status = try {
                    ActionStatus.valueOf(request.status)
                } catch (e: IllegalArgumentException) {
                    throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid status")
                }

                if (status !in listOf(ActionStatus.ACKED, ActionStatus.FAILED)) {
                    throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Status must be ACKED or FAILED")
                }

                val action = deviceActionService.acknowledgeAction(actionId, status)
                    ?: throw ApiException.notFound(ErrorCode.ACTION_NOT_FOUND, "Action not found or already processed")

                call.respond(AckActionResponse(acknowledged = true))
            }
        }
    }
}
