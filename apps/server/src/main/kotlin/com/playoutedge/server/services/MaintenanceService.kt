package com.playoutedge.server.services

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.tables.Tenants
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

data class MaintenanceStatus(
    val maintenanceMode: Boolean,
    val reason: String?,
    val until: Instant?
)

class MaintenanceService {

    suspend fun enableMaintenance(
        tenantId: TenantId,
        reason: String,
        until: Instant? = null
    ): MaintenanceStatus = newSuspendedTransaction {
        val tenant = TenantEntity.findById(tenantId.value)
            ?: error("Tenant not found: ${tenantId.value}")

        tenant.maintenanceMode = true
        tenant.maintenanceReason = reason
        tenant.maintenanceUntil = until

        MaintenanceStatus(
            maintenanceMode = true,
            reason = reason,
            until = until
        )
    }

    suspend fun disableMaintenance(tenantId: TenantId): MaintenanceStatus = newSuspendedTransaction {
        val tenant = TenantEntity.findById(tenantId.value)
            ?: error("Tenant not found: ${tenantId.value}")

        tenant.maintenanceMode = false
        tenant.maintenanceReason = null
        tenant.maintenanceUntil = null

        MaintenanceStatus(
            maintenanceMode = false,
            reason = null,
            until = null
        )
    }

    suspend fun getStatus(tenantId: TenantId): MaintenanceStatus = newSuspendedTransaction {
        val tenant = TenantEntity.findById(tenantId.value)
            ?: error("Tenant not found: ${tenantId.value}")

        MaintenanceStatus(
            maintenanceMode = tenant.maintenanceMode,
            reason = tenant.maintenanceReason,
            until = tenant.maintenanceUntil
        )
    }

    suspend fun isInMaintenance(tenantId: TenantId): Boolean = newSuspendedTransaction {
        val tenant = TenantEntity.findById(tenantId.value)
            ?: return@newSuspendedTransaction false

        if (!tenant.maintenanceMode) return@newSuspendedTransaction false

        // Auto-disable if maintenance window has passed
        val until = tenant.maintenanceUntil
        if (until != null && until < kotlinx.datetime.Clock.System.now()) {
            tenant.maintenanceMode = false
            tenant.maintenanceReason = null
            tenant.maintenanceUntil = null
            return@newSuspendedTransaction false
        }

        true
    }
}
