package com.playoutedge.persistence.repositories

import com.playoutedge.domain.enums.DeviceEnrollStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.DeviceEntity
import java.util.UUID

interface DeviceRepository {
    suspend fun findById(tenantId: TenantId, deviceId: UUID): DeviceEntity?
    suspend fun findAll(tenantId: TenantId): List<DeviceEntity>
    suspend fun findAllPaged(tenantId: TenantId, limit: Int = 50, offset: Int = 0): Pair<List<DeviceEntity>, Long>
    suspend fun findByStatus(tenantId: TenantId, status: DeviceEnrollStatus): List<DeviceEntity>
    suspend fun findByChannel(tenantId: TenantId, channelId: UUID): List<DeviceEntity>
    suspend fun update(tenantId: TenantId, deviceId: UUID, update: UpdateDeviceRequest): DeviceEntity?
    suspend fun updateHeartbeat(tenantId: TenantId, deviceId: UUID, appVersion: String?): DeviceEntity?
    suspend fun delete(tenantId: TenantId, deviceId: UUID): Boolean
    suspend fun getEnrolledCount(tenantId: TenantId): Long
}

data class UpdateDeviceRequest(
    val displayName: String? = null,
    val assignedChannelId: UUID? = null,
    val enrollStatus: DeviceEnrollStatus? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val powerOnTime: String? = null,
    val powerOffTime: String? = null
)
