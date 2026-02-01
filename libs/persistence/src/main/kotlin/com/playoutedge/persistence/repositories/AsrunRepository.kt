package com.playoutedge.persistence.repositories

import com.playoutedge.domain.enums.AsrunEventType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AsrunEventEntity
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import java.util.UUID

interface AsrunRepository {
    suspend fun createEvent(
        tenantId: TenantId,
        deviceId: UUID,
        channelId: UUID?,
        scheduleVersionId: UUID?,
        assetId: UUID?,
        eventType: AsrunEventType,
        at: Instant,
        details: JsonObject? = null
    ): AsrunEventEntity

    suspend fun createEvents(events: List<AsrunEventInput>): Int

    suspend fun findByDevice(
        tenantId: TenantId,
        deviceId: UUID,
        filters: AsrunFilters
    ): List<AsrunEventEntity>

    suspend fun findByChannel(
        tenantId: TenantId,
        channelId: UUID,
        filters: AsrunFilters
    ): List<AsrunEventEntity>

    suspend fun findByTenant(
        tenantId: TenantId,
        filters: AsrunFilters
    ): List<AsrunEventEntity>

    suspend fun count(tenantId: TenantId, filters: AsrunFilters): Long

    suspend fun getSummary(tenantId: TenantId, filters: AsrunFilters): AsrunSummary

    suspend fun deleteOlderThan(days: Int): Long
}

data class AsrunEventInput(
    val tenantId: TenantId,
    val deviceId: UUID,
    val channelId: UUID?,
    val scheduleVersionId: UUID?,
    val assetId: UUID?,
    val eventType: AsrunEventType,
    val at: Instant,
    val details: JsonObject?
)

data class AsrunFilters(
    val deviceId: UUID? = null,
    val channelId: UUID? = null,
    val assetId: UUID? = null,
    val eventType: AsrunEventType? = null,
    val from: Instant? = null,
    val to: Instant? = null,
    val limit: Int = 100,
    val offset: Int = 0
)

data class AsrunSummary(
    val totalEvents: Long,
    val byAsset: Map<UUID, AssetPlaybackSummary>
)

data class AssetPlaybackSummary(
    val assetId: UUID,
    val playCount: Long
)
