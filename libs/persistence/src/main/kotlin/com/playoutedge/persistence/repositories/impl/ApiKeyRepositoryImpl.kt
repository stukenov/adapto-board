package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.ApiKeyEntity
import com.playoutedge.persistence.repositories.ApiKeyRepository
import com.playoutedge.persistence.tables.ApiKeys
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ApiKeyRepositoryImpl : ApiKeyRepository {

    override suspend fun create(
        tenantId: TenantId,
        name: String,
        keyHash: String,
        keyPrefix: String,
        scopes: List<String>,
        expiresAt: Instant?
    ): ApiKeyEntity = newSuspendedTransaction {
        ApiKeyEntity.new {
            this.tenantId = org.jetbrains.exposed.dao.id.EntityID(tenantId.value, com.playoutedge.persistence.tables.Tenants)
            this.name = name
            this.keyHash = keyHash
            this.keyPrefix = keyPrefix
            this.scopes = scopes.joinToString(",")
            this.expiresAt = expiresAt
            this.createdAt = Clock.System.now()
        }
    }

    override suspend fun findByTenant(tenantId: TenantId): List<ApiKeyEntity> = newSuspendedTransaction {
        ApiKeyEntity.find {
            (ApiKeys.tenantId eq tenantId.value) and ApiKeys.revokedAt.isNull()
        }.toList()
    }

    override suspend fun findByHash(keyHash: String): ApiKeyEntity? = newSuspendedTransaction {
        ApiKeyEntity.find {
            (ApiKeys.keyHash eq keyHash) and ApiKeys.revokedAt.isNull()
        }.firstOrNull()
    }

    override suspend fun revoke(tenantId: TenantId, keyId: UUID): Boolean = newSuspendedTransaction {
        val key = ApiKeyEntity.find {
            (ApiKeys.id eq keyId) and (ApiKeys.tenantId eq tenantId.value) and ApiKeys.revokedAt.isNull()
        }.firstOrNull() ?: return@newSuspendedTransaction false
        key.revokedAt = Clock.System.now()
        true
    }
}
