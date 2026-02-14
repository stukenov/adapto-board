package com.playoutedge.persistence.repositories.impl

import com.playoutedge.persistence.entities.RefreshTokenEntity
import com.playoutedge.persistence.repositories.RefreshTokenRepository
import com.playoutedge.persistence.tables.RefreshTokens
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class RefreshTokenRepositoryImpl : RefreshTokenRepository {

    override suspend fun store(
        tokenHash: String,
        subjectId: UUID,
        subjectType: String,
        tenantId: UUID,
        expiresAt: Instant
    ): RefreshTokenEntity = newSuspendedTransaction {
        RefreshTokenEntity.new {
            this.tokenHash = tokenHash
            this.subjectId = subjectId
            this.subjectType = subjectType
            this.tenantId = org.jetbrains.exposed.dao.id.EntityID(tenantId, com.playoutedge.persistence.tables.Tenants)
            this.expiresAt = expiresAt
            this.createdAt = Clock.System.now()
        }
    }

    override suspend fun findByHash(tokenHash: String): RefreshTokenEntity? = newSuspendedTransaction {
        RefreshTokenEntity.find {
            (RefreshTokens.tokenHash eq tokenHash) and RefreshTokens.revokedAt.isNull()
        }.firstOrNull()
    }

    override suspend fun revoke(tokenHash: String): Boolean = newSuspendedTransaction {
        val entity = RefreshTokenEntity.find {
            (RefreshTokens.tokenHash eq tokenHash) and RefreshTokens.revokedAt.isNull()
        }.firstOrNull() ?: return@newSuspendedTransaction false
        entity.revokedAt = Clock.System.now()
        true
    }

    override suspend fun revokeAllForSubject(subjectId: UUID): Int = newSuspendedTransaction {
        val tokens = RefreshTokenEntity.find {
            (RefreshTokens.subjectId eq subjectId) and RefreshTokens.revokedAt.isNull()
        }.toList()
        val now = Clock.System.now()
        tokens.forEach { it.revokedAt = now }
        tokens.size
    }

    override suspend fun cleanExpired(): Int = newSuspendedTransaction {
        RefreshTokens.deleteWhere { expiresAt less Clock.System.now() }
    }
}
