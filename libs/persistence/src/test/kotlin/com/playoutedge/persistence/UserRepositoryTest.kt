package com.playoutedge.persistence

import com.playoutedge.domain.enums.TenantStatus
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.repositories.UserRepository
import com.playoutedge.persistence.repositories.impl.UserRepositoryImpl
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*

@DisplayName("UserRepository")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest : DatabaseTestBase() {

    private lateinit var userRepository: UserRepository
    private var tenantId: TenantId = TenantId(UUID.randomUUID())

    @BeforeAll
    fun setup() {
        initDatabase()
        userRepository = UserRepositoryImpl()

        // Create a test tenant
        tenantId = TenantId(UUID.randomUUID())
        transaction {
            TenantEntity.new(tenantId.value) {
                name = "Test Tenant for UserRepo"
                status = TenantStatus.ACTIVE
                createdAt = Clock.System.now()
            }
        }
    }

    @Nested
    @DisplayName("create()")
    inner class CreateTests {

        @Test
        @DisplayName("should create user with all fields")
        fun `create should persist user`() = runBlocking {
            val email = "create-test-${UUID.randomUUID()}@example.com"
            val displayName = "Create Test User"
            val passwordHash = "hashed_password"
            val role = UserRole.OPERATOR

            val user = userRepository.create(
                tenantId = tenantId,
                email = email,
                displayName = displayName,
                passwordHash = passwordHash,
                role = role
            )

            assertNotNull(user)
            assertEquals(email.lowercase(), user.email)
            assertEquals(displayName, user.displayName)
            assertEquals(passwordHash, user.passwordHash)
            assertEquals(UserStatus.ACTIVE, user.status)
        }

        @Test
        @DisplayName("should normalize email to lowercase")
        fun `create should lowercase email`() = runBlocking {
            val email = "UPPERCASE-${UUID.randomUUID()}@EXAMPLE.COM"

            val user = userRepository.create(
                tenantId = tenantId,
                email = email,
                displayName = "Test",
                passwordHash = "hash",
                role = UserRole.OPERATOR
            )

            assertEquals(email.lowercase(), user.email)
        }

        @Test
        @DisplayName("should assign role to user")
        fun `create should assign role`() = runBlocking {
            val email = "role-test-${UUID.randomUUID()}@example.com"
            val role = UserRole.TENANT_ADMIN

            val user = userRepository.create(
                tenantId = tenantId,
                email = email,
                displayName = "Test",
                passwordHash = "hash",
                role = role
            )

            val roles = userRepository.getRoles(user.id.value)
            assertTrue(role in roles)
        }
    }

    @Nested
    @DisplayName("findByEmail()")
    inner class FindByEmailTests {

        @Test
        @DisplayName("should find existing user by email")
        fun `findByEmail should return user`() = runBlocking {
            val email = "find-email-${UUID.randomUUID()}@example.com"
            userRepository.create(tenantId, email, "Test", "hash", UserRole.OPERATOR)

            val found = userRepository.findByEmail(email)

            assertNotNull(found)
            assertEquals(email.lowercase(), found!!.email)
        }

        @Test
        @DisplayName("should find user with case-insensitive email")
        fun `findByEmail should be case insensitive`() = runBlocking {
            val email = "case-test-${UUID.randomUUID()}@example.com"
            userRepository.create(tenantId, email, "Test", "hash", UserRole.OPERATOR)

            val found = userRepository.findByEmail(email.uppercase())

            assertNotNull(found)
        }

        @Test
        @DisplayName("should return null for non-existing email")
        fun `findByEmail should return null for missing user`() = runBlocking {
            val found = userRepository.findByEmail("nonexistent-${UUID.randomUUID()}@example.com")
            assertNull(found)
        }
    }

    @Nested
    @DisplayName("findById()")
    inner class FindByIdTests {

        @Test
        @DisplayName("should find user by ID")
        fun `findById should return user`() = runBlocking {
            val email = "find-id-${UUID.randomUUID()}@example.com"
            val created = userRepository.create(tenantId, email, "Test", "hash", UserRole.OPERATOR)

            val found = userRepository.findById(created.id.value)

            assertNotNull(found)
            assertEquals(created.id.value, found!!.id.value)
        }

        @Test
        @DisplayName("should return null for non-existing ID")
        fun `findById should return null for missing user`() = runBlocking {
            val found = userRepository.findById(UUID.randomUUID())
            assertNull(found)
        }
    }

    @Nested
    @DisplayName("updatePassword()")
    inner class UpdatePasswordTests {

        @Test
        @DisplayName("should update password hash")
        fun `updatePassword should change hash`() = runBlocking {
            val email = "update-pwd-${UUID.randomUUID()}@example.com"
            val user = userRepository.create(tenantId, email, "Test", "old_hash", UserRole.OPERATOR)
            val newHash = "new_hash_value"

            val updated = userRepository.updatePassword(user.id.value, newHash)

            assertNotNull(updated)
            assertEquals(newHash, updated!!.passwordHash)
        }

        @Test
        @DisplayName("should return null for non-existing user")
        fun `updatePassword should return null for missing user`() = runBlocking {
            val result = userRepository.updatePassword(UUID.randomUUID(), "new_hash")
            assertNull(result)
        }
    }

    @Nested
    @DisplayName("updateStatus()")
    inner class UpdateStatusTests {

        @Test
        @DisplayName("should update user status")
        fun `updateStatus should change status`() = runBlocking {
            val email = "update-status-${UUID.randomUUID()}@example.com"
            val user = userRepository.create(tenantId, email, "Test", "hash", UserRole.OPERATOR)

            val updated = userRepository.updateStatus(user.id.value, UserStatus.INACTIVE)

            assertNotNull(updated)
            assertEquals(UserStatus.INACTIVE, updated!!.status)
        }
    }

    @Nested
    @DisplayName("findAllByTenant()")
    inner class FindAllByTenantTests {

        @Test
        @DisplayName("should return all users in tenant")
        fun `findAllByTenant should return users`() = runBlocking {
            // Create a separate tenant for this test
            val testTenantId = TenantId(UUID.randomUUID())
            transaction {
                TenantEntity.new(testTenantId.value) {
                    name = "FindAll Test Tenant"
                    status = TenantStatus.ACTIVE
                    createdAt = Clock.System.now()
                }
            }

            val email1 = "tenant-user1-${UUID.randomUUID()}@example.com"
            val email2 = "tenant-user2-${UUID.randomUUID()}@example.com"
            userRepository.create(testTenantId, email1, "User 1", "hash", UserRole.OPERATOR)
            userRepository.create(testTenantId, email2, "User 2", "hash", UserRole.OPERATOR)

            val users = userRepository.findAllByTenant(testTenantId)

            assertEquals(2, users.size)
        }
    }

    @Nested
    @DisplayName("getRoles()")
    inner class GetRolesTests {

        @Test
        @DisplayName("should return user roles")
        fun `getRoles should return assigned roles`() = runBlocking {
            val email = "roles-test-${UUID.randomUUID()}@example.com"
            val user = userRepository.create(tenantId, email, "Test", "hash", UserRole.TENANT_ADMIN)

            val roles = userRepository.getRoles(user.id.value)

            assertTrue(UserRole.TENANT_ADMIN in roles)
        }

        @Test
        @DisplayName("should return empty set for non-existing user")
        fun `getRoles should return empty for missing user`() = runBlocking {
            val roles = userRepository.getRoles(UUID.randomUUID())
            assertTrue(roles.isEmpty())
        }
    }
}
