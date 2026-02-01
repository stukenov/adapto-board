package com.playoutedge.server.routes.player

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.domain.enums.AsrunEventType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AsrunEventInput
import com.playoutedge.server.plugins.AUTH_DEVICE
import com.playoutedge.server.plugins.deviceClaims
import com.playoutedge.server.services.AsrunService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.util.UUID

@Serializable
data class AsrunEventDto(
    val channelId: String? = null,
    val scheduleVersionId: String? = null,
    val assetId: String? = null,
    val eventType: String,
    val at: String,
    val details: JsonObject? = null
)

@Serializable
data class AsrunBatchRequest(
    val events: List<AsrunEventDto>
)

@Serializable
data class AsrunBatchResponse(
    val accepted: Int
)

fun Route.playerAsrunRoutes(asrunService: AsrunService) {
    authenticate(AUTH_DEVICE) {
        route("/api/player") {
            // POST /api/player/asrun - Batch upload as-run events
            post("/asrun") {
                val deviceClaims = call.deviceClaims ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing device claims"
                )

                val tenantId = TenantId(deviceClaims.tenantId)
                val deviceId = deviceClaims.subject

                val request = call.receive<AsrunBatchRequest>()

                if (request.events.isEmpty()) {
                    call.respond(AsrunBatchResponse(accepted = 0))
                    return@post
                }

                if (request.events.size > 100) {
                    throw ApiException.badRequest(
                        ErrorCode.VALIDATION_ERROR,
                        "Maximum 100 events per batch"
                    )
                }

                val inputs = request.events.map { event ->
                    val eventType = try {
                        AsrunEventType.valueOf(event.eventType.uppercase())
                    } catch (e: IllegalArgumentException) {
                        throw ApiException.badRequest(
                            ErrorCode.VALIDATION_ERROR,
                            "Invalid event type: ${event.eventType}. Valid types: PLAY_START, PLAY_END, HEARTBEAT, SKIP, ERROR"
                        )
                    }

                    val at = try {
                        Instant.parse(event.at)
                    } catch (e: Exception) {
                        throw ApiException.badRequest(
                            ErrorCode.VALIDATION_ERROR,
                            "Invalid timestamp: ${event.at}"
                        )
                    }

                    AsrunEventInput(
                        tenantId = tenantId,
                        deviceId = deviceId,
                        channelId = event.channelId?.let { UUID.fromString(it) },
                        scheduleVersionId = event.scheduleVersionId?.let { UUID.fromString(it) },
                        assetId = event.assetId?.let { UUID.fromString(it) },
                        eventType = eventType,
                        at = at,
                        details = event.details
                    )
                }

                val accepted = asrunService.recordBatch(inputs)
                call.respond(AsrunBatchResponse(accepted = accepted))
            }
        }
    }
}
