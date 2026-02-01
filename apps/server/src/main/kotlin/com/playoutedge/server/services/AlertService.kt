package com.playoutedge.server.services

import com.playoutedge.domain.enums.AlertStatus
import com.playoutedge.domain.enums.AlertType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AlertEntity
import com.playoutedge.persistence.repositories.AlertFilters
import com.playoutedge.persistence.repositories.AlertRepository
import kotlinx.serialization.json.JsonObject

data class AlertQueryResult(
    val alerts: List<AlertEntity>,
    val total: Long,
    val limit: Int,
    val offset: Int
)

class AlertService(
    private val alertRepo: AlertRepository
) {
    suspend fun createAlert(
        tenantId: TenantId,
        type: AlertType,
        payload: JsonObject
    ): AlertEntity {
        // Check if there's already an open alert of this type
        val existing = alertRepo.findOpenByType(tenantId, type)
        if (existing != null) {
            // Update last seen time would be handled by a separate method
            return existing
        }
        return alertRepo.create(tenantId, type, payload)
    }

    suspend fun query(
        tenantId: TenantId,
        status: AlertStatus? = null,
        type: AlertType? = null,
        limit: Int = 50,
        offset: Int = 0
    ): AlertQueryResult {
        val filters = AlertFilters(
            status = status,
            type = type,
            limit = limit,
            offset = offset
        )
        val alerts = alertRepo.findByTenant(tenantId, filters)
        val total = alertRepo.count(tenantId, filters)
        return AlertQueryResult(
            alerts = alerts,
            total = total,
            limit = limit,
            offset = offset
        )
    }

    suspend fun acknowledge(tenantId: TenantId, alertId: java.util.UUID): AlertEntity? {
        val alert = alertRepo.findById(alertId) ?: return null
        // Verify tenant ownership
        if (alert.tenant.id.value != tenantId.value) return null
        return alertRepo.acknowledge(alertId)
    }

    suspend fun resolve(tenantId: TenantId, alertId: java.util.UUID): AlertEntity? {
        val alert = alertRepo.findById(alertId) ?: return null
        // Verify tenant ownership
        if (alert.tenant.id.value != tenantId.value) return null
        return alertRepo.resolve(alertId)
    }
}
