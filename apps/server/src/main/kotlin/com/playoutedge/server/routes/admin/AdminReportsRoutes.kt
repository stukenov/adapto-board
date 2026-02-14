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
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
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

            // Date presets
            val preset = call.request.queryParameters["preset"]
            val (resolvedFromDate, resolvedToDate) = resolveDatePreset(
                preset,
                call.request.queryParameters["fromDate"],
                call.request.queryParameters["toDate"]
            )
            val fromDate = resolvedFromDate
            val toDate = resolvedToDate

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

            // Build chart data - playback by day (last 7 days)
            val tz = TimeZone.UTC
            val dayMap = mutableMapOf<String, Long>()
            eventItems.forEach { event ->
                val dayLabel = event.at.toLocalDateTime(tz).date.toString()
                dayMap[dayLabel] = (dayMap[dayLabel] ?: 0) + 1
            }
            val maxDayValue = dayMap.values.maxOrNull() ?: 1L
            val playbackByDay = dayMap.entries
                .sortedBy { it.key }
                .takeLast(7)
                .map { (day, count) ->
                    ChartDataPoint(day, count, (count.toDouble() / maxDayValue) * 100.0)
                }

            // Top 5 assets by play count
            val maxAssetCount = summaryView.byAsset.maxOfOrNull { it.playCount } ?: 1L
            val topAssets = summaryView.byAsset
                .sortedByDescending { it.playCount }
                .take(5)
                .map { asset ->
                    ChartDataPoint(asset.assetName, asset.playCount, (asset.playCount.toDouble() / maxAssetCount) * 100.0)
                }

            call.respondHtml {
                asrunReportsView(session, eventItems, summaryView, filtersView, deviceOptions, channelOptions, playbackByDay, topAssets, preset)
            }
        }

        // GET /admin/reports/asrun/export - Export as-run to CSV or JSON
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

            val format = call.request.queryParameters["format"] ?: "csv"
            val events = asrunRepository.findByTenant(tenantId, filters)

            if (format == "json") {
                val jsonStr = buildString {
                    append("[")
                    events.forEachIndexed { index, event ->
                        if (index > 0) append(",")
                        append("{")
                        append("\"timestamp\":\"${event.at}\",")
                        append("\"deviceId\":\"${event.device.id.value}\",")
                        append("\"deviceName\":\"${event.device.displayName.replace("\"", "\\\"")}\",")
                        append("\"channelId\":\"${event.channel?.id?.value ?: ""}\",")
                        append("\"channelName\":\"${(event.channel?.name ?: "").replace("\"", "\\\"")}\",")
                        append("\"assetId\":\"${event.asset?.id?.value ?: ""}\",")
                        append("\"assetName\":\"${(event.asset?.name ?: "").replace("\"", "\\\"")}\",")
                        append("\"eventType\":\"${event.eventType.name}\"")
                        append("}")
                    }
                    append("]")
                }
                call.respondText(jsonStr, ContentType.Application.Json)
            } else {
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
        }

        // GET /admin/reports/asrun/chart-data - Chart data as HTML fragment
        get("/asrun/chart-data") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)

            val preset = call.request.queryParameters["preset"] ?: "last7days"
            val (resolvedFrom, resolvedTo) = resolveDatePreset(preset, null, null)

            val filters = AsrunFilters(
                from = resolvedFrom?.let { parseDate(it) },
                to = resolvedTo?.let { parseDate(it) },
                limit = 10000
            )

            val events = asrunRepository.findByTenant(tenantId, filters)
            val summary = asrunRepository.getSummary(tenantId, filters)

            val tz = TimeZone.UTC
            val dayMap = mutableMapOf<String, Long>()
            events.forEach { event ->
                val dayLabel = event.at.toLocalDateTime(tz).date.toString()
                dayMap[dayLabel] = (dayMap[dayLabel] ?: 0) + 1
            }
            val maxDayValue = dayMap.values.maxOrNull() ?: 1L

            val assetNames = events
                .mapNotNull { it.asset }
                .distinctBy { it.id.value }
                .associate { it.id.value to it.name }

            val maxAssetCount = summary.byAsset.maxOfOrNull { it.value.playCount } ?: 1L

            val html = buildString {
                append("<div class='chart-container'><h4>Playback by Day</h4>")
                dayMap.entries.sortedBy { it.key }.takeLast(7).forEach { (day, count) ->
                    val pct = (count.toDouble() / maxDayValue) * 100.0
                    append("<div class='chart-bar-row'>")
                    append("<span class='chart-bar-label'>$day</span>")
                    append("<div class='chart-bar-track'><div class='chart-bar-fill' style='width:${pct}%'></div></div>")
                    append("<span class='chart-bar-value'>$count</span>")
                    append("</div>")
                }
                append("</div>")

                append("<div class='chart-container'><h4>Top Assets</h4>")
                summary.byAsset.entries.sortedByDescending { it.value.playCount }.take(5).forEach { (assetId, assetSummary) ->
                    val name = assetNames[assetId] ?: "Unknown"
                    val pct = (assetSummary.playCount.toDouble() / maxAssetCount) * 100.0
                    append("<div class='chart-bar-row'>")
                    append("<span class='chart-bar-label'>$name</span>")
                    append("<div class='chart-bar-track'><div class='chart-bar-fill' style='width:${pct}%'></div></div>")
                    append("<span class='chart-bar-value'>${assetSummary.playCount}</span>")
                    append("</div>")
                }
                append("</div>")
            }

            call.respondText(html, ContentType.Text.Html)
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

/**
 * Resolve date preset to fromDate/toDate strings (yyyy-MM-dd).
 */
private fun resolveDatePreset(
    preset: String?,
    fromDate: String?,
    toDate: String?
): Pair<String?, String?> {
    if (preset == null) return Pair(fromDate, toDate)

    val tz = TimeZone.UTC
    val today = Clock.System.now().toLocalDateTime(tz).date
    return when (preset) {
        "last7days" -> {
            val from = today.minus(7, DateTimeUnit.DAY)
            Pair(from.toString(), today.toString())
        }
        "last30days" -> {
            val from = today.minus(30, DateTimeUnit.DAY)
            Pair(from.toString(), today.toString())
        }
        "thisMonth" -> {
            val from = LocalDate(today.year, today.monthNumber, 1)
            Pair(from.toString(), today.toString())
        }
        "lastMonth" -> {
            val firstOfThisMonth = LocalDate(today.year, today.monthNumber, 1)
            val lastMonthEnd = firstOfThisMonth.minus(1, DateTimeUnit.DAY)
            val lastMonthStart = LocalDate(lastMonthEnd.year, lastMonthEnd.monthNumber, 1)
            Pair(lastMonthStart.toString(), lastMonthEnd.toString())
        }
        else -> Pair(fromDate, toDate)
    }
}
