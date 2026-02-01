package com.playoutedge.server.routes

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.tenantId
import com.playoutedge.server.services.MaintenanceService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceRequest(
    val enabled: Boolean,
    val reason: String? = null,
    val until: String? = null
)

@Serializable
data class MaintenanceResponse(
    val maintenanceMode: Boolean,
    val reason: String?,
    val until: String?
)

fun Route.maintenanceRoutes(maintenanceService: MaintenanceService) {
    authenticate(AUTH_ADMIN) {
        route("/api/admin/tenant/maintenance") {
            // GET /api/admin/tenant/maintenance - Get maintenance status
            get {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val status = maintenanceService.getStatus(tenantId)

                call.respond(MaintenanceResponse(
                    maintenanceMode = status.maintenanceMode,
                    reason = status.reason,
                    until = status.until?.toString()
                ))
            }

            // PUT /api/admin/tenant/maintenance - Toggle maintenance mode
            put {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val request = call.receive<MaintenanceRequest>()

                val status = if (request.enabled) {
                    if (request.reason.isNullOrBlank()) {
                        throw ApiException.badRequest(
                            ErrorCode.VALIDATION_ERROR,
                            "Reason is required when enabling maintenance mode"
                        )
                    }
                    val until = request.until?.let { Instant.parse(it) }
                    maintenanceService.enableMaintenance(tenantId, request.reason, until)
                } else {
                    maintenanceService.disableMaintenance(tenantId)
                }

                call.respond(MaintenanceResponse(
                    maintenanceMode = status.maintenanceMode,
                    reason = status.reason,
                    until = status.until?.toString()
                ))
            }
        }
    }
}
