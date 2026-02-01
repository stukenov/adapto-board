package com.playoutedge.persistence.repositories

import com.playoutedge.domain.enums.ActorType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AuditLogEntity
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import java.util.UUID

interface AuditRepository {
    suspend fun log(
        tenantId: TenantId?,
        actorUserId: UUID?,
        actorType: ActorType,
        action: String,
        entityType: String,
        entityId: UUID,
        diff: JsonObject? = null
    ): AuditLogEntity

    suspend fun findByTenant(
        tenantId: TenantId,
        filters: AuditFilters
    ): List<AuditLogEntity>

    suspend fun findByEntity(
        tenantId: TenantId,
        entityType: String,
        entityId: UUID
    ): List<AuditLogEntity>

    suspend fun count(tenantId: TenantId, filters: AuditFilters): Long

    suspend fun deleteOlderThan(days: Int): Long
}

data class AuditFilters(
    val entityType: String? = null,
    val entityId: UUID? = null,
    val actorUserId: UUID? = null,
    val action: String? = null,
    val fromDate: Instant? = null,
    val toDate: Instant? = null,
    val limit: Int = 100,
    val offset: Int = 0
)
