package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.ActorType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AuditLogEntity
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.UserEntity
import com.playoutedge.persistence.repositories.AuditFilters
import com.playoutedge.persistence.repositories.AuditRepository
import com.playoutedge.persistence.tables.AuditLog
import kotlinx.datetime.Clock
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

class AuditRepositoryImpl : AuditRepository {

    override suspend fun log(
        tenantId: TenantId?,
        actorUserId: UUID?,
        actorType: ActorType,
        action: String,
        entityType: String,
        entityId: UUID,
        diff: JsonObject?
    ): AuditLogEntity = newSuspendedTransaction {
        AuditLogEntity.new {
            tenantId?.let { this.tenant = TenantEntity[it.value] }
            actorUserId?.let { this.actorUser = UserEntity[it] }
            this.actorType = actorType
            this.action = action
            this.entityType = entityType
            this.entityId = entityId
            this.diffJson = diff
            this.createdAt = Clock.System.now()
        }
    }

    private fun buildWhereClause(tenantId: TenantId, filters: AuditFilters): Op<Boolean> {
        var condition: Op<Boolean> = AuditLog.tenantId eq tenantId.value

        filters.entityType?.let { entityType ->
            condition = condition and (AuditLog.entityType eq entityType)
        }

        filters.entityId?.let { entityId ->
            condition = condition and (AuditLog.entityId eq entityId)
        }

        filters.actorUserId?.let { actorUserId ->
            condition = condition and (AuditLog.actorUserId eq actorUserId)
        }

        filters.action?.let { action ->
            condition = condition and (AuditLog.action eq action)
        }

        filters.fromDate?.let { from ->
            condition = condition and (AuditLog.createdAt greaterEq from)
        }

        filters.toDate?.let { to ->
            condition = condition and (AuditLog.createdAt lessEq to)
        }

        return condition
    }

    override suspend fun findByTenant(
        tenantId: TenantId,
        filters: AuditFilters
    ): List<AuditLogEntity> = newSuspendedTransaction {
        AuditLogEntity.find { buildWhereClause(tenantId, filters) }
            .orderBy(AuditLog.createdAt to SortOrder.DESC)
            .limit(filters.limit, filters.offset.toLong())
            .toList()
    }

    override suspend fun findByEntity(
        tenantId: TenantId,
        entityType: String,
        entityId: UUID
    ): List<AuditLogEntity> = newSuspendedTransaction {
        AuditLogEntity.find {
            (AuditLog.tenantId eq tenantId.value) and
            (AuditLog.entityType eq entityType) and
            (AuditLog.entityId eq entityId)
        }.orderBy(AuditLog.createdAt to SortOrder.DESC).toList()
    }

    override suspend fun count(tenantId: TenantId, filters: AuditFilters): Long = newSuspendedTransaction {
        AuditLogEntity.find { buildWhereClause(tenantId, filters) }.count()
    }

    override suspend fun deleteOlderThan(days: Int): Long = newSuspendedTransaction {
        val threshold = Clock.System.now().minus(days.days)
        AuditLog.deleteWhere { createdAt less threshold }.toLong()
    }
}
