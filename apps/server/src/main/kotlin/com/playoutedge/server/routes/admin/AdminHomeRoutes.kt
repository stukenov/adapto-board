package com.playoutedge.server.routes.admin

import com.playoutedge.domain.enums.AlertStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AlertFilters
import com.playoutedge.persistence.repositories.AlertRepository
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.home.AlertSummary
import com.playoutedge.server.views.home.FleetHealth
import com.playoutedge.server.views.home.OfflineDevice
import com.playoutedge.server.views.home.homeView
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * Admin home/dashboard routes.
 */
fun Route.adminHomeRoutes(
    deviceRepository: DeviceRepository,
    alertRepository: AlertRepository
) {
    route("/admin") {
        // GET /admin - Dashboard
        get {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)

            // Fetch fleet health
            val devices = deviceRepository.findAll(tenantId)
            val now = Clock.System.now()
            val onlineThreshold = now - 5.minutes

            val onlineDevices = devices.filter { device ->
                device.lastSeenAt?.let { it >= onlineThreshold } ?: false
            }
            val offlineDevices = devices.filter { device ->
                device.lastSeenAt?.let { it < onlineThreshold } ?: true
            }

            val fleetHealth = FleetHealth(
                onlineCount = onlineDevices.size,
                totalCount = devices.size,
                offlineDevices = offlineDevices.map { device ->
                    OfflineDevice(
                        id = device.id.value,
                        name = device.displayName,
                        lastSeen = device.lastSeenAt
                    )
                }
            )

            // Fetch alerts
            val alertFilters = AlertFilters(
                status = AlertStatus.OPEN,
                limit = 10
            )
            val alertEntities = alertRepository.findByTenant(tenantId, alertFilters)
            val alerts = alertEntities.map { alert ->
                AlertSummary(
                    id = alert.id.value,
                    type = alert.type.name,
                    message = alert.payloadJson.toString(),
                    createdAt = alert.firstSeenAt
                )
            }

            call.respondHtml {
                homeView(
                    session = session,
                    fleetHealth = fleetHealth,
                    alerts = alerts
                )
            }
        }

        // Redirect /admin/ to /admin
        get("/") {
            call.respondRedirect("/admin")
        }
    }
}
