package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.UserEntity
import com.playoutedge.persistence.repositories.UserRepository
import com.playoutedge.persistence.tables.UserRoles
import com.playoutedge.persistence.tables.Users
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class UserRepositoryImpl : UserRepository {

    override suspend fun findById(userId: UUID): UserEntity? = newSuspendedTransaction {
        UserEntity.findById(userId)
    }

    override suspend fun findByEmail(email: String): UserEntity? = newSuspendedTransaction {
        UserEntity.find { Users.email eq email.lowercase() }.firstOrNull()
    }

    override suspend fun findByTenantAndEmail(tenantId: TenantId, email: String): UserEntity? = newSuspendedTransaction {
        UserEntity.find {
            (Users.tenantId eq tenantId.value) and (Users.email eq email.lowercase())
        }.firstOrNull()
    }

    override suspend fun findAllByTenant(tenantId: TenantId): List<UserEntity> = newSuspendedTransaction {
        UserEntity.find { Users.tenantId eq tenantId.value }.toList()
    }

    override suspend fun create(
        tenantId: TenantId,
        email: String,
        displayName: String,
        passwordHash: String,
        role: UserRole
    ): UserEntity = newSuspendedTransaction {
        val now = Clock.System.now()
        val user = UserEntity.new {
            this.tenant = TenantEntity[tenantId.value]
            this.email = email.lowercase()
            this.displayName = displayName
            this.passwordHash = passwordHash
            this.status = UserStatus.ACTIVE
            this.createdAt = now
            this.inviteExpiresAt = now.plus(kotlin.time.Duration.parse("7d"))
        }

        // Add role
        UserRoles.insert {
            it[userId] = user.id
            it[UserRoles.role] = role
        }

        user
    }

    override suspend fun updatePassword(userId: UUID, newPasswordHash: String): UserEntity? = newSuspendedTransaction {
        val user = UserEntity.findById(userId) ?: return@newSuspendedTransaction null
        user.passwordHash = newPasswordHash
        user.updatedAt = Clock.System.now()
        user
    }

    override suspend fun updateStatus(userId: UUID, status: UserStatus): UserEntity? = newSuspendedTransaction {
        val user = UserEntity.findById(userId) ?: return@newSuspendedTransaction null
        user.status = status
        user.updatedAt = Clock.System.now()
        user
    }

    override suspend fun getRoles(userId: UUID): Set<UserRole> = newSuspendedTransaction {
        UserRoles.selectAll()
            .where { UserRoles.userId eq userId }
            .map { it[UserRoles.role] }
            .toSet()
    }
}
