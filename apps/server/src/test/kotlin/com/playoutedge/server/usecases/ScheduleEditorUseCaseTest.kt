package com.playoutedge.server.usecases

import com.playoutedge.auth.AdminClaims
import com.playoutedge.auth.AuthConfig
import com.playoutedge.auth.JwtService
import com.playoutedge.domain.enums.*
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.*
import com.playoutedge.persistence.repositories.impl.AssetRepositoryImpl
import com.playoutedge.persistence.repositories.impl.ChannelRepositoryImpl
import com.playoutedge.persistence.repositories.impl.ScheduleRepositoryImpl
import com.playoutedge.persistence.tables.UserRoles
import com.playoutedge.server.DatabaseTestContainer
import com.playoutedge.server.plugins.AdminSessionPlugin
import com.playoutedge.server.plugins.configureErrorHandling
import com.playoutedge.server.routes.admin.adminScheduleRoutes
import com.playoutedge.server.routes.admin.adminChannelRoutes
import com.playoutedge.server.services.ScheduleService
import com.playoutedge.storage.StorageResult
import com.playoutedge.storage.StorageService
import com.playoutedge.persistence.repositories.impl.DeviceRepositoryImpl
import java.io.InputStream
import kotlin.time.Duration
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@DisplayName("Schedule Editor Use Cases")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("usecase")
class ScheduleEditorUseCaseTest : DatabaseTestContainer() {

    private lateinit var jwtService: JwtService
    private lateinit var channelRepository: ChannelRepositoryImpl
    private lateinit var scheduleRepository: ScheduleRepositoryImpl
    private lateinit var assetRepository: AssetRepositoryImpl
    private lateinit var deviceRepository: DeviceRepositoryImpl
    private lateinit var scheduleService: ScheduleService
    private lateinit var sessionToken: String

    private val testTenantId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()
    private val testChannelId = UUID.randomUUID()
    private val testAssetId1 = UUID.randomUUID()
    private val testAssetId2 = UUID.randomUUID()
    private val testAssetId3 = UUID.randomUUID()

    @BeforeAll
    fun setup() {
        initDatabase()

        val authConfig = AuthConfig(
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
        scheduleRepository = ScheduleRepositoryImpl()
        assetRepository = AssetRepositoryImpl()
        deviceRepository = DeviceRepositoryImpl()
        scheduleService = ScheduleService(scheduleRepository, assetRepository)

        transaction {
            val tenant = TenantEntity.new(testTenantId) {
                name = "Schedule Test Tenant"
                status = TenantStatus.ACTIVE
                createdAt = Clock.System.now()
            }

            val user = UserEntity.new(testUserId) {
                this.tenant = tenant
                email = "schedule-admin@test.com"
                displayName = "Schedule Admin"
                status = UserStatus.ACTIVE
                passwordHash = "hash"
                createdAt = Clock.System.now()
            }

            UserRoles.insert {
                it[userId] = user.id
                it[role] = UserRole.TENANT_ADMIN
            }

            ChannelEntity.new(testChannelId) {
                this.tenant = tenant
                name = "Schedule Test Channel"
                status = ChannelStatus.ACTIVE
                createdAt = Clock.System.now()
            }

            // Create test assets
            AssetEntity.new(testAssetId1) {
                this.tenant = tenant
                type = AssetType.VIDEO
                name = "test-video-1.mp4"
                status = AssetStatus.READY
                durationMs = 30000
                mimeType = "video/mp4"
                storageKey = "test/video1.mp4"
                fileSizeBytes = 1024000
                createdAt = Clock.System.now()
            }

            AssetEntity.new(testAssetId2) {
                this.tenant = tenant
                type = AssetType.IMAGE
                name = "test-image-1.png"
                status = AssetStatus.READY
                durationMs = 10000
                mimeType = "image/png"
                storageKey = "test/image1.png"
                fileSizeBytes = 512000
                createdAt = Clock.System.now()
            }

            AssetEntity.new(testAssetId3) {
                this.tenant = tenant
                type = AssetType.VIDEO
                name = "test-video-2.mp4"
                status = AssetStatus.READY
                durationMs = 60000
                mimeType = "video/mp4"
                storageKey = "test/video2.mp4"
                fileSizeBytes = 2048000
                createdAt = Clock.System.now()
            }
        }

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
            this.jwtService = this@ScheduleEditorUseCaseTest.jwtService
        }
        routing {
            adminScheduleRoutes(channelRepository, scheduleRepository, assetRepository, scheduleService)
            adminChannelRoutes(channelRepository, deviceRepository, scheduleRepository, object : StorageService {
                override suspend fun put(key: String, content: InputStream, contentLength: Long) = StorageResult(key, "", contentLength)
                override suspend fun delete(key: String) = true
                override suspend fun getSignedUrl(key: String, ttl: Duration) = "/mock-storage/$key"
                override suspend fun exists(key: String) = true
            })
        }
    }

    private fun HttpRequestBuilder.withSession() {
        cookie("admin_session", sessionToken)
    }

    @Nested
    @DisplayName("Schedule Editor Page")
    inner class ScheduleEditorPageTests {

        @Test
        @DisplayName("Should display schedule editor with library assets")
        fun `should display schedule editor`() = testApplication {
            application { configureTestApp() }

            val response = client.get("/admin/channels/$testChannelId/schedule") {
                withSession()
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("Schedule Editor"), "Should show editor title")
            assertTrue(body.contains("test-video-1.mp4"), "Should show asset in library")
            assertTrue(body.contains("test-image-1.png"), "Should show image asset")
            assertTrue(body.contains("Asset Library"), "Should show library section")
            assertTrue(body.contains("Playlist"), "Should show playlist section")
        }

        @Test
        @DisplayName("Should redirect for non-existent channel")
        fun `should redirect for non-existent channel`() = testApplication {
            application { configureTestApp() }
            val client = createClient { followRedirects = false }

            val response = client.get("/admin/channels/${UUID.randomUUID()}/schedule") {
                withSession()
            }

            assertEquals(HttpStatusCode.Found, response.status)
        }

        @Test
        @DisplayName("Should require authentication")
        fun `should require auth`() = testApplication {
            application { configureTestApp() }
            val client = createClient { followRedirects = false }

            val response = client.get("/admin/channels/$testChannelId/schedule")

            assertEquals(HttpStatusCode.Found, response.status)
            assertTrue(response.headers["Location"]?.contains("login") == true)
        }
    }

    @Nested
    @DisplayName("Save Draft")
    inner class SaveDraftTests {

        @Test
        @DisplayName("Should save draft with items")
        fun `should save draft`() = testApplication {
            application { configureTestApp() }
            val client = createClient { followRedirects = false }

            val itemsJson = """[{"assetId":"$testAssetId1","orderIndex":0},{"assetId":"$testAssetId2","orderIndex":1}]"""

            val response = client.post("/admin/channels/$testChannelId/schedule/save") {
                withSession()
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("itemsJson=${itemsJson.encodeURLParameter()}")
            }

            assertEquals(HttpStatusCode.Found, response.status)
            val location = response.headers["Location"] ?: ""
            assertTrue(location.contains("success"), "Should redirect with success: $location")
        }

        @Test
        @DisplayName("Should save draft with timing")
        fun `should save draft with timing`() = testApplication {
            application { configureTestApp() }
            val client = createClient { followRedirects = false }

            val itemsJson = """[{"assetId":"$testAssetId1","orderIndex":0,"timeStart":"09:00","timeEnd":"17:00","daysOfWeek":31}]"""

            val response = client.post("/admin/channels/$testChannelId/schedule/save") {
                withSession()
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("itemsJson=${itemsJson.encodeURLParameter()}")
            }

            assertEquals(HttpStatusCode.Found, response.status)
            assertTrue(response.headers["Location"]?.contains("success") == true)
        }

        @Test
        @DisplayName("Should save empty schedule")
        fun `should save empty schedule`() = testApplication {
            application { configureTestApp() }
            val client = createClient { followRedirects = false }

            val response = client.post("/admin/channels/$testChannelId/schedule/save") {
                withSession()
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("itemsJson=[]")
            }

            assertEquals(HttpStatusCode.Found, response.status)
        }
    }

    @Nested
    @DisplayName("Publish Schedule")
    inner class PublishTests {

        @Test
        @DisplayName("Should publish schedule with items")
        fun `should publish with items`() = testApplication {
            application { configureTestApp() }
            val client = createClient { followRedirects = false }

            val itemsJson = """[{"assetId":"$testAssetId1","orderIndex":0},{"assetId":"$testAssetId2","orderIndex":1},{"assetId":"$testAssetId3","orderIndex":2}]"""

            val response = client.post("/admin/channels/$testChannelId/schedule/publish") {
                withSession()
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("itemsJson=${itemsJson.encodeURLParameter()}")
            }

            assertEquals(HttpStatusCode.Found, response.status)
            val location = response.headers["Location"] ?: ""
            assertTrue(location.contains("success"), "Should redirect with success: $location")
        }

        @Test
        @DisplayName("Should fail to publish empty schedule")
        fun `should fail to publish empty`() = testApplication {
            application { configureTestApp() }
            val client = createClient { followRedirects = false }

            val response = client.post("/admin/channels/$testChannelId/schedule/publish") {
                withSession()
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("itemsJson=[]")
            }

            assertEquals(HttpStatusCode.Found, response.status)
            val location = response.headers["Location"] ?: ""
            assertTrue(location.contains("error"), "Should redirect with error: $location")
        }
    }

    @Nested
    @DisplayName("Live Preview")
    inner class LivePreviewTests {

        @Test
        @DisplayName("Should display live preview page")
        fun `should display live preview`() = testApplication {
            application { configureTestApp() }

            val response = client.get("/admin/channels/$testChannelId/live") {
                withSession()
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("Live Preview"), "Should show live preview title")
        }

        @Test
        @DisplayName("Should show empty state when no schedule")
        fun `should show empty state`() = testApplication {
            application { configureTestApp() }

            // Use a channel with no schedule
            val emptyChannelId = UUID.randomUUID()
            transaction {
                ChannelEntity.new(emptyChannelId) {
                    this.tenant = TenantEntity[testTenantId]
                    name = "Empty Channel"
                    status = ChannelStatus.ACTIVE
                    createdAt = Clock.System.now()
                }
            }

            val response = client.get("/admin/channels/$emptyChannelId/live") {
                withSession()
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("No active schedule") || body.contains("Publish a schedule"), "Should show empty state")
        }

        @Test
        @DisplayName("Should redirect for non-existent channel")
        fun `should redirect for bad channel`() = testApplication {
            application { configureTestApp() }
            val client = createClient { followRedirects = false }

            val response = client.get("/admin/channels/${UUID.randomUUID()}/live") {
                withSession()
            }

            assertEquals(HttpStatusCode.Found, response.status)
        }
    }

    @Nested
    @DisplayName("Schedule Editor after Publish")
    inner class EditorAfterPublishTests {

        @Test
        @DisplayName("Should show published items in editor")
        fun `should show published items`() = testApplication {
            application { configureTestApp() }

            // First publish a schedule
            val client = createClient { followRedirects = false }
            val itemsJson = """[{"assetId":"$testAssetId1","orderIndex":0}]"""
            client.post("/admin/channels/$testChannelId/schedule/publish") {
                withSession()
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("itemsJson=${itemsJson.encodeURLParameter()}")
            }

            // Now open editor
            val followClient = this@testApplication.client
            val response = followClient.get("/admin/channels/$testChannelId/schedule") {
                withSession()
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("test-video-1.mp4"), "Should show published asset in playlist")
            assertTrue(body.contains("Published"), "Should show published badge")
        }
    }
}

private fun String.encodeURLParameter(): String =
    java.net.URLEncoder.encode(this, "UTF-8")
