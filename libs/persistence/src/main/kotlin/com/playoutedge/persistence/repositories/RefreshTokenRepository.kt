package com.playoutedge.persistence.repositories

import com.playoutedge.persistence.entities.RefreshTokenEntity
import java.util.UUID

interface RefreshTokenRepository {
    suspend fun store(tokenHash: String, subjectId: UUID, subjectType: String, tenantId: UUID, expiresAt: kotlinx.datetime.Instant): RefreshTokenEntity
    suspend fun findByHash(tokenHash: String): RefreshTokenEntity?
    suspend fun revoke(tokenHash: String): Boolean
    suspend fun revokeAllForSubject(subjectId: UUID): Int
    suspend fun cleanExpired(): Int
}
