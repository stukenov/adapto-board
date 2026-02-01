package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.tenant.QuotaCheck
import com.playoutedge.domain.tenant.QuotaUsage
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.domain.tenant.TenantQuotas
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.persistence.repositories.QuotaService

class QuotaServiceImpl(
    private val assetRepository: AssetRepository,
    private val deviceRepository: DeviceRepository
) : QuotaService {

    override suspend fun getQuotas(tenantId: TenantId): TenantQuotas {
        // For now, return default quotas
        // In future, this would read from tenant settings
        return TenantQuotas.DEFAULT
    }

    override suspend fun getUsage(tenantId: TenantId): QuotaUsage {
        val storageBytes = assetRepository.getTotalStorageBytes(tenantId)
        val deviceCount = deviceRepository.getEnrolledCount(tenantId)

        return QuotaUsage(
            storageBytes = storageBytes,
            deviceCount = deviceCount
        )
    }

    override suspend fun checkStorageQuota(tenantId: TenantId, additionalBytes: Long): QuotaCheck {
        val quotas = getQuotas(tenantId)
        val usage = getUsage(tenantId)

        val newTotal = usage.storageBytes + additionalBytes
        return if (newTotal <= quotas.maxStorageBytes) {
            QuotaCheck(allowed = true)
        } else {
            QuotaCheck(
                allowed = false,
                reason = "Storage quota exceeded: ${newTotal / (1024 * 1024)}MB / ${quotas.maxStorageBytes / (1024 * 1024)}MB"
            )
        }
    }

    override suspend fun checkDeviceQuota(tenantId: TenantId): QuotaCheck {
        val quotas = getQuotas(tenantId)
        val usage = getUsage(tenantId)

        return if (usage.deviceCount < quotas.maxDevices) {
            QuotaCheck(allowed = true)
        } else {
            QuotaCheck(
                allowed = false,
                reason = "Device quota exceeded: ${usage.deviceCount} / ${quotas.maxDevices}"
            )
        }
    }
}
