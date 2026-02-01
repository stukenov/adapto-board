package com.playoutedge.server.routes.admin

import com.playoutedge.domain.enums.AsrunEventType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AsrunFilters
import com.playoutedge.persistence.repositories.AsrunRepository
import com.playoutedge.persistence.repositories.AuditFilters
import com.playoutedge.persistence.repositories.AuditRepository
import com.playoutedge.persistence.repositories.ChannelRepository
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.reports.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import java.util.UUID

/**
 * Admin reports routes.
 */
fun Route.adminReportsRoutes(
    asrunRepository: AsrunRepository,
    auditRepository: AuditRepository,
    deviceRepository: DeviceRepository,
    channelRepository: ChannelRepository
) {
    route("/admin/reports") {
        // GET /admin/reports - Redirect to as-run
        get {
            call.respondRedirect("/admin/reports/asrun")
        }

        // === As-Run Reports ===

        // GET /admin/reports/asrun - As-run reports
        get("/asrun") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)

            // Parse filters
            val deviceId = call.request.queryParameters["deviceId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }
            val channelId = call.request.queryParameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }
            val eventType = call.request.queryParameters["eventType"]?.let {
                runCatching { AsrunEventType.valueOf(it) }.getOrNull()
            }
            val fromDate = call.request.queryParameters["fromDate"]
            val toDate = call.request.queryParameters["toDate"]

            val filters = AsrunFilters(
                deviceId = deviceId,
                channelId = channelId,
                eventType = eventType,
                from = fromDate?.let { parseDate(it) },
                to = toDate?.let { parseDate(it) },
                limit = 100
            )

            // Fetch data
            val events = asrunRepository.findByTenant(tenantId, filters)
            val summary = asrunRepository.getSummary(tenantId, filters)
            val devices = deviceRepository.findAll(tenantId)
            val channels = channelRepository.findAll(tenantId)

            // Map to view models
            val eventItems = events.map { event ->
                AsrunEventItem(
                    id = event.id.value,
                    deviceId = event.device.id.value,
                    deviceName = event.device.displayName,
                    channelId = event.channel?.id?.value,
                    channelName = event.channel?.name,
                    assetId = event.asset?.id?.value,
                    assetName = event.asset?.name,
                    eventType = event.eventType,
                    at = event.at
                )
            }

            val assetNames = events
                .mapNotNull { it.asset }
                .distinctBy { it.id.value }
                .associate { it.id.value to it.name }

            val summaryView = AsrunSummaryView(
                totalEvents = summary.totalEvents,
                uniqueAssets = summary.byAsset.size,
                totalPlayTime = formatDuration(0), // Would calculate from events
                byAsset = summary.byAsset.map { (assetId, assetSummary) ->
                    AssetPlaybackView(
                        assetId = assetId,
                        assetName = assetNames[assetId] ?: "Unknown",
                        playCount = assetSummary.playCount
                    )
                }
            )

            val filtersView = AsrunFiltersView(
                deviceId = deviceId,
                channelId = channelId,
                eventType = eventType,
                fromDate = fromDate,
                toDate = toDate
            )

            val deviceOptions = devices.map { DeviceFilterOption(it.id.value, it.displayName) }
            val channelOptions = channels.map { ChannelFilterOption(it.id.value, it.name) }

            call.respondHtml {
                asrunReportsView(session, eventItems, summaryView, filtersView, deviceOptions, channelOptions)
            }
        }

        // GET /admin/reports/asrun/export - Export as-run to CSV
        get("/asrun/export") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)

            // Parse filters
            val deviceId = call.request.queryParameters["deviceId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }
            val channelId = call.request.queryParameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }
            val eventType = call.request.queryParameters["eventType"]?.let {
                runCatching { AsrunEventType.valueOf(it) }.getOrNull()
            }
            val fromDate = call.request.queryParameters["fromDate"]
            val toDate = call.request.queryParameters["toDate"]

            val filters = AsrunFilters(
                deviceId = deviceId,
                channelId = channelId,
                eventType = eventType,
                from = fromDate?.let { parseDate(it) },
                to = toDate?.let { parseDate(it) },
                limit = 10000
            )

            val events = asrunRepository.findByTenant(tenantId, filters)

            val csv = buildString {
                appendLine("timestamp,device_id,device_name,channel_id,channel_name,asset_id,asset_name,event_type")
                events.forEach { event ->
                    appendLine(listOf(
                        event.at.toString(),
                        event.device.id.value.toString(),
                        escapeCsv(event.device.displayName),
                        event.channel?.id?.value?.toString() ?: "",
                        escapeCsv(event.channel?.name ?: ""),
                        event.asset?.id?.value?.toString() ?: "",
                        escapeCsv(event.asset?.name ?: ""),
                        event.eventType.name
                    ).joinToString(","))
                }
            }

            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName,
                    "asrun-report-${System.currentTimeMillis()}.csv"
                ).toString()
            )
            call.respondText(csv, ContentType.Text.CSV)
        }

        // === Audit Log ===

        // GET /admin/reports/audit - Audit log
        get("/audit") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)

            // Parse filters
            val entityType = call.request.queryParameters["entityType"]?.takeIf { it.isNotBlank() }
            val action = call.request.queryParameters["action"]?.takeIf { it.isNotBlank() }
            val fromDate = call.request.queryParameters["fromDate"]
            val toDate = call.request.queryParameters["toDate"]

            val filters = AuditFilters(
                entityType = entityType,
                action = action,
                fromDate = fromDate?.let { parseDate(it) },
                toDate = toDate?.let { parseDate(it) },
                limit = 100
            )

            val logs = auditRepository.findByTenant(tenantId, filters)

            val logItems = logs.map { log ->
                AuditLogItem(
                    id = log.id.value,
                    actorName = log.actorUser?.displayName,
                    actorType = log.actorType.name,
                    action = log.action,
                    entityType = log.entityType,
                    entityId = log.entityId,
                    hasDiff = log.diffJson != null,
                    createdAt = log.createdAt
                )
            }

            val filtersView = AuditLogFilters(
                entityType = entityType,
                action = action,
                fromDate = fromDate,
                toDate = toDate
            )

            call.respondHtml {
                auditLogView(session, logItems, filtersView)
            }
        }

        // GET /admin/reports/audit/export - Export audit log to CSV
        get("/audit/export") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)

            // Parse filters
            val entityType = call.request.queryParameters["entityType"]?.takeIf { it.isNotBlank() }
            val action = call.request.queryParameters["action"]?.takeIf { it.isNotBlank() }
            val fromDate = call.request.queryParameters["fromDate"]
            val toDate = call.request.queryParameters["toDate"]

            val filters = AuditFilters(
                entityType = entityType,
                action = action,
                fromDate = fromDate?.let { parseDate(it) },
                toDate = toDate?.let { parseDate(it) },
                limit = 10000
            )

            val logs = auditRepository.findByTenant(tenantId, filters)

            val csv = buildString {
                appendLine("timestamp,actor,actor_type,action,entity_type,entity_id")
                logs.forEach { log ->
                    appendLine(listOf(
                        log.createdAt.toString(),
                        escapeCsv(log.actorUser?.displayName ?: "System"),
                        log.actorType.name,
                        log.action,
                        log.entityType,
                        log.entityId.toString()
                    ).joinToString(","))
                }
            }

            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName,
                    "audit-log-${System.currentTimeMillis()}.csv"
                ).toString()
            )
            call.respondText(csv, ContentType.Text.CSV)
        }
    }
}

private fun parseDate(dateStr: String): Instant? {
    return try {
        LocalDate.parse(dateStr).atStartOfDayIn(TimeZone.UTC)
    } catch (e: Exception) {
        null
    }
}

private fun escapeCsv(value: String): String {
    return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
        "\"${value.replace("\"", "\"\"")}\""
    } else {
        value
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return "${hours}h ${minutes}m"
}
