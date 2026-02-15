package com.playoutedge.server.routes.admin

import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.ChannelRepository
import com.playoutedge.persistence.repositories.CreateChannelRequest
import com.playoutedge.persistence.repositories.UpdateChannelRequest
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.persistence.repositories.ScheduleRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.services.ScheduleService
import com.playoutedge.server.views.channels.*
import com.playoutedge.server.views.emptyState
import kotlinx.html.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.playoutedge.server.views.components.isHtmx
import com.playoutedge.server.views.components.respondHxFragment
import com.playoutedge.server.views.components.hxPagination
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.storage.StorageService
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Admin channel management routes.
 */
fun Route.adminChannelRoutes(
    channelRepository: ChannelRepository,
    deviceRepository: DeviceRepository,
    scheduleRepository: ScheduleRepository,
    storageService: StorageService,
    scheduleService: ScheduleService? = null
) {
    route("/admin/channels") {
        // GET /admin/channels - List channels
        get {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)

            // Parse filters
            val statusParam = call.request.queryParameters["status"]
            val search = call.request.queryParameters["search"]
            val page = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val pageSize = 50
            val filters = ChannelFilters(
                status = statusParam?.let { runCatching { ChannelStatus.valueOf(it) }.getOrNull() },
                search = search?.takeIf { it.isNotBlank() }
            )

            // Fetch channels with pagination
            val (allChannels, totalCount) = channelRepository.findAllPaged(tenantId, pageSize, (page - 1) * pageSize)

            // Apply filters
            var channels = allChannels
            if (filters.status != null) {
                channels = channels.filter { it.status == filters.status }
            }
            if (filters.search != null) {
                channels = channels.filter { it.name.contains(filters.search, ignoreCase = true) }
            }

            // Map to view models with device counts
            val channelItems = channels.map { channel ->
                val deviceCount = deviceRepository.findByChannel(tenantId, channel.id.value).size
                val lastPublish = scheduleRepository.findActiveVersion(tenantId, channel.id.value)?.publishedAt

                ChannelListItem(
                    id = channel.id.value,
                    name = channel.name,
                    status = channel.status,
                    deviceCount = deviceCount,
                    lastPublish = lastPublish
                )
            }

            val totalPages = ((totalCount + pageSize - 1) / pageSize).toInt().coerceAtLeast(1)
            val queryParams = buildString {
                if (filters.status != null) append("&status=${filters.status!!.name}")
                if (filters.search != null) append("&search=${filters.search}")
            }.let { if (it.isNotEmpty()) "?${it.removePrefix("&")}" else "" }

            val pagination = com.playoutedge.server.views.PaginationInfo(
                currentPage = page,
                totalPages = totalPages,
                totalItems = totalCount,
                basePath = "/admin/channels",
                queryParams = queryParams
            )

            call.respondHtml {
                channelsListView(
                    session = session,
                    channels = channelItems,
                    filters = filters,
                    pagination = pagination
                )
            }
        }

        // GET /admin/channels/table - HTMX fragment for channel table
        get("/table") {
            val session = call.adminSession ?: return@get
            val tenantId = TenantId(session.tenantId)

            val statusParam = call.request.queryParameters["status"]
            val search = call.request.queryParameters["search"]
            val page = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val pageSize = 50
            val filters = ChannelFilters(
                status = statusParam?.let { runCatching { ChannelStatus.valueOf(it) }.getOrNull() },
                search = search?.takeIf { it.isNotBlank() }
            )

            val (allChannels, totalCount) = channelRepository.findAllPaged(tenantId, pageSize, (page - 1) * pageSize)

            var channels = allChannels
            if (filters.status != null) {
                channels = channels.filter { it.status == filters.status }
            }
            if (filters.search != null) {
                channels = channels.filter { it.name.contains(filters.search, ignoreCase = true) }
            }

            val channelItems = channels.map { channel ->
                val deviceCount = deviceRepository.findByChannel(tenantId, channel.id.value).size
                val lastPublish = scheduleRepository.findActiveVersion(tenantId, channel.id.value)?.publishedAt
                ChannelListItem(
                    id = channel.id.value,
                    name = channel.name,
                    status = channel.status,
                    deviceCount = deviceCount,
                    lastPublish = lastPublish
                )
            }

            val totalPages = ((totalCount + pageSize - 1) / pageSize).toInt().coerceAtLeast(1)

            call.respondHxFragment {
                div {
                    id = "channels-table"
                    div("card") {
                        if (channelItems.isEmpty()) {
                            emptyState(
                                icon = "monitor",
                                title = "No channels found",
                                description = "Try adjusting your filters or create a new channel.",
                                actionHref = "/admin/channels/new",
                                actionLabel = "Create Channel"
                            )
                        } else {
                            channelTable(channelItems)
                        }
                    }
                    if (totalPages > 1) {
                        hxPagination(
                            currentPage = page,
                            totalPages = totalPages,
                            totalItems = totalCount,
                            baseUrl = "/admin/channels/table",
                            target = "#channels-table"
                        )
                    }
                }
            }
        }

        // GET /admin/channels/new - New channel form
        get("/new") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            call.respondHtml {
                newChannelView(session = session)
            }
        }

        // POST /admin/channels - Create channel
        post {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val params = call.receiveParameters()
            val name = params["name"]?.trim()

            if (name.isNullOrBlank()) {
                call.respondHtml {
                    newChannelView(session = session, error = "Channel name is required")
                }
                return@post
            }

            val channel = channelRepository.create(tenantId, CreateChannelRequest(name = name))
            call.respondRedirect("/admin/channels/${channel.id.value}")
        }

        // GET /admin/channels/:id - Channel detail
        get("/{id}") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val channel = channelRepository.findById(tenantId, channelId)
            if (channel == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            // Get devices assigned to this channel — wrap in transaction for lazy entity access
            val now = Clock.System.now()
            val onlineThreshold = now - 5.minutes
            val (devices, scheduleItems, channelDetail) = newSuspendedTransaction {
                val devices = deviceRepository.findByChannel(tenantId, channelId).map { device ->
                    DeviceListItem(
                        id = device.id.value,
                        name = device.displayName,
                        status = device.enrollStatus,
                        lastSeen = device.lastSeenAt,
                        isOnline = device.lastSeenAt?.let { it >= onlineThreshold } ?: false
                    )
                }

                val scheduleVersion = scheduleRepository.findActiveVersion(tenantId, channelId)
                val scheduleItems = scheduleVersion?.let { version ->
                    scheduleRepository.findItemsByVersion(tenantId, version.id.value).map { item ->
                        ScheduleItemView(
                            id = item.id.value,
                            assetId = item.asset.id.value,
                            assetName = item.asset.name,
                            sortOrder = item.orderIndex,
                            timeStart = item.timeStart?.toString(),
                            timeEnd = item.timeEnd?.toString()
                        )
                    }
                } ?: emptyList()

                val channelDetail = ChannelDetail(
                    id = channel.id.value,
                    name = channel.name,
                    status = channel.status,
                    createdAt = channel.createdAt
                )

                Triple(devices, scheduleItems, channelDetail)
            }

            // Validate active schedule if service available
            val validationWarnings = if (scheduleService != null) {
                val activeVersion = scheduleRepository.findActiveVersion(tenantId, channelId)
                if (activeVersion != null) {
                    scheduleService.validateSchedule(tenantId, activeVersion.id.value)
                } else emptyList()
            } else emptyList()

            val embedHost = call.request.host().let { host ->
                val port = call.request.port()
                if (port != 80 && port != 443) "$host:$port" else host
            }

            call.respondHtml {
                channelDetailView(
                    session = session,
                    channel = channelDetail,
                    scheduleItems = scheduleItems,
                    devices = devices,
                    validationWarnings = validationWarnings,
                    embedHost = embedHost
                )
            }
        }

        // GET /admin/channels/:id/edit - Edit channel form
        get("/{id}/edit") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val channel = channelRepository.findById(tenantId, channelId)
            if (channel == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            call.respondHtml {
                editChannelView(
                    session = session,
                    channel = ChannelDetail(
                        id = channel.id.value,
                        name = channel.name,
                        status = channel.status,
                        createdAt = channel.createdAt
                    )
                )
            }
        }

        // POST /admin/channels/:id - Update channel
        post("/{id}") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@post
            }

            val params = call.receiveParameters()
            val name = params["name"]?.trim()

            if (name.isNullOrBlank()) {
                val channel = channelRepository.findById(tenantId, channelId)
                if (channel != null) {
                    call.respondHtml {
                        editChannelView(
                            session = session,
                            channel = ChannelDetail(
                                id = channel.id.value,
                                name = channel.name,
                                status = channel.status,
                                createdAt = channel.createdAt
                            ),
                            error = "Channel name is required"
                        )
                    }
                } else {
                    call.respondRedirect("/admin/channels")
                }
                return@post
            }

            channelRepository.update(tenantId, channelId, UpdateChannelRequest(name = name))
            call.respondRedirect("/admin/channels/$channelId")
        }

        // POST /admin/channels/:id/pause - Pause channel
        post("/{id}/pause") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

            if (channelId != null) {
                channelRepository.update(tenantId, channelId, UpdateChannelRequest(status = ChannelStatus.PAUSED))
            }

            call.respondRedirect("/admin/channels/$channelId")
        }

        // POST /admin/channels/:id/resume - Resume channel
        post("/{id}/resume") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

            if (channelId != null) {
                channelRepository.update(tenantId, channelId, UpdateChannelRequest(status = ChannelStatus.ACTIVE))
            }

            call.respondRedirect("/admin/channels/$channelId")
        }

        // POST /admin/channels/:id/delete - Delete channel
        post("/{id}/delete") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

            if (channelId != null) {
                channelRepository.delete(tenantId, channelId)
            }

            if (call.isHtmx) {
                call.response.header("HX-Redirect", "/admin/channels?success=Channel+deleted")
                call.respond(io.ktor.http.HttpStatusCode.OK)
            } else {
                call.respondRedirect("/admin/channels?success=Channel+deleted")
            }
        }

        // GET /admin/channels/:id/live - Live preview
        get("/{id}/live") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val channel = channelRepository.findById(tenantId, channelId)
            if (channel == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val (scheduleVersionNumber, previewItems) = newSuspendedTransaction {
                val activeVersion = scheduleRepository.findActiveVersion(tenantId, channelId)
                val scheduleItems = activeVersion?.let { version ->
                    scheduleRepository.findItemsByVersion(tenantId, version.id.value)
                } ?: emptyList()

                val items = scheduleItems.mapIndexed { index, item ->
                    LivePreviewItem(
                        assetId = item.asset.id.value,
                        assetName = item.asset.name,
                        assetType = item.asset.type,
                        assetStorageKey = item.asset.storageKey,
                        durationMs = item.asset.durationMs,
                        orderIndex = index,
                        timeStart = item.timeStart?.toString(),
                        timeEnd = item.timeEnd?.toString()
                    )
                }

                Pair(activeVersion?.version, items)
            }

            // Generate signed URLs outside transaction
            val itemsWithUrls = previewItems.map { item ->
                item.copy(assetUrl = storageService.getSignedUrl(item.assetStorageKey, 1.hours))
            }

            val now = Clock.System.now()
            val currentTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
            val currentIndex = calculateCurrentIndex(previewItems, now.toEpochMilliseconds())

            call.respondHtml {
                livePreviewView(
                    session = session,
                    channelId = channelId,
                    channelName = channel.name,
                    scheduleVersion = scheduleVersionNumber,
                    items = itemsWithUrls,
                    currentIndex = currentIndex,
                    currentTimeFormatted = "${currentTime.hour}:${String.format("%02d", currentTime.minute)}"
                )
            }
        }
    }
}

/**
 * Calculate which item should be playing based on elapsed time.
 */
private fun calculateCurrentIndex(items: List<LivePreviewItem>, nowEpochMs: Long): Int {
    if (items.isEmpty()) return 0
    val totalDurationMs = items.mapNotNull { it.durationMs }.sum().toLong()
    if (totalDurationMs <= 0) return 0
    val elapsed = nowEpochMs % totalDurationMs
    var cumulative = 0L
    items.forEachIndexed { index, item ->
        cumulative += (item.durationMs ?: 0).toLong()
        if (elapsed < cumulative) return index
    }
    return 0
}
