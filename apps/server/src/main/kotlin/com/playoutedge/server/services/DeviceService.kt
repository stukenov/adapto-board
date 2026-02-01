package com.playoutedge.server.services

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.DeviceEntity
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.persistence.repositories.UpdateDeviceRequest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class DeviceService(
    private val deviceRepo: DeviceRepository
) {
    private val onlineThreshold = 5.minutes

    suspend fun findAll(tenantId: TenantId): List<DeviceEntity> {
        return deviceRepo.findAll(tenantId)
    }

    suspend fun findById(tenantId: TenantId, deviceId: UUID): DeviceEntity? {
        return deviceRepo.findById(tenantId, deviceId)
    }

    suspend fun findByChannel(tenantId: TenantId, channelId: UUID): List<DeviceEntity> {
        return deviceRepo.findByChannel(tenantId, channelId)
    }

    suspend fun update(
        tenantId: TenantId,
        deviceId: UUID,
        displayName: String? = null
    ): DeviceEntity? {
        return deviceRepo.update(tenantId, deviceId, UpdateDeviceRequest(displayName = displayName))
    }

    suspend fun assignChannel(tenantId: TenantId, deviceId: UUID, channelId: UUID?): DeviceEntity? {
        return deviceRepo.update(tenantId, deviceId, UpdateDeviceRequest(assignedChannelId = channelId))
    }

    fun isOnline(lastSeenAt: Instant?): Boolean {
        if (lastSeenAt == null) return false
        return (Clock.System.now() - lastSeenAt) < onlineThreshold
    }
}
