package com.playoutedge.server.routes

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.domain.enums.AsrunEventType
import com.playoutedge.persistence.repositories.AsrunFilters
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.tenantId
import com.playoutedge.server.services.AsrunService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.util.UUID

@Serializable
data class AsrunEventResponse(
    val id: String,
    val deviceId: String,
    val channelId: String?,
    val scheduleVersionId: String?,
    val assetId: String?,
    val eventType: String,
    val at: String,
    val details: JsonObject?,
    val createdAt: String
)

@Serializable
data class AsrunSummaryResponse(
    val totalEvents: Long,
    val byAsset: List<AssetPlaybackSummaryResponse>
)

@Serializable
data class AssetPlaybackSummaryResponse(
    val assetId: String,
    val playCount: Long
)

@Serializable
data class AsrunQueryResponse(
    val events: List<AsrunEventResponse>,
    val total: Long,
    val summary: AsrunSummaryResponse?,
    val limit: Int,
    val offset: Int
)

fun Route.asrunRoutes(asrunService: AsrunService) {
    authenticate(AUTH_ADMIN) {
        route("/api/admin/asrun") {
            // GET /api/admin/asrun - Query as-run events
            get {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val deviceId = call.request.queryParameters["deviceId"]?.let { UUID.fromString(it) }
                val channelId = call.request.queryParameters["channelId"]?.let { UUID.fromString(it) }
                val assetId = call.request.queryParameters["assetId"]?.let { UUID.fromString(it) }
                val eventType = call.request.queryParameters["eventType"]?.let {
                    try {
                        AsrunEventType.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                val from = call.request.queryParameters["from"]?.let { Instant.parse(it) }
                val to = call.request.queryParameters["to"]?.let { Instant.parse(it) }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                val includeSummary = call.request.queryParameters["summary"]?.toBoolean() ?: false

                val filters = AsrunFilters(
                    deviceId = deviceId,
                    channelId = channelId,
                    assetId = assetId,
                    eventType = eventType,
                    from = from,
                    to = to,
                    limit = limit.coerceIn(1, 1000),
                    offset = offset.coerceAtLeast(0)
                )

                val result = asrunService.query(tenantId, filters, includeSummary)

                call.respond(AsrunQueryResponse(
                    events = result.events.map { event ->
                        AsrunEventResponse(
                            id = event.id.value.toString(),
                            deviceId = event.device.id.value.toString(),
                            channelId = event.channel?.id?.value?.toString(),
                            scheduleVersionId = event.scheduleVersion?.id?.value?.toString(),
                            assetId = event.asset?.id?.value?.toString(),
                            eventType = event.eventType.name,
                            at = event.at.toString(),
                            details = event.detailsJson,
                            createdAt = event.createdAt.toString()
                        )
                    },
                    total = result.total,
                    summary = result.summary?.let { summary ->
                        AsrunSummaryResponse(
                            totalEvents = summary.totalEvents,
                            byAsset = summary.byAsset.values.map { assetSummary ->
                                AssetPlaybackSummaryResponse(
                                    assetId = assetSummary.assetId.toString(),
                                    playCount = assetSummary.playCount
                                )
                            }
                        )
                    },
                    limit = result.limit,
                    offset = result.offset
                ))
            }

            // GET /api/admin/asrun/export - Export as CSV
            get("/export") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val deviceId = call.request.queryParameters["deviceId"]?.let { UUID.fromString(it) }
                val channelId = call.request.queryParameters["channelId"]?.let { UUID.fromString(it) }
                val from = call.request.queryParameters["from"]?.let { Instant.parse(it) }
                val to = call.request.queryParameters["to"]?.let { Instant.parse(it) }

                val filters = AsrunFilters(
                    deviceId = deviceId,
                    channelId = channelId,
                    from = from,
                    to = to,
                    limit = 50000,
                    offset = 0
                )

                val result = asrunService.query(tenantId, filters, includeSummary = false)

                val csv = buildString {
                    appendLine("id,device_id,channel_id,schedule_version_id,asset_id,event_type,at,created_at")
                    result.events.forEach { event ->
                        appendLine(listOf(
                            event.id.value,
                            event.device.id.value,
                            event.channel?.id?.value ?: "",
                            event.scheduleVersion?.id?.value ?: "",
                            event.asset?.id?.value ?: "",
                            event.eventType.name,
                            event.at,
                            event.createdAt
                        ).joinToString(","))
                    }
                }

                call.response.header(
                    HttpHeaders.ContentDisposition,
                    "attachment; filename=\"asrun-log.csv\""
                )
                call.respondText(csv, ContentType.Text.CSV)
            }
        }
    }
}
