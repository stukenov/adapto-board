package com.playoutedge.server.routes.admin

import com.playoutedge.domain.enums.DeviceEnrollStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.ChannelRepository
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.persistence.repositories.UpdateDeviceRequest
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.devices.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

/**
 * Admin device management routes.
 */
fun Route.adminDeviceRoutes(
    deviceRepository: DeviceRepository,
    channelRepository: ChannelRepository
) {
    route("/admin/devices") {
        // GET /admin/devices - List devices
        get {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val now = Clock.System.now()
            val onlineThreshold = now - 5.minutes

            // Parse filters
            val statusFilter = call.request.queryParameters["status"]
            val channelIdParam = call.request.queryParameters["channel"]
            val search = call.request.queryParameters["search"]

            val channelId = channelIdParam?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            val filters = DeviceFilters(
                status = statusFilter?.takeIf { it.isNotBlank() },
                channelId = channelId,
                search = search?.takeIf { it.isNotBlank() }
            )

            // Fetch devices
            var devices = deviceRepository.findAll(tenantId)

            // Apply filters
            if (filters.channelId != null) {
                devices = devices.filter { it.assignedChannelId == filters.channelId }
            }
            if (filters.search != null) {
                devices = devices.filter { it.displayName.contains(filters.search, ignoreCase = true) }
            }

            // Fetch channels for filter dropdown
            val channels = channelRepository.findAll(tenantId).map { channel ->
                ChannelOption(id = channel.id.value, name = channel.name)
            }

            // Map to view models
            val deviceItems = devices.map { device ->
                val isOnline = device.lastSeenAt?.let { it >= onlineThreshold } ?: false
                val channelName = device.assignedChannelId?.let { channelId ->
                    channelRepository.findById(tenantId, channelId)?.name
                }

                DeviceViewItem(
                    id = device.id.value,
                    name = device.displayName,
                    status = device.enrollStatus,
                    isOnline = isOnline,
                    channelName = channelName,
                    appVersion = device.appVersion,
                    lastSeen = device.lastSeenAt
                )
            }.let { items ->
                // Filter by online/offline status after mapping
                when (filters.status) {
                    "online" -> items.filter { it.isOnline }
                    "offline" -> items.filter { !it.isOnline }
                    else -> items
                }
            }

            // Calculate stats
            val allDevices = deviceRepository.findAll(tenantId)
            val stats = DeviceStats(
                total = allDevices.size,
                online = allDevices.count { d -> d.lastSeenAt?.let { it >= onlineThreshold } ?: false },
                offline = allDevices.count { d -> d.lastSeenAt?.let { it < onlineThreshold } ?: true },
                pending = allDevices.count { it.enrollStatus == DeviceEnrollStatus.PENDING }
            )

            call.respondHtml {
                devicesListView(
                    session = session,
                    devices = deviceItems,
                    stats = stats,
                    filters = filters,
                    channels = channels
                )
            }
        }

        // GET /admin/devices/enroll - Enroll codes page
        get("/enroll") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val channels = channelRepository.findAll(tenantId).map { channel ->
                ChannelOption(id = channel.id.value, name = channel.name)
            }

            call.respondHtml {
                enrollCodesView(
                    session = session,
                    channels = channels,
                    activeCodes = emptyList()
                )
            }
        }

        // POST /admin/devices/enroll/generate - Generate new enrollment code
        post("/enroll/generate") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val params = call.receiveParameters()
            val channelIdStr = params["channelId"]
            val channelId = channelIdStr?.takeIf { it.isNotBlank() }?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            // Generate a 6-digit code
            val code = (100000..999999).random().toString()

            // TODO: Store the code in database with expiry
            // For now, just show it to the user

            val channels = channelRepository.findAll(tenantId).map { channel ->
                ChannelOption(id = channel.id.value, name = channel.name)
            }

            call.respondHtml {
                enrollCodesView(
                    session = session,
                    channels = channels,
                    activeCodes = emptyList(),
                    generatedCode = code
                )
            }
        }

        // GET /admin/devices/:id - Device detail
        get("/{id}") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val deviceId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

            if (deviceId == null) {
                call.respondRedirect("/admin/devices")
                return@get
            }

            val device = deviceRepository.findById(tenantId, deviceId)
            if (device == null) {
                call.respondRedirect("/admin/devices")
                return@get
            }

            val now = Clock.System.now()
            val onlineThreshold = now - 5.minutes
            val isOnline = device.lastSeenAt?.let { it >= onlineThreshold } ?: false

            val channelName = device.assignedChannelId?.let { channelId ->
                channelRepository.findById(tenantId, channelId)?.name
            }

            val channels = channelRepository.findAll(tenantId).map { channel ->
                ChannelOption(id = channel.id.value, name = channel.name)
            }

            val deviceModel = DeviceDetailModel(
                id = device.id.value,
                name = device.displayName,
                status = device.enrollStatus,
                isOnline = isOnline,
                channelId = device.assignedChannelId,
                channelName = channelName,
                appVersion = device.appVersion,
                androidModel = device.androidModel,
                androidVersion = device.androidVersion,
                lastSeen = device.lastSeenAt,
                createdAt = device.createdAt
            )

            call.respondHtml {
                deviceDetailView(
                    session = session,
                    device = deviceModel,
                    channels = channels
                )
            }
        }

        // POST /admin/devices/:id/assign - Assign channel
        post("/{id}/assign") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val deviceId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

            if (deviceId == null) {
                call.respondRedirect("/admin/devices")
                return@post
            }

            val params = call.receiveParameters()
            val channelIdStr = params["channelId"]
            val channelId = channelIdStr?.takeIf { it.isNotBlank() }?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            deviceRepository.update(tenantId, deviceId, UpdateDeviceRequest(assignedChannelId = channelId))
            call.respondRedirect("/admin/devices/$deviceId")
        }
    }
}
