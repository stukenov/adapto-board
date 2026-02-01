package com.playoutedge.persistence.repositories

import com.playoutedge.domain.tenant.QuotaCheck
import com.playoutedge.domain.tenant.QuotaUsage
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.domain.tenant.TenantQuotas

interface QuotaService {
    suspend fun getQuotas(tenantId: TenantId): TenantQuotas
    suspend fun getUsage(tenantId: TenantId): QuotaUsage
    suspend fun checkStorageQuota(tenantId: TenantId, additionalBytes: Long): QuotaCheck
    suspend fun checkDeviceQuota(tenantId: TenantId): QuotaCheck
}
