package com.playoutedge.persistence.repositories

import com.playoutedge.domain.enums.DeviceEnrollStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.DeviceEntity
import java.util.UUID

interface DeviceRepository {
    suspend fun findById(tenantId: TenantId, deviceId: UUID): DeviceEntity?
    suspend fun findAll(tenantId: TenantId): List<DeviceEntity>
    suspend fun findByStatus(tenantId: TenantId, status: DeviceEnrollStatus): List<DeviceEntity>
    suspend fun findByChannel(tenantId: TenantId, channelId: UUID): List<DeviceEntity>
    suspend fun update(tenantId: TenantId, deviceId: UUID, update: UpdateDeviceRequest): DeviceEntity?
    suspend fun delete(tenantId: TenantId, deviceId: UUID): Boolean
    suspend fun getEnrolledCount(tenantId: TenantId): Long
}

data class UpdateDeviceRequest(
    val displayName: String? = null,
    val assignedChannelId: UUID? = null,
    val enrollStatus: DeviceEnrollStatus? = null
)
