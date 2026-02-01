package com.playoutedge.persistence.repositories

import com.playoutedge.domain.enums.ActionStatus
import com.playoutedge.domain.enums.ActionType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.DeviceActionEntity
import kotlinx.serialization.json.JsonObject
import java.util.UUID

interface DeviceActionRepository {
    suspend fun create(action: CreateDeviceAction): DeviceActionEntity
    suspend fun findPending(tenantId: TenantId, deviceId: UUID): List<DeviceActionEntity>
    suspend fun findByDevice(tenantId: TenantId, deviceId: UUID): List<DeviceActionEntity>
    suspend fun acknowledge(actionId: UUID, status: ActionStatus): DeviceActionEntity?
    suspend fun expireOldActions(): Long
}

data class CreateDeviceAction(
    val tenantId: TenantId,
    val deviceId: UUID,
    val actionType: ActionType,
    val params: JsonObject? = null,
    val createdBy: UUID? = null
)
