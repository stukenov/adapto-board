package com.playoutedge.server.services

import com.playoutedge.domain.enums.ActorType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AuditLogEntity
import com.playoutedge.persistence.repositories.AuditFilters
import com.playoutedge.persistence.repositories.AuditRepository
import kotlinx.serialization.json.JsonObject
import java.util.UUID

data class AuditQueryResult(
    val items: List<AuditLogEntity>,
    val total: Long,
    val limit: Int,
    val offset: Int
)

class AuditService(
    private val auditRepo: AuditRepository
) {
    suspend fun log(
        tenantId: TenantId?,
        actorUserId: UUID?,
        actorType: ActorType,
        action: String,
        entityType: String,
        entityId: UUID,
        diff: JsonObject? = null
    ): AuditLogEntity {
        return auditRepo.log(tenantId, actorUserId, actorType, action, entityType, entityId, diff)
    }

    suspend fun query(tenantId: TenantId, filters: AuditFilters): AuditQueryResult {
        val items = auditRepo.findByTenant(tenantId, filters)
        val total = auditRepo.count(tenantId, filters)
        return AuditQueryResult(
            items = items,
            total = total,
            limit = filters.limit,
            offset = filters.offset
        )
    }

    suspend fun findByEntity(tenantId: TenantId, entityType: String, entityId: UUID): List<AuditLogEntity> {
        return auditRepo.findByEntity(tenantId, entityType, entityId)
    }

    suspend fun cleanup(retentionDays: Int = 180): Long {
        return auditRepo.deleteOlderThan(retentionDays)
    }
}
