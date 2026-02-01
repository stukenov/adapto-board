package com.playoutedge.persistence.repositories

import com.playoutedge.domain.enums.AlertStatus
import com.playoutedge.domain.enums.AlertType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AlertEntity
import kotlinx.serialization.json.JsonObject
import java.util.UUID

interface AlertRepository {
    suspend fun create(
        tenantId: TenantId,
        type: AlertType,
        payload: JsonObject
    ): AlertEntity

    suspend fun findById(alertId: UUID): AlertEntity?

    suspend fun findByTenant(
        tenantId: TenantId,
        filters: AlertFilters
    ): List<AlertEntity>

    suspend fun count(tenantId: TenantId, filters: AlertFilters): Long

    suspend fun acknowledge(alertId: UUID): AlertEntity?

    suspend fun resolve(alertId: UUID): AlertEntity?

    suspend fun findOpenByType(
        tenantId: TenantId,
        type: AlertType
    ): AlertEntity?
}

data class AlertFilters(
    val status: AlertStatus? = null,
    val type: AlertType? = null,
    val limit: Int = 50,
    val offset: Int = 0
)
