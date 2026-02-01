package com.playoutedge.server.services

import com.playoutedge.domain.enums.AsrunEventType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AsrunEventEntity
import com.playoutedge.persistence.repositories.AsrunEventInput
import com.playoutedge.persistence.repositories.AsrunFilters
import com.playoutedge.persistence.repositories.AsrunRepository
import com.playoutedge.persistence.repositories.AsrunSummary
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import java.util.UUID

data class AsrunQueryResult(
    val events: List<AsrunEventEntity>,
    val total: Long,
    val summary: AsrunSummary?,
    val limit: Int,
    val offset: Int
)

class AsrunService(
    private val asrunRepo: AsrunRepository
) {
    suspend fun recordEvent(
        tenantId: TenantId,
        deviceId: UUID,
        channelId: UUID?,
        scheduleVersionId: UUID?,
        assetId: UUID?,
        eventType: AsrunEventType,
        at: Instant,
        details: JsonObject? = null
    ): AsrunEventEntity {
        return asrunRepo.createEvent(
            tenantId = tenantId,
            deviceId = deviceId,
            channelId = channelId,
            scheduleVersionId = scheduleVersionId,
            assetId = assetId,
            eventType = eventType,
            at = at,
            details = details
        )
    }

    suspend fun recordBatch(events: List<AsrunEventInput>): Int {
        return asrunRepo.createEvents(events)
    }

    suspend fun query(
        tenantId: TenantId,
        filters: AsrunFilters,
        includeSummary: Boolean = false
    ): AsrunQueryResult {
        val events = asrunRepo.findByTenant(tenantId, filters)
        val total = asrunRepo.count(tenantId, filters)
        val summary = if (includeSummary) asrunRepo.getSummary(tenantId, filters) else null

        return AsrunQueryResult(
            events = events,
            total = total,
            summary = summary,
            limit = filters.limit,
            offset = filters.offset
        )
    }

    suspend fun queryByDevice(
        tenantId: TenantId,
        deviceId: UUID,
        filters: AsrunFilters
    ): List<AsrunEventEntity> {
        return asrunRepo.findByDevice(tenantId, deviceId, filters)
    }

    suspend fun queryByChannel(
        tenantId: TenantId,
        channelId: UUID,
        filters: AsrunFilters
    ): List<AsrunEventEntity> {
        return asrunRepo.findByChannel(tenantId, channelId, filters)
    }

    suspend fun cleanup(retentionDays: Int = 30): Long {
        return asrunRepo.deleteOlderThan(retentionDays)
    }
}
