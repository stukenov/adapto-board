package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.AlertStatus
import com.playoutedge.domain.enums.AlertType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AlertEntity
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.repositories.AlertFilters
import com.playoutedge.persistence.repositories.AlertRepository
import com.playoutedge.persistence.tables.Alerts
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AlertRepositoryImpl : AlertRepository {

    override suspend fun create(
        tenantId: TenantId,
        type: AlertType,
        payload: JsonObject
    ): AlertEntity = newSuspendedTransaction {
        val now = Clock.System.now()
        AlertEntity.new {
            this.tenant = TenantEntity[tenantId.value]
            this.type = type
            this.status = AlertStatus.OPEN
            this.payloadJson = payload
            this.firstSeenAt = now
            this.lastSeenAt = now
        }
    }

    override suspend fun findById(alertId: UUID): AlertEntity? = newSuspendedTransaction {
        AlertEntity.findById(alertId)
    }

    private fun buildWhereClause(tenantId: TenantId, filters: AlertFilters): Op<Boolean> {
        var condition: Op<Boolean> = Alerts.tenantId eq tenantId.value

        filters.status?.let { status ->
            condition = condition and (Alerts.status eq status)
        }

        filters.type?.let { type ->
            condition = condition and (Alerts.type eq type)
        }

        return condition
    }

    override suspend fun findByTenant(
        tenantId: TenantId,
        filters: AlertFilters
    ): List<AlertEntity> = newSuspendedTransaction {
        AlertEntity.find { buildWhereClause(tenantId, filters) }
            .orderBy(Alerts.lastSeenAt to SortOrder.DESC)
            .limit(filters.limit, filters.offset.toLong())
            .toList()
    }

    override suspend fun count(tenantId: TenantId, filters: AlertFilters): Long = newSuspendedTransaction {
        AlertEntity.find { buildWhereClause(tenantId, filters) }.count()
    }

    override suspend fun acknowledge(alertId: UUID): AlertEntity? = newSuspendedTransaction {
        val alert = AlertEntity.findById(alertId) ?: return@newSuspendedTransaction null
        if (alert.status == AlertStatus.OPEN) {
            alert.status = AlertStatus.ACKNOWLEDGED
        }
        alert
    }

    override suspend fun resolve(alertId: UUID): AlertEntity? = newSuspendedTransaction {
        val alert = AlertEntity.findById(alertId) ?: return@newSuspendedTransaction null
        if (alert.status != AlertStatus.RESOLVED) {
            alert.status = AlertStatus.RESOLVED
            alert.resolvedAt = Clock.System.now()
        }
        alert
    }

    override suspend fun findOpenByType(
        tenantId: TenantId,
        type: AlertType
    ): AlertEntity? = newSuspendedTransaction {
        AlertEntity.find {
            (Alerts.tenantId eq tenantId.value) and
            (Alerts.type eq type) and
            (Alerts.status eq AlertStatus.OPEN)
        }.firstOrNull()
    }
}
