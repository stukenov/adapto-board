package com.playoutedge.server.services

import com.playoutedge.domain.enums.ActionStatus
import com.playoutedge.domain.enums.ActionType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.DeviceActionEntity
import com.playoutedge.persistence.repositories.CreateDeviceAction
import com.playoutedge.persistence.repositories.DeviceActionRepository
import kotlinx.serialization.json.JsonObject
import java.util.UUID

class DeviceActionService(
    private val actionRepo: DeviceActionRepository
) {
    suspend fun createAction(
        tenantId: TenantId,
        deviceId: UUID,
        actionType: ActionType,
        params: JsonObject? = null,
        createdBy: UUID? = null
    ): DeviceActionEntity {
        return actionRepo.create(
            CreateDeviceAction(
                tenantId = tenantId,
                deviceId = deviceId,
                actionType = actionType,
                params = params,
                createdBy = createdBy
            )
        )
    }

    suspend fun getPendingActions(tenantId: TenantId, deviceId: UUID): List<DeviceActionEntity> {
        return actionRepo.findPending(tenantId, deviceId)
    }

    suspend fun getActionHistory(tenantId: TenantId, deviceId: UUID): List<DeviceActionEntity> {
        return actionRepo.findByDevice(tenantId, deviceId)
    }

    suspend fun acknowledgeAction(actionId: UUID, status: ActionStatus): DeviceActionEntity? {
        return actionRepo.acknowledge(actionId, status)
    }

    suspend fun expireOldActions(): Long {
        return actionRepo.expireOldActions()
    }
}
