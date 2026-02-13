package com.playoutedge.server.services

import com.playoutedge.auth.PasswordService
import com.playoutedge.domain.enums.TenantStatus
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.UserEntity
import com.playoutedge.persistence.repositories.TenantRepository
import com.playoutedge.persistence.repositories.UserRepository
import com.playoutedge.persistence.tables.UserRoles
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

data class RegistrationResult(
    val tenant: TenantEntity,
    val user: UserEntity
)

sealed class RegistrationError(val message: String) {
    class InvalidTenantName : RegistrationError("Organization name must be between 3 and 100 characters")
    class InvalidEmail : RegistrationError("Please enter a valid email address")
    class InvalidPassword : RegistrationError("Password must be at least 8 characters with at least one letter and one digit")
    class EmailAlreadyExists : RegistrationError("An account with this email already exists")
}

class RegistrationService(
    private val tenantRepository: TenantRepository,
    private val userRepository: UserRepository,
    private val passwordService: PasswordService
) {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")

    suspend fun registerTenant(
        tenantName: String,
        email: String,
        displayName: String,
        password: String
    ): Result<RegistrationResult> {
        // Validate tenant name
        if (tenantName.length !in 3..100) {
            return Result.failure(IllegalArgumentException(RegistrationError.InvalidTenantName().message))
        }

        // Validate email
        if (!emailRegex.matches(email)) {
            return Result.failure(IllegalArgumentException(RegistrationError.InvalidEmail().message))
        }

        // Validate password
        if (!passwordRegex.matches(password)) {
            return Result.failure(IllegalArgumentException(RegistrationError.InvalidPassword().message))
        }

        // Check email uniqueness
        val existingUser = userRepository.findByEmail(email.lowercase())
        if (existingUser != null) {
            return Result.failure(IllegalArgumentException(RegistrationError.EmailAlreadyExists().message))
        }

        // Hash password before transaction (CPU-intensive)
        val passwordHash = passwordService.hash(password)

        // Create tenant + admin user in a single transaction
        val registration = newSuspendedTransaction {
            val tenant = TenantEntity.new {
                this.name = tenantName
                this.status = TenantStatus.ACTIVE
                this.createdAt = Clock.System.now()
            }

            val user = UserEntity.new {
                this.tenant = tenant
                this.email = email.lowercase()
                this.displayName = displayName
                this.passwordHash = passwordHash
                this.status = UserStatus.ACTIVE
                this.createdAt = Clock.System.now()
            }

            UserRoles.insert {
                it[userId] = user.id
                it[role] = UserRole.TENANT_ADMIN
            }

            RegistrationResult(tenant, user)
        }

        return Result.success(registration)
    }
}
