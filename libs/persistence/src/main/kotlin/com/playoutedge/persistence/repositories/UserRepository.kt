package com.playoutedge.persistence.repositories

import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.UserEntity
import java.util.UUID

interface UserRepository {
    suspend fun findById(userId: UUID): UserEntity?

    suspend fun findByEmail(email: String): UserEntity?

    suspend fun findByTenantAndEmail(tenantId: TenantId, email: String): UserEntity?

    suspend fun findAllByTenant(tenantId: TenantId): List<UserEntity>

    suspend fun create(
        tenantId: TenantId,
        email: String,
        displayName: String,
        passwordHash: String,
        role: UserRole
    ): UserEntity

    suspend fun updatePassword(userId: UUID, newPasswordHash: String): UserEntity?

    suspend fun updateStatus(userId: UUID, status: UserStatus): UserEntity?

    suspend fun getRoles(userId: UUID): Set<UserRole>
}
