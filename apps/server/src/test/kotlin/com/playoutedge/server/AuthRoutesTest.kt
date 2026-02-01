package com.playoutedge.server

import com.playoutedge.auth.AuthConfig
import com.playoutedge.auth.JwtService
import com.playoutedge.auth.PasswordService
import com.playoutedge.domain.enums.TenantStatus
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.UserEntity
import com.playoutedge.persistence.tables.UserRoles
import com.playoutedge.server.routes.authRoutes
import com.playoutedge.server.plugins.RequestIdPlugin
import com.playoutedge.server.plugins.configureErrorHandling
import com.playoutedge.server.plugins.configureJwtAuth
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@DisplayName("Auth Routes")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthRoutesTest : DatabaseTestContainer() {

    private lateinit var authConfig: AuthConfig
    private lateinit var jwtService: JwtService
    private lateinit var passwordService: PasswordService

    private val testTenantId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testEmail = "test@example.com"
    private val testPassword = "TestPassword123!"

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
        passwordService = PasswordService(costFactor = 10) // Lower cost for faster tests

        // Create test data
        transaction {
            val tenant = TenantEntity.new(testTenantId) {
                name = "Test Tenant"
                status = TenantStatus.ACTIVE
                createdAt = Clock.System.now()
            }

            val user = UserEntity.new(testUserId) {
                this.tenant = tenant
                email = testEmail
                displayName = "Test User"
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

    private fun Application.configureTestRouting() {
        install(RequestIdPlugin)
        install(ContentNegotiation) {
            json()
        }
        configureErrorHandling()
        configureJwtAuth(authConfig)

        routing {
            authRoutes(jwtService, passwordService, authConfig)
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    inner class LoginTests {

        @Test
        @DisplayName("should return access token for valid credentials")
        fun `login should return token for valid credentials`() = testApplication {
            application { configureTestRouting() }

            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email": "$testEmail", "password": "$testPassword"}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertNotNull(body["accessToken"]?.jsonPrimitive?.content)
            assertNotNull(body["expiresIn"]?.jsonPrimitive?.content)
        }

        @Test
        @DisplayName("should reject invalid email")
        fun `login should reject invalid email`() = testApplication {
            application { configureTestRouting() }

            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email": "wrong@example.com", "password": "$testPassword"}""")
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        @DisplayName("should reject invalid password")
        fun `login should reject invalid password`() = testApplication {
            application { configureTestRouting() }

            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email": "$testEmail", "password": "wrongpassword"}""")
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        @Test
        @DisplayName("should set refresh token cookie")
        fun `login should set refresh token cookie`() = testApplication {
            application { configureTestRouting() }

            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"email": "$testEmail", "password": "$testPassword"}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val cookies = response.headers.getAll("Set-Cookie")
            assertTrue(cookies?.any { it.contains("refresh_token") } == true)
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    inner class LogoutTests {

        @Test
        @DisplayName("should clear refresh token cookie")
        fun `logout should clear cookie`() = testApplication {
            application { configureTestRouting() }

            val response = client.post("/api/auth/logout")

            assertEquals(HttpStatusCode.NoContent, response.status)
            val cookies = response.headers.getAll("Set-Cookie")
            // Cookie should be expired (either Max-Age=0 or Expires with past date)
            assertTrue(
                cookies?.any {
                    it.contains("refresh_token") && (it.contains("Max-Age=0") || it.contains("Expires="))
                } == true
            )
        }
    }
}
