package com.playoutedge.server.routes

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.domain.enums.AlertStatus
import com.playoutedge.domain.enums.AlertType
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.tenantId
import com.playoutedge.server.services.AlertService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.util.UUID

@Serializable
data class AlertResponse(
    val id: String,
    val type: String,
    val status: String,
    val payload: JsonObject,
    val firstSeenAt: String,
    val lastSeenAt: String,
    val resolvedAt: String?
)

@Serializable
data class AlertsQueryResponse(
    val alerts: List<AlertResponse>,
    val total: Long,
    val limit: Int,
    val offset: Int
)

fun Route.alertsRoutes(alertService: AlertService) {
    authenticate(AUTH_ADMIN) {
        route("/api/admin/alerts") {
            // GET /api/admin/alerts - List alerts
            get {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val status = call.request.queryParameters["status"]?.let {
                    try {
                        AlertStatus.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                val type = call.request.queryParameters["type"]?.let {
                    try {
                        AlertType.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                val result = alertService.query(
                    tenantId = tenantId,
                    status = status,
                    type = type,
                    limit = limit.coerceIn(1, 100),
                    offset = offset.coerceAtLeast(0)
                )

                call.respond(AlertsQueryResponse(
                    alerts = result.alerts.map { alert ->
                        AlertResponse(
                            id = alert.id.value.toString(),
                            type = alert.type.name,
                            status = alert.status.name,
                            payload = alert.payloadJson,
                            firstSeenAt = alert.firstSeenAt.toString(),
                            lastSeenAt = alert.lastSeenAt.toString(),
                            resolvedAt = alert.resolvedAt?.toString()
                        )
                    },
                    total = result.total,
                    limit = result.limit,
                    offset = result.offset
                ))
            }

            // POST /api/admin/alerts/{id}/ack - Acknowledge alert
            post("/{id}/ack") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val alertId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid alert ID")

                val alert = alertService.acknowledge(tenantId, alertId)
                    ?: throw ApiException.notFound(ErrorCode.NOT_FOUND, "Alert not found")

                call.respond(AlertResponse(
                    id = alert.id.value.toString(),
                    type = alert.type.name,
                    status = alert.status.name,
                    payload = alert.payloadJson,
                    firstSeenAt = alert.firstSeenAt.toString(),
                    lastSeenAt = alert.lastSeenAt.toString(),
                    resolvedAt = alert.resolvedAt?.toString()
                ))
            }

            // POST /api/admin/alerts/{id}/resolve - Resolve alert
            post("/{id}/resolve") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val alertId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid alert ID")

                val alert = alertService.resolve(tenantId, alertId)
                    ?: throw ApiException.notFound(ErrorCode.NOT_FOUND, "Alert not found")

                call.respond(AlertResponse(
                    id = alert.id.value.toString(),
                    type = alert.type.name,
                    status = alert.status.name,
                    payload = alert.payloadJson,
                    firstSeenAt = alert.firstSeenAt.toString(),
                    lastSeenAt = alert.lastSeenAt.toString(),
                    resolvedAt = alert.resolvedAt?.toString()
                ))
            }
        }
    }
}
