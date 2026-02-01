package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.AsrunEventType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AsrunEventEntity
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.DeviceEntity
import com.playoutedge.persistence.entities.ChannelEntity
import com.playoutedge.persistence.entities.ScheduleVersionEntity
import com.playoutedge.persistence.entities.AssetEntity
import com.playoutedge.persistence.repositories.AsrunEventInput
import com.playoutedge.persistence.repositories.AsrunFilters
import com.playoutedge.persistence.repositories.AsrunRepository
import com.playoutedge.persistence.repositories.AsrunSummary
import com.playoutedge.persistence.repositories.AssetPlaybackSummary
import com.playoutedge.persistence.tables.AsrunEvents
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.time.Duration.Companion.days

class AsrunRepositoryImpl : AsrunRepository {

    override suspend fun createEvent(
        tenantId: TenantId,
        deviceId: UUID,
        channelId: UUID?,
        scheduleVersionId: UUID?,
        assetId: UUID?,
        eventType: AsrunEventType,
        at: Instant,
        details: JsonObject?
    ): AsrunEventEntity = newSuspendedTransaction {
        AsrunEventEntity.new {
            this.tenant = TenantEntity[tenantId.value]
            this.device = DeviceEntity[deviceId]
            channelId?.let { this.channel = ChannelEntity[it] }
            scheduleVersionId?.let { this.scheduleVersion = ScheduleVersionEntity[it] }
            assetId?.let { this.asset = AssetEntity[it] }
            this.eventType = eventType
            this.at = at
            this.detailsJson = details
            this.createdAt = Clock.System.now()
        }
    }

    override suspend fun createEvents(events: List<AsrunEventInput>): Int = newSuspendedTransaction {
        var count = 0
        for (event in events) {
            AsrunEventEntity.new {
                this.tenant = TenantEntity[event.tenantId.value]
                this.device = DeviceEntity[event.deviceId]
                event.channelId?.let { this.channel = ChannelEntity[it] }
                event.scheduleVersionId?.let { this.scheduleVersion = ScheduleVersionEntity[it] }
                event.assetId?.let { this.asset = AssetEntity[it] }
                this.eventType = event.eventType
                this.at = event.at
                this.detailsJson = event.details
                this.createdAt = Clock.System.now()
            }
            count++
        }
        count
    }

    private fun buildWhereClause(tenantId: TenantId, filters: AsrunFilters): Op<Boolean> {
        var condition: Op<Boolean> = AsrunEvents.tenantId eq tenantId.value

        filters.deviceId?.let { deviceId ->
            condition = condition and (AsrunEvents.deviceId eq deviceId)
        }

        filters.channelId?.let { channelId ->
            condition = condition and (AsrunEvents.channelId eq channelId)
        }

        filters.assetId?.let { assetId ->
            condition = condition and (AsrunEvents.assetId eq assetId)
        }

        filters.eventType?.let { eventType ->
            condition = condition and (AsrunEvents.eventType eq eventType)
        }

        filters.from?.let { from ->
            condition = condition and (AsrunEvents.at greaterEq from)
        }

        filters.to?.let { to ->
            condition = condition and (AsrunEvents.at lessEq to)
        }

        return condition
    }

    override suspend fun findByDevice(
        tenantId: TenantId,
        deviceId: UUID,
        filters: AsrunFilters
    ): List<AsrunEventEntity> = newSuspendedTransaction {
        val filtersWithDevice = filters.copy(deviceId = deviceId)
        AsrunEventEntity.find { buildWhereClause(tenantId, filtersWithDevice) }
            .orderBy(AsrunEvents.at to SortOrder.DESC)
            .limit(filters.limit, filters.offset.toLong())
            .toList()
    }

    override suspend fun findByChannel(
        tenantId: TenantId,
        channelId: UUID,
        filters: AsrunFilters
    ): List<AsrunEventEntity> = newSuspendedTransaction {
        val filtersWithChannel = filters.copy(channelId = channelId)
        AsrunEventEntity.find { buildWhereClause(tenantId, filtersWithChannel) }
            .orderBy(AsrunEvents.at to SortOrder.DESC)
            .limit(filters.limit, filters.offset.toLong())
            .toList()
    }

    override suspend fun findByTenant(
        tenantId: TenantId,
        filters: AsrunFilters
    ): List<AsrunEventEntity> = newSuspendedTransaction {
        AsrunEventEntity.find { buildWhereClause(tenantId, filters) }
            .orderBy(AsrunEvents.at to SortOrder.DESC)
            .limit(filters.limit, filters.offset.toLong())
            .toList()
    }

    override suspend fun count(tenantId: TenantId, filters: AsrunFilters): Long = newSuspendedTransaction {
        AsrunEventEntity.find { buildWhereClause(tenantId, filters) }.count()
    }

    override suspend fun getSummary(tenantId: TenantId, filters: AsrunFilters): AsrunSummary = newSuspendedTransaction {
        val events = AsrunEventEntity.find { buildWhereClause(tenantId, filters) }.toList()

        val totalEvents = events.size.toLong()

        val byAsset = events
            .filter { it.asset != null }
            .groupBy { it.asset!!.id.value }
            .mapValues { (assetId, assetEvents) ->
                AssetPlaybackSummary(
                    assetId = assetId,
                    playCount = assetEvents.size.toLong()
                )
            }

        AsrunSummary(
            totalEvents = totalEvents,
            byAsset = byAsset
        )
    }

    override suspend fun deleteOlderThan(days: Int): Long = newSuspendedTransaction {
        val threshold = Clock.System.now().minus(days.days)
        AsrunEvents.deleteWhere { createdAt less threshold }.toLong()
    }
}
