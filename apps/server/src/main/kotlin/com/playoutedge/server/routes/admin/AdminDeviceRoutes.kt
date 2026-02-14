package com.playoutedge.server.routes.admin

import com.playoutedge.domain.enums.ActionStatus
import com.playoutedge.domain.enums.ActionType
import com.playoutedge.domain.enums.DeviceEnrollStatus
import com.playoutedge.domain.enums.EnrollCodeStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.DeviceActionEntity
import com.playoutedge.persistence.entities.EnrollCodeEntity
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.UserEntity
import com.playoutedge.persistence.repositories.ChannelRepository
import com.playoutedge.persistence.repositories.DeviceLogRepository
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.persistence.repositories.UpdateDeviceRequest
import com.playoutedge.persistence.tables.Devices
import com.playoutedge.persistence.tables.EnrollCodes
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.devices.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Admin device management routes.
 */
fun Route.adminDeviceRoutes(
    deviceRepository: DeviceRepository,
    channelRepository: ChannelRepository,
    deviceLogRepository: DeviceLogRepository? = null
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
            val page = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val pageSize = 50

            val channelId = channelIdParam?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            val filters = DeviceFilters(
                status = statusFilter?.takeIf { it.isNotBlank() },
                channelId = channelId,
                search = search?.takeIf { it.isNotBlank() }
            )

            // Fetch devices with pagination
            val (pagedDevices, totalCount) = deviceRepository.findAllPaged(tenantId, pageSize, (page - 1) * pageSize)
            var devices = pagedDevices

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
                    lastSeen = device.lastSeenAt,
                    latitude = device.latitude,
                    longitude = device.longitude
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

            val totalPages = ((totalCount + pageSize - 1) / pageSize).toInt().coerceAtLeast(1)
            val queryParams = buildString {
                if (filters.status != null) append("&status=${filters.status}")
                if (filters.channelId != null) append("&channel=${filters.channelId}")
                if (filters.search != null) append("&search=${filters.search}")
            }.let { if (it.isNotEmpty()) "?${it.removePrefix("&")}" else "" }

            val pagination = com.playoutedge.server.views.PaginationInfo(
                currentPage = page,
                totalPages = totalPages,
                totalItems = totalCount,
                basePath = "/admin/devices",
                queryParams = queryParams
            )

            call.respondHtml {
                devicesListView(
                    session = session,
                    devices = deviceItems,
                    stats = stats,
                    filters = filters,
                    channels = channels,
                    pagination = pagination
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

            // Generate code and store in database
            val code = com.playoutedge.server.routes.generateEnrollCode()
            val expiresAt = Clock.System.now().plus(24.hours)

            newSuspendedTransaction {
                EnrollCodeEntity.new {
                    this.tenant = TenantEntity[session.tenantId]
                    this.code = code
                    this.status = EnrollCodeStatus.PENDING
                    this.expiresAt = expiresAt
                    this.createdBy = UserEntity[session.subject]
                    this.createdAt = Clock.System.now()
                    this.channel = channelId?.let { com.playoutedge.persistence.entities.ChannelEntity.findById(it) }
                }
            }

            // Fetch active codes to display
            val activeCodes = newSuspendedTransaction {
                EnrollCodeEntity.find {
                    (EnrollCodes.tenantId eq session.tenantId) and
                    (EnrollCodes.status eq EnrollCodeStatus.PENDING)
                }.toList().map { ec ->
                    EnrollCodeItem(
                        id = ec.id.value,
                        code = ec.code,
                        expiresAt = ec.expiresAt,
                        channelName = ec.channel?.name,
                        isExpired = ec.expiresAt < Clock.System.now()
                    )
                }
            }

            val channels = channelRepository.findAll(tenantId).map { channel ->
                ChannelOption(id = channel.id.value, name = channel.name)
            }

            call.respondHtml {
                enrollCodesView(
                    session = session,
                    channels = channels,
                    activeCodes = activeCodes,
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
                createdAt = device.createdAt,
                latitude = device.latitude,
                longitude = device.longitude,
                locationName = device.locationName,
                powerOnTime = device.powerOnTime,
                powerOffTime = device.powerOffTime
            )

            call.respondHtml {
                deviceDetailView(
                    session = session,
                    device = deviceModel,
                    channels = channels
                )
            }
        }

        // POST /admin/devices/bulk-assign - Bulk assign channel to devices
        post("/bulk-assign") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
            val tenantId = TenantId(session.tenantId)
            val params = call.receiveParameters()
            val deviceIds = params.getAll("deviceIds")?.mapNotNull { runCatching { UUID.fromString(it) }.getOrNull() } ?: emptyList()
            val channelId = params["channelId"]?.takeIf { it.isNotBlank() }?.let { runCatching { UUID.fromString(it) }.getOrNull() }

            deviceIds.forEach { deviceId ->
                deviceRepository.update(tenantId, deviceId, UpdateDeviceRequest(assignedChannelId = channelId))
            }
            call.respondRedirect("/admin/devices")
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

        // POST /admin/devices/:id/location - Save device location
        post("/{id}/location") {
            val session = call.adminSession!!
            val id = UUID.fromString(call.parameters["id"]!!)
            val params = call.receiveParameters()
            newSuspendedTransaction {
                Devices.update({ (Devices.id eq id) and (Devices.tenantId eq session.tenantId) }) {
                    it[latitude] = params["latitude"]?.toDoubleOrNull()
                    it[longitude] = params["longitude"]?.toDoubleOrNull()
                    it[locationName] = params["locationName"]?.takeIf { n -> n.isNotBlank() }
                }
            }
            call.respondRedirect("/admin/devices/$id")
        }

        // POST /admin/devices/:id/power-schedule - Save power schedule
        post("/{id}/power-schedule") {
            val session = call.adminSession!!
            val id = UUID.fromString(call.parameters["id"]!!)
            val params = call.receiveParameters()
            newSuspendedTransaction {
                Devices.update({ (Devices.id eq id) and (Devices.tenantId eq session.tenantId) }) {
                    it[powerOnTime] = params["powerOnTime"]?.takeIf { t -> t.matches(Regex("\\d{2}:\\d{2}")) }
                    it[powerOffTime] = params["powerOffTime"]?.takeIf { t -> t.matches(Regex("\\d{2}:\\d{2}")) }
                }
            }
            call.respondRedirect("/admin/devices/$id")
        }

        // GET /admin/devices/:id/logs - Device logs page
        get("/{id}/logs") {
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

            val page = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val pageSize = 50
            val logs = deviceLogRepository?.findByDevice(deviceId, pageSize, (page - 1) * pageSize) ?: emptyList()
            val totalLogs = deviceLogRepository?.countByDevice(deviceId) ?: 0L

            call.respondHtml {
                deviceLogsView(
                    session = session,
                    deviceId = deviceId,
                    deviceName = device.displayName,
                    logs = logs,
                    currentPage = page,
                    totalLogs = totalLogs,
                    pageSize = pageSize
                )
            }
        }

        // POST /admin/devices/:id/reboot - Request device reboot
        post("/{id}/reboot") {
            val session = call.adminSession!!
            val id = UUID.fromString(call.parameters["id"]!!)
            val tenantId = TenantId(session.tenantId)

            // Verify device belongs to tenant
            val device = deviceRepository.findById(tenantId, id)
            if (device == null) {
                call.respondRedirect("/admin/devices")
                return@post
            }

            newSuspendedTransaction {
                DeviceActionEntity.new {
                    this.tenant = TenantEntity[session.tenantId]
                    this.device = device
                    this.action = ActionType.REBOOT
                    this.status = ActionStatus.PENDING
                    this.paramsJson = null
                    this.createdBy = UserEntity[session.subject]
                    this.createdAt = Clock.System.now()
                    this.expiresAt = Clock.System.now().plus(1.hours)
                }
            }

            call.respondRedirect("/admin/devices/$id")
        }
    }
}
