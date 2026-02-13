package com.playoutedge.persistence

import com.playoutedge.domain.enums.*
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.*
import com.playoutedge.persistence.tables.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import java.util.*

/**
 * Extended test base for repository tests with helper methods for creating test entities.
 * Provides transaction rollback support and factory methods for common test entities.
 */
abstract class RepositoryTestBase : DatabaseTestBase() {

    /**
     * Cleans up test data before each test.
     * Override this method to add custom cleanup logic if needed.
     */
    @BeforeEach
    open fun cleanupTestData() {
        // Note: We don't clean up here by default to allow test-specific setup.
        // Each test should create its own isolated data using unique IDs.
    }

    // ==================== Tenant Helpers ====================

    /**
     * Creates a test tenant with default values.
     */
    protected fun createTestTenant(
        id: UUID = UUID.randomUUID(),
        name: String = "Test Tenant ${UUID.randomUUID().toString().take(8)}",
        status: TenantStatus = TenantStatus.ACTIVE,
        supportTier: SupportTier = SupportTier.BASIC,
        releaseRing: ReleaseRing = ReleaseRing.STABLE,
        maintenanceMode: Boolean = false,
        maintenanceReason: String? = null
    ): TenantEntity = transaction {
        TenantEntity.new(id) {
            this.name = name
            this.status = status
            this.supportTier = supportTier
            this.releaseRing = releaseRing
            this.maintenanceMode = maintenanceMode
            this.maintenanceReason = maintenanceReason
            this.createdAt = Clock.System.now()
        }
    }

    /**
     * Creates a TenantId wrapper for the given tenant entity.
     */
    protected fun TenantEntity.toTenantId(): TenantId = TenantId(this.id.value)

    // ==================== User Helpers ====================

    /**
     * Creates a test user with default values.
     */
    protected fun createTestUser(
        tenant: TenantEntity,
        id: UUID = UUID.randomUUID(),
        email: String = "user-${UUID.randomUUID().toString().take(8)}@test.com",
        displayName: String = "Test User",
        passwordHash: String = "hashed_password",
        status: UserStatus = UserStatus.ACTIVE,
        role: UserRole = UserRole.OPERATOR
    ): UserEntity = transaction {
        val user = UserEntity.new(id) {
            this.tenant = tenant
            this.email = email.lowercase()
            this.displayName = displayName
            this.status = status
            this.passwordHash = passwordHash
            this.createdAt = Clock.System.now()
        }
        // Add role
        UserRoles.insert {
            it[userId] = user.id
            it[UserRoles.role] = role
        }
        user
    }

    // ==================== Channel Helpers ====================

    /**
     * Creates a test channel with default values.
     */
    protected fun createTestChannel(
        tenant: TenantEntity,
        id: UUID = UUID.randomUUID(),
        name: String = "Channel ${UUID.randomUUID().toString().take(8)}",
        status: ChannelStatus = ChannelStatus.ACTIVE,
        defaultOverlayProfileId: UUID? = null
    ): ChannelEntity = transaction {
        ChannelEntity.new(id) {
            this.tenant = tenant
            this.name = name
            this.status = status
            this.defaultOverlayProfileId = defaultOverlayProfileId
            this.createdAt = Clock.System.now()
        }
    }

    // ==================== Device Helpers ====================

    /**
     * Creates a test device with default values.
     */
    protected fun createTestDevice(
        tenant: TenantEntity,
        id: UUID = UUID.randomUUID(),
        displayName: String = "Device ${UUID.randomUUID().toString().take(8)}",
        enrollStatus: DeviceEnrollStatus = DeviceEnrollStatus.ENROLLED,
        deviceSecretHash: String? = "secret_hash",
        assignedChannelId: UUID? = null,
        appVersion: String? = "1.0.0",
        androidModel: String? = "Test Model",
        androidVersion: String? = "14"
    ): DeviceEntity = transaction {
        DeviceEntity.new(id) {
            this.tenant = tenant
            this.displayName = displayName
            this.enrollStatus = enrollStatus
            this.deviceSecretHash = deviceSecretHash
            this.assignedChannelId = assignedChannelId
            this.appVersion = appVersion
            this.androidModel = androidModel
            this.androidVersion = androidVersion
            this.createdAt = Clock.System.now()
        }
    }

    /**
     * Creates a test enroll code.
     */
    protected fun createTestEnrollCode(
        tenant: TenantEntity,
        channel: ChannelEntity? = null,
        createdBy: UserEntity? = null,
        id: UUID = UUID.randomUUID(),
        code: String = UUID.randomUUID().toString().take(8).uppercase(),
        status: EnrollCodeStatus = EnrollCodeStatus.PENDING
    ): EnrollCodeEntity = transaction {
        EnrollCodeEntity.new(id) {
            this.tenant = tenant
            this.code = code
            this.status = status
            this.channel = channel
            this.expiresAt = Clock.System.now().plus(kotlin.time.Duration.parse("24h"))
            this.createdBy = createdBy
            this.createdAt = Clock.System.now()
        }
    }

    // ==================== Asset Helpers ====================

    /**
     * Creates a test asset with default values.
     */
    protected fun createTestAsset(
        tenant: TenantEntity,
        createdBy: UserEntity? = null,
        id: UUID = UUID.randomUUID(),
        name: String = "Asset ${UUID.randomUUID().toString().take(8)}",
        type: AssetType = AssetType.VIDEO,
        status: AssetStatus = AssetStatus.READY,
        mimeType: String = "video/mp4",
        storageKey: String = "assets/${UUID.randomUUID()}",
        durationMs: Int? = 30000,
        width: Int? = 1920,
        height: Int? = 1080,
        fileSizeBytes: Long? = 1024000L,
        checksumSha256: String? = "abc123checksum"
    ): AssetEntity = transaction {
        AssetEntity.new(id) {
            this.tenant = tenant
            this.type = type
            this.name = name
            this.status = status
            this.mimeType = mimeType
            this.storageKey = storageKey
            this.durationMs = durationMs
            this.width = width
            this.height = height
            this.fileSizeBytes = fileSizeBytes
            this.checksumSha256 = checksumSha256
            this.createdBy = createdBy
            this.createdAt = Clock.System.now()
        }
    }

    /**
     * Creates a test asset version.
     */
    protected fun createTestAssetVersion(
        asset: AssetEntity,
        id: UUID = UUID.randomUUID(),
        profile: AssetProfile = AssetProfile.ORIGINAL,
        storageKey: String = "assets/versions/${UUID.randomUUID()}",
        codec: String? = "h264",
        bitrate: Int? = 5000000,
        container: String? = "mp4"
    ): AssetVersionEntity = transaction {
        AssetVersionEntity.new(id) {
            this.asset = asset
            this.profile = profile
            this.storageKey = storageKey
            this.codec = codec
            this.bitrate = bitrate
            this.container = container
            this.createdAt = Clock.System.now()
        }
    }

    // ==================== Schedule Helpers ====================

    /**
     * Creates a test schedule version.
     */
    protected fun createTestScheduleVersion(
        tenant: TenantEntity,
        channel: ChannelEntity,
        createdBy: UserEntity? = null,
        id: UUID = UUID.randomUUID(),
        version: Int = 1,
        state: ScheduleState = ScheduleState.DRAFT
    ): ScheduleVersionEntity = transaction {
        ScheduleVersionEntity.new(id) {
            this.tenant = tenant
            this.channel = channel
            this.version = version
            this.state = state
            this.createdBy = createdBy
            this.createdAt = Clock.System.now()
        }
    }

    /**
     * Creates a test schedule item.
     */
    protected fun createTestScheduleItem(
        tenant: TenantEntity,
        scheduleVersion: ScheduleVersionEntity,
        asset: AssetEntity,
        id: UUID = UUID.randomUUID(),
        orderIndex: Int = 0,
        weight: Int = 1
    ): ScheduleItemEntity = transaction {
        ScheduleItemEntity.new(id) {
            this.tenant = tenant
            this.scheduleVersion = scheduleVersion
            this.asset = asset
            this.orderIndex = orderIndex
            this.weight = weight
        }
    }

    // ==================== Overlay Helpers ====================

    /**
     * Creates a test overlay profile.
     */
    protected fun createTestOverlayProfile(
        tenant: TenantEntity,
        id: UUID = UUID.randomUUID(),
        name: String = "Overlay ${UUID.randomUUID().toString().take(8)}",
        definitionJson: JsonObject = Json.decodeFromString("""{"type": "test", "layers": []}""")
    ): OverlayProfileEntity = transaction {
        OverlayProfileEntity.new(id) {
            this.tenant = tenant
            this.name = name
            this.definitionJson = definitionJson
            this.createdAt = Clock.System.now()
        }
    }

    /**
     * Creates a test overlay binding.
     */
    protected fun createTestOverlayBinding(
        tenant: TenantEntity,
        channel: ChannelEntity,
        overlayProfile: OverlayProfileEntity,
        id: UUID = UUID.randomUUID(),
        sourceType: OverlaySourceType = OverlaySourceType.MANUAL,
        sourceConfigJson: JsonObject = Json.decodeFromString("""{"key": "value"}"""),
        status: BindingStatus = BindingStatus.ACTIVE,
        webhookSecret: String? = null
    ): OverlayBindingEntity = transaction {
        OverlayBindingEntity.new(id) {
            this.tenant = tenant
            this.channel = channel
            this.overlayProfile = overlayProfile
            this.sourceType = sourceType
            this.sourceConfigJson = sourceConfigJson
            this.status = status
            this.webhookSecret = webhookSecret
            this.createdAt = Clock.System.now()
        }
    }

    // ==================== Alert Helpers ====================

    /**
     * Creates a test alert.
     */
    protected fun createTestAlert(
        tenant: TenantEntity,
        id: UUID = UUID.randomUUID(),
        type: AlertType = AlertType.DEVICE_OFFLINE,
        status: AlertStatus = AlertStatus.OPEN,
        payloadJson: JsonObject = Json.decodeFromString("""{"deviceId": "${UUID.randomUUID()}"}""")
    ): AlertEntity = transaction {
        val now = Clock.System.now()
        AlertEntity.new(id) {
            this.tenant = tenant
            this.type = type
            this.status = status
            this.payloadJson = payloadJson
            this.firstSeenAt = now
            this.lastSeenAt = now
        }
    }

    // ==================== Audit Helpers ====================

    /**
     * Creates a test audit log entry.
     */
    protected fun createTestAuditLog(
        tenant: TenantEntity? = null,
        actorUser: UserEntity? = null,
        id: UUID = UUID.randomUUID(),
        actorType: ActorType = ActorType.USER,
        action: String = "CREATE",
        entityType: String = "test_entity",
        entityId: UUID = UUID.randomUUID(),
        diffJson: JsonObject? = null
    ): AuditLogEntity = transaction {
        AuditLogEntity.new(id) {
            this.tenant = tenant
            this.actorUser = actorUser
            this.actorType = actorType
            this.action = action
            this.entityType = entityType
            this.entityId = entityId
            this.diffJson = diffJson
            this.createdAt = Clock.System.now()
        }
    }

    // ==================== Asrun Helpers ====================

    /**
     * Creates a test as-run event.
     */
    protected fun createTestAsrunEvent(
        tenant: TenantEntity,
        device: DeviceEntity,
        channel: ChannelEntity? = null,
        scheduleVersion: ScheduleVersionEntity? = null,
        asset: AssetEntity? = null,
        id: UUID = UUID.randomUUID(),
        eventType: AsrunEventType = AsrunEventType.PLAY_START,
        detailsJson: JsonObject? = null
    ): AsrunEventEntity = transaction {
        val now = Clock.System.now()
        AsrunEventEntity.new(id) {
            this.tenant = tenant
            this.device = device
            this.channel = channel
            this.scheduleVersion = scheduleVersion
            this.asset = asset
            this.eventType = eventType
            this.at = now
            this.detailsJson = detailsJson
            this.createdAt = now
        }
    }

    // ==================== Webhook Log Helpers ====================

    /**
     * Creates a test webhook log entry.
     */
    protected fun createTestWebhookLog(
        binding: OverlayBindingEntity,
        id: UUID = UUID.randomUUID(),
        statusCode: Int = 200,
        latencyMs: Int = 100,
        payloadSize: Int = 256,
        error: String? = null
    ): WebhookLogEntity = transaction {
        WebhookLogEntity.new(id) {
            this.binding = binding
            this.statusCode = statusCode
            this.latencyMs = latencyMs
            this.payloadSize = payloadSize
            this.error = error
            this.createdAt = Clock.System.now()
        }
    }

    // ==================== Cleanup Utilities ====================

    /**
     * Deletes all data from specified tables in the correct order to respect FK constraints.
     * Use sparingly as it affects all data in the test database.
     */
    protected fun cleanAllTables() = transaction {
        // Delete in reverse order of dependencies
        WebhookLogs.deleteAll()
        AsrunEvents.deleteAll()
        AuditLog.deleteAll()
        Alerts.deleteAll()
        Jobs.deleteAll()
        OverlayStates.deleteAll()
        OverlayBindings.deleteAll()
        OverlayProfiles.deleteAll()
        ScheduleItems.deleteAll()
        ScheduleVersions.deleteAll()
        DeviceGroupMembers.deleteAll()
        DeviceGroups.deleteAll()
        DeviceActions.deleteAll()
        EnrollCodes.deleteAll()
        Devices.deleteAll()
        AssetVersions.deleteAll()
        Assets.deleteAll()
        Channels.deleteAll()
        UserRoles.deleteAll()
        Users.deleteAll()
        TenantContacts.deleteAll()
        Tenants.deleteAll()
    }
}
