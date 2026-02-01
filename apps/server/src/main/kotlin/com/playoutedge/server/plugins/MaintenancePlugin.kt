package com.playoutedge.server.plugins

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.server.services.MaintenanceService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

class MaintenancePluginConfig {
    var maintenanceService: MaintenanceService? = null
}

val MaintenancePlugin = createApplicationPlugin(name = "MaintenancePlugin", ::MaintenancePluginConfig) {
    val service = pluginConfig.maintenanceService
        ?: error("MaintenanceService not configured")

    // Paths that should be blocked in maintenance mode
    val blockedPathPrefixes = listOf(
        "/api/admin/channels",
        "/api/admin/schedules",
        "/api/admin/overlays",
        "/api/admin/devices"
    )

    // Methods that should be blocked (mutating operations)
    val blockedMethods = setOf(
        HttpMethod.Post,
        HttpMethod.Put,
        HttpMethod.Patch,
        HttpMethod.Delete
    )

    // Paths that are always allowed
    val allowedPaths = listOf(
        "/api/admin/tenant/maintenance",
        "/api/admin/auth",
        "/api/player",
        "/api/device"
    )

    onCall { call ->
        val path = call.request.local.uri
        val method = call.request.local.method

        // Skip maintenance check for allowed paths
        if (allowedPaths.any { path.startsWith(it) }) {
            return@onCall
        }

        // Skip if not a mutating method
        if (method !in blockedMethods) {
            return@onCall
        }

        // Skip if path is not in blocked prefixes
        if (blockedPathPrefixes.none { path.startsWith(it) }) {
            return@onCall
        }

        // Check maintenance mode
        val tenantId = call.tenantId ?: return@onCall

        if (service.isInMaintenance(tenantId)) {
            val status = service.getStatus(tenantId)
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                mapOf(
                    "error" to "Tenant is in maintenance mode",
                    "reason" to status.reason,
                    "until" to status.until?.toString()
                )
            )
        }
    }
}
