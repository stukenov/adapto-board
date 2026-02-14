package com.playoutedge.persistence.repositories

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.ApiKeyEntity
import java.util.UUID

interface ApiKeyRepository {
    suspend fun create(tenantId: TenantId, name: String, keyHash: String, keyPrefix: String, scopes: List<String>, expiresAt: kotlinx.datetime.Instant?): ApiKeyEntity
    suspend fun findByTenant(tenantId: TenantId): List<ApiKeyEntity>
    suspend fun findByHash(keyHash: String): ApiKeyEntity?
    suspend fun revoke(tenantId: TenantId, keyId: UUID): Boolean
}
