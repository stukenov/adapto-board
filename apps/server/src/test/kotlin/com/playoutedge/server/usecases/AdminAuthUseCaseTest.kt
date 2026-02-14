package com.playoutedge.server.usecases

import com.playoutedge.auth.AuthConfig
import com.playoutedge.auth.JwtService
import com.playoutedge.auth.PasswordService
import com.playoutedge.domain.enums.TenantStatus
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.UserEntity
import com.playoutedge.persistence.tables.UserRoles
import com.playoutedge.server.DatabaseTestContainer
import com.playoutedge.server.routes.admin.adminAuthRoutes
import com.playoutedge.server.plugins.AdminSessionPlugin
import com.playoutedge.server.views.authLayout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.datetime.Clock
import kotlinx.html.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import com.playoutedge.auth.NoOpEmailService
import com.playoutedge.persistence.repositories.impl.TenantRepositoryImpl
import com.playoutedge.persistence.repositories.impl.UserRepositoryImpl
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Use Case Tests: UC-1.3 Login, UC-1.4 Password Reset, UC-10.1 Logout
 */
@DisplayName("Admin Authentication Use Cases")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("usecase")
class AdminAuthUseCaseTest : DatabaseTestContainer() {

    private lateinit var authConfig: AuthConfig
    private lateinit var jwtService: JwtService
    private lateinit var passwordService: PasswordService
    private lateinit var userRepository: UserRepositoryImpl

    private val testTenantId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testEmail = "admin@test.com"
    private val testPassword = "testpassword"

    @BeforeAll
    fun setup() {
        initDatabase()

        authConfig = AuthConfig(
            jwtSecret = "test-secret-key-at-least-32-chars-long",
            jwtIssuer = "test-issuer",
            jwtAudience = "test-audience",
            adminAccessTokenTtl = 15.minutes,
            adminRefreshTokenTtl = 7.days,
            deviceAccessTokenTtl = 1.hours,
            deviceRefreshTokenTtl = 90.days,
            enrollCodeTtl = 30.minutes
        )
        jwtService = JwtService(authConfig)
        passwordService = PasswordService(costFactor = 10)
        userRepository = UserRepositoryImpl()

        // Create test tenant and admin user
        transaction {
            val tenant = TenantEntity.new(testTenantId) {
                name = "Test Tenant"
                status = TenantStatus.ACTIVE
                createdAt = Clock.System.now()
            }

            val user = UserEntity.new(testUserId) {
                this.tenant = tenant
                email = testEmail
                displayName = "Admin User"
                status = UserStatus.ACTIVE
                passwordHash = passwordService.hash(testPassword)
                createdAt = Clock.System.now()
            }

            UserRoles.insert {
                it[userId] = user.id
                it[role] = UserRole.TENANT_ADMIN
            }
        }
    }

    private fun Application.configureTestApp() {
        install(ContentNegotiation) { json() }
        install(AdminSessionPlugin) {
            this.jwtService = this@AdminAuthUseCaseTest.jwtService
        }

        routing {
            adminAuthRoutes(userRepository, jwtService, passwordService, TenantRepositoryImpl(), NoOpEmailService())
        }
    }

    @Nested
    @DisplayName("UC-1.3: Login to Admin Panel")
    inner class LoginUseCaseTests {

        @Test
        @DisplayName("Login page should display form")
        fun `login page should display form`() = testApplication {
            application { configureTestApp() }

            val response = client.get("/admin/login")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("<form"), "Should contain form")
            assertTrue(body.contains("email"), "Should have email field")
            assertTrue(body.contains("password"), "Should have password field")
        }

        @Test
        @DisplayName("Valid credentials should redirect to dashboard")
        fun `valid credentials should redirect to dashboard`() = testApplication {
            application { configureTestApp() }

            val client = createClient { followRedirects = false }

            val response = client.post("/admin/login") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("email=$testEmail&password=$testPassword")
            }

            assertEquals(HttpStatusCode.Found, response.status)
            val location = response.headers["Location"]
            assertTrue(location?.contains("/admin") == true, "Should redirect to admin")
        }

        @Test
        @DisplayName("Invalid credentials should show error")
        fun `invalid credentials should show error`() = testApplication {
            application { configureTestApp() }

            val client = createClient { followRedirects = false }

            val response = client.post("/admin/login") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("email=wrong@example.com&password=wrongpass")
            }

            assertEquals(HttpStatusCode.Found, response.status)
            val location = response.headers["Location"]
            assertTrue(location?.contains("error=invalid") == true, "Should redirect with error")
        }

        @Test
        @DisplayName("Login should set session cookie")
        fun `login should set session cookie`() = testApplication {
            application { configureTestApp() }

            val client = createClient { followRedirects = false }

            val response = client.post("/admin/login") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("email=$testEmail&password=$testPassword")
            }

            val cookies = response.headers.getAll("Set-Cookie")
            assertTrue(cookies?.any { it.contains("admin_session") } == true, "Should set session cookie")
        }

        @Test
        @DisplayName("Empty email should show error")
        fun `empty email should show error`() = testApplication {
            application { configureTestApp() }

            val client = createClient { followRedirects = false }

            val response = client.post("/admin/login") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("email=&password=somepass")
            }

            assertEquals(HttpStatusCode.Found, response.status)
            val location = response.headers["Location"]
            assertTrue(location?.contains("error") == true, "Should redirect with error")
        }
    }

    @Nested
    @DisplayName("UC-10.1: Logout")
    inner class LogoutUseCaseTests {

        @Test
        @DisplayName("Logout should clear session")
        fun `logout should clear session`() = testApplication {
            application { configureTestApp() }

            val client = createClient { followRedirects = false }

            val response = client.post("/admin/logout")

            assertEquals(HttpStatusCode.Found, response.status)
            val location = response.headers["Location"]
            assertTrue(location?.contains("/login") == true, "Should redirect to login")
        }

        @Test
        @DisplayName("Logout should invalidate session cookie")
        fun `logout should invalidate session cookie`() = testApplication {
            application { configureTestApp() }

            val client = createClient { followRedirects = false }

            val response = client.post("/admin/logout")

            val cookies = response.headers.getAll("Set-Cookie")
            val sessionCookie = cookies?.find { it.contains("admin_session") }
            assertTrue(
                sessionCookie?.contains("Max-Age=0") == true ||
                sessionCookie?.contains("Expires=") == true,
                "Session cookie should be expired"
            )
        }
    }

    @Nested
    @DisplayName("UC-10.2: Session Timeout")
    inner class SessionTimeoutTests {

        @Test
        @DisplayName("Unauthenticated request should redirect to login")
        fun `unauthenticated request should redirect to login`() = testApplication {
            application { configureTestApp() }

            val client = createClient { followRedirects = false }

            // Try to access protected route without session
            val response = client.get("/admin")

            assertEquals(HttpStatusCode.Found, response.status)
            val location = response.headers["Location"]
            assertTrue(location?.contains("/login") == true, "Should redirect to login")
        }
    }
}
