package com.playoutedge.server.usecases

import com.playoutedge.auth.AdminClaims
import com.playoutedge.auth.AuthConfig
import com.playoutedge.auth.JwtService
import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.domain.enums.TenantStatus
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.persistence.entities.ChannelEntity
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.UserEntity
import com.playoutedge.persistence.repositories.UpdateChannelRequest
import com.playoutedge.persistence.repositories.impl.ChannelRepositoryImpl
import com.playoutedge.persistence.repositories.impl.DeviceRepositoryImpl
import com.playoutedge.persistence.repositories.impl.ScheduleRepositoryImpl
import com.playoutedge.persistence.tables.UserRoles
import com.playoutedge.server.DatabaseTestContainer
import com.playoutedge.server.plugins.AdminSessionPlugin
import com.playoutedge.server.plugins.configureErrorHandling
import com.playoutedge.server.routes.admin.adminChannelRoutes
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Use Case Tests: UC-3.1 Create Channel, UC-3.2 View, UC-3.3 Edit, UC-3.4 Pause/Resume, UC-3.5 Delete
 */
@DisplayName("Channel Management Use Cases")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("usecase")
class ChannelUseCaseTest : DatabaseTestContainer() {

    private lateinit var authConfig: AuthConfig
    private lateinit var jwtService: JwtService
    private lateinit var channelRepository: ChannelRepositoryImpl
    private lateinit var deviceRepository: DeviceRepositoryImpl
    private lateinit var scheduleRepository: ScheduleRepositoryImpl
    private lateinit var sessionToken: String

    private val testTenantId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testChannelId = UUID.randomUUID()

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
        channelRepository = ChannelRepositoryImpl()
        deviceRepository = DeviceRepositoryImpl()
        scheduleRepository = ScheduleRepositoryImpl()

        // Create test data
        transaction {
            val tenant = TenantEntity.new(testTenantId) {
                name = "Test Tenant"
                status = TenantStatus.ACTIVE
                createdAt = Clock.System.now()
            }

            val user = UserEntity.new(testUserId) {
                this.tenant = tenant
                email = "admin@test.com"
                displayName = "Admin"
                status = UserStatus.ACTIVE
                passwordHash = "hash"
                createdAt = Clock.System.now()
            }

            UserRoles.insert {
                it[userId] = user.id
                it[role] = UserRole.TENANT_ADMIN
            }

            // Create a test channel
            ChannelEntity.new(testChannelId) {
                this.tenant = tenant
                name = "Test Channel"
                status = ChannelStatus.ACTIVE
                createdAt = Clock.System.now()
            }
        }

        // Create session token
        sessionToken = jwtService.generateAdminAccessToken(
            AdminClaims(
                subject = testUserId,
                tenantId = testTenantId,
                role = UserRole.TENANT_ADMIN
            )
        )
    }

    private fun Application.configureTestApp() {
        install(ContentNegotiation) { json() }
        configureErrorHandling()
        install(AdminSessionPlugin) {
            this.jwtService = this@ChannelUseCaseTest.jwtService
        }

        routing {
            adminChannelRoutes(channelRepository, deviceRepository, scheduleRepository)
        }
    }

    private fun HttpRequestBuilder.withSession() {
        cookie("admin_session", sessionToken)
    }

    @Nested
    @DisplayName("UC-3.1: Create Channel")
    inner class CreateChannelTests {

        @Test
        @DisplayName("Should display create channel form")
        fun `should display create form`() = testApplication {
            application { configureTestApp() }

            val response = client.get("/admin/channels/new") {
                withSession()
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("<form"), "Should contain form")
            assertTrue(body.contains("name"), "Should have name field")
        }

        @Test
        @DisplayName("Should create channel with valid data")
        fun `should create channel`() = testApplication {
            application { configureTestApp() }

            val client = createClient { followRedirects = false }

            val response = client.post("/admin/channels") {
                withSession()
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("name=New Test Channel")
            }

            assertEquals(HttpStatusCode.Found, response.status)
            val location = response.headers["Location"]
            assertTrue(location?.contains("/admin/channels") == true, "Should redirect to channels")
        }

        @Test
        @DisplayName("Should reject empty channel name")
        fun `should reject empty name`() = testApplication {
            application { configureTestApp() }

            val response = client.post("/admin/channels") {
                withSession()
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("name=")
            }

            val body = response.bodyAsText()
            assertTrue(body.contains("error") || body.contains("required"), "Should show error")
        }
    }

    @Nested
    @DisplayName("UC-3.2: View Channel Details")
    inner class ViewChannelTests {

        @Test
        @DisplayName("Should display channel list")
        fun `should display channel list`() = testApplication {
            application { configureTestApp() }

            val response = client.get("/admin/channels") {
                withSession()
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("Test Channel"), "Should show channel name")
        }

        @Test
        @DisplayName("Should display channel details")
        fun `should display channel details`() = testApplication {
            application { configureTestApp() }

            val testClient = createClient { followRedirects = false }

            val response = testClient.get("/admin/channels/$testChannelId") {
                withSession()
            }

            // If redirect, means channel not found - print debug info
            if (response.status == HttpStatusCode.Found) {
                println("Got redirect to: ${response.headers["Location"]}")
                println("Test channel ID: $testChannelId")
                println("Test tenant ID: $testTenantId")
            }

            assertEquals(HttpStatusCode.OK, response.status, "Should return 200 OK, not redirect")
            val body = response.bodyAsText()
            assertTrue(body.contains("Test Channel") || body.contains(testChannelId.toString()), "Should show channel name or ID")
        }

        @Test
        @DisplayName("Should show 404 for non-existent channel")
        fun `should show 404 for non-existent`() = testApplication {
            application { configureTestApp() }

            val client = createClient { followRedirects = false }

            val response = client.get("/admin/channels/${UUID.randomUUID()}") {
                withSession()
            }

            // Should redirect to list or show not found
            assertTrue(
                response.status == HttpStatusCode.Found || response.status == HttpStatusCode.NotFound,
                "Should redirect or return 404"
            )
        }
    }

    @Nested
    @DisplayName("UC-3.3: Edit Channel")
    inner class EditChannelTests {

        @Test
        @DisplayName("Should display edit form")
        fun `should display edit form`() = testApplication {
            application { configureTestApp() }

            val testClient = createClient { followRedirects = false }

            val response = testClient.get("/admin/channels/$testChannelId/edit") {
                withSession()
            }

            // If redirect, means channel not found - print debug info
            if (response.status == HttpStatusCode.Found) {
                println("Got redirect to: ${response.headers["Location"]}")
                println("Test channel ID: $testChannelId")
                println("Test tenant ID: $testTenantId")
            }

            assertEquals(HttpStatusCode.OK, response.status, "Should return 200 OK, not redirect")
            val body = response.bodyAsText()
            assertTrue(body.contains("Test Channel") || body.contains(testChannelId.toString()), "Should show current name or ID")
            assertTrue(body.contains("<form"), "Should contain form")
        }

        @Test
        @DisplayName("Should update channel name")
        fun `should update channel name`() = testApplication {
            application { configureTestApp() }

            val client = createClient { followRedirects = false }

            val response = client.post("/admin/channels/$testChannelId") {
                withSession()
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("name=Updated Channel Name")
            }

            assertEquals(HttpStatusCode.Found, response.status)

            // Verify update
            val channel = runBlocking {
                channelRepository.findById(
                    com.playoutedge.domain.tenant.TenantId(testTenantId),
                    testChannelId
                )
            }
            assertEquals("Updated Channel Name", channel?.name)
        }
    }

    @Nested
    @DisplayName("UC-3.4: Pause/Resume Channel")
    inner class PauseResumeChannelTests {

        @Test
        @DisplayName("Should pause channel")
        fun `should pause channel`() = testApplication {
            application { configureTestApp() }

            val client = createClient { followRedirects = false }

            val response = client.post("/admin/channels/$testChannelId/pause") {
                withSession()
            }

            assertEquals(HttpStatusCode.Found, response.status)

            val channel = runBlocking {
                channelRepository.findById(
                    com.playoutedge.domain.tenant.TenantId(testTenantId),
                    testChannelId
                )
            }
            assertEquals(ChannelStatus.PAUSED, channel?.status)
        }

        @Test
        @DisplayName("Should resume channel")
        fun `should resume channel`() = testApplication {
            application { configureTestApp() }

            // First pause it
            runBlocking {
                channelRepository.update(
                    com.playoutedge.domain.tenant.TenantId(testTenantId),
                    testChannelId,
                    UpdateChannelRequest(status = ChannelStatus.PAUSED)
                )
            }

            val client = createClient { followRedirects = false }

            val response = client.post("/admin/channels/$testChannelId/resume") {
                withSession()
            }

            assertEquals(HttpStatusCode.Found, response.status)

            val channel = runBlocking {
                channelRepository.findById(
                    com.playoutedge.domain.tenant.TenantId(testTenantId),
                    testChannelId
                )
            }
            assertEquals(ChannelStatus.ACTIVE, channel?.status)
        }
    }
}
