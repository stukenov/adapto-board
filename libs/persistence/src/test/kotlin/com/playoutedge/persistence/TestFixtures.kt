package com.playoutedge.persistence

import com.playoutedge.domain.enums.*
import com.playoutedge.domain.tenant.TenantId
import java.util.*

/**
 * Test data factories for repository tests.
 * Provides reusable test data including valid values and edge cases.
 */
object TestFixtures {

    // ==================== ID Generation ====================

    /**
     * Generates a unique ID with an optional prefix for debugging.
     */
    fun uniqueId(): UUID = UUID.randomUUID()

    /**
     * Generates a unique string suffix for names/emails.
     */
    fun uniqueSuffix(): String = UUID.randomUUID().toString().take(8)

    // ==================== Tenant Fixtures ====================

    object Tenants {
        val DEFAULT_NAME = "Test Tenant"
        val LONG_NAME = "A".repeat(255)  // Max length name
        val UNICODE_NAME = "テスト租户 🏢"
        val SPECIAL_CHARS_NAME = "Test's Tenant (Dev) & Prod"

        fun validName() = "Tenant ${uniqueSuffix()}"

        val allStatuses = TenantStatus.entries.toList()
        val allSupportTiers = SupportTier.entries.toList()
        val allReleaseRings = ReleaseRing.entries.toList()
    }

    // ==================== User Fixtures ====================

    object Users {
        val DEFAULT_EMAIL = "test@example.com"
        val DEFAULT_DISPLAY_NAME = "Test User"
        val DEFAULT_PASSWORD_HASH = "hashed_password_value"

        fun validEmail() = "user-${uniqueSuffix()}@example.com"
        fun uppercaseEmail() = "USER-${uniqueSuffix()}@EXAMPLE.COM"

        val LONG_EMAIL = "a".repeat(200) + "@example.com"  // Near max length
        val UNICODE_DISPLAY_NAME = "用户名 Test"
        val LONG_DISPLAY_NAME = "D".repeat(255)

        val allStatuses = UserStatus.entries.toList()
        val allRoles = UserRole.entries.toList()

        // Edge cases for passwords
        val EMPTY_PASSWORD_HASH = ""
        val LONG_PASSWORD_HASH = "h".repeat(255)
    }

    // ==================== Channel Fixtures ====================

    object Channels {
        val DEFAULT_NAME = "Test Channel"

        fun validName() = "Channel ${uniqueSuffix()}"

        val LONG_NAME = "C".repeat(255)
        val UNICODE_NAME = "频道 チャンネル"
        val SPECIAL_CHARS_NAME = "Channel #1 (Main) - Live & Test"

        val allStatuses = ChannelStatus.entries.toList()
    }

    // ==================== Device Fixtures ====================

    object Devices {
        val DEFAULT_DISPLAY_NAME = "Test Device"

        fun validDisplayName() = "Device ${uniqueSuffix()}"

        val LONG_DISPLAY_NAME = "D".repeat(255)
        val DEFAULT_SECRET_HASH = "device_secret_hash_value"
        val DEFAULT_APP_VERSION = "1.0.0"
        val DEFAULT_ANDROID_MODEL = "Samsung Galaxy Tab"
        val DEFAULT_ANDROID_VERSION = "14"

        // Edge case versions
        val MIN_VERSION = "0.0.1"
        val MAX_VERSION = "99.99.99"
        val SEMANTIC_VERSION = "2.0.0-beta.1+build.123"

        val allEnrollStatuses = DeviceEnrollStatus.entries.toList()
    }

    // ==================== Enroll Code Fixtures ====================

    object EnrollCodes {
        fun validCode() = uniqueSuffix().uppercase()

        val SHORT_CODE = "A1B2"
        val LONG_CODE = "ABCD1234"
        val NUMERIC_CODE = "12345678"

        val allStatuses = EnrollCodeStatus.entries.toList()
    }

    // ==================== Asset Fixtures ====================

    object Assets {
        val DEFAULT_NAME = "Test Asset"

        fun validName() = "Asset ${uniqueSuffix()}"

        val LONG_NAME = "A".repeat(255)
        val UNICODE_NAME = "资产 アセット"

        // Video fixtures
        val VIDEO_MIME_TYPE = "video/mp4"
        val VIDEO_DURATION_MS = 30000  // 30 seconds
        val VIDEO_WIDTH = 1920
        val VIDEO_HEIGHT = 1080
        val VIDEO_FILE_SIZE = 10_000_000L  // 10 MB

        // Image fixtures
        val IMAGE_MIME_TYPE = "image/png"
        val IMAGE_WIDTH = 1920
        val IMAGE_HEIGHT = 1080
        val IMAGE_FILE_SIZE = 500_000L  // 500 KB

        // Edge case durations
        val MIN_DURATION_MS = 1
        val MAX_DURATION_MS = 86_400_000  // 24 hours

        // Edge case file sizes
        val MIN_FILE_SIZE = 1L
        val MAX_FILE_SIZE = 10_737_418_240L  // 10 GB

        fun validStorageKey() = "tenants/${uniqueId()}/assets/${uniqueId()}"
        fun validChecksum() = "sha256:" + UUID.randomUUID().toString().replace("-", "")

        val allTypes = AssetType.entries.toList()
        val allStatuses = AssetStatus.entries.toList()
        val allProfiles = AssetProfile.entries.toList()
    }

    // ==================== Schedule Fixtures ====================

    object Schedules {
        val DEFAULT_VERSION = 1
        val DEFAULT_WEIGHT = 1

        // Edge case versions
        val MIN_VERSION = 1
        val MAX_VERSION = Int.MAX_VALUE

        // Edge case weights
        val MIN_WEIGHT = 1
        val MAX_WEIGHT = 100

        // Days of week bitmask (Mon-Sun: 1-127)
        val ALL_DAYS = 127  // 0b1111111
        val WEEKDAYS = 31   // 0b0011111 (Mon-Fri)
        val WEEKENDS = 96   // 0b1100000 (Sat-Sun)
        val MONDAY_ONLY = 1 // 0b0000001

        val allStates = ScheduleState.entries.toList()
    }

    // ==================== Overlay Fixtures ====================

    object Overlays {
        val DEFAULT_NAME = "Test Overlay"

        fun validName() = "Overlay ${uniqueSuffix()}"

        val LONG_NAME = "O".repeat(255)

        // Valid JSON definitions
        val SIMPLE_DEFINITION = """{"type": "simple", "layers": []}"""
        val COMPLEX_DEFINITION = """
            {
                "type": "composite",
                "layers": [
                    {"id": "clock", "type": "clock", "position": {"x": 10, "y": 10}},
                    {"id": "logo", "type": "image", "source": "logo.png", "position": {"x": 100, "y": 50}}
                ]
            }
        """.trimIndent()

        val EMPTY_DEFINITION = """{}"""
        val MINIMAL_ARRAY = """[]"""

        // Source configs
        val MANUAL_SOURCE_CONFIG = """{"manual": true}"""
        val REST_PULL_SOURCE_CONFIG = """{"url": "https://api.example.com/data", "interval": 60}"""
        val WEBHOOK_SOURCE_CONFIG = """{"path": "/webhook/overlay"}"""

        val allSourceTypes = OverlaySourceType.entries.toList()
        val allBindingStatuses = BindingStatus.entries.toList()
    }

    // ==================== Alert Fixtures ====================

    object Alerts {
        // Payload examples
        val DEVICE_OFFLINE_PAYLOAD = """{"deviceId": "${uniqueId()}", "lastSeen": "2024-01-01T00:00:00Z"}"""
        val PLAYBACK_ERROR_PAYLOAD = """{"deviceId": "${uniqueId()}", "error": "CODEC_NOT_SUPPORTED", "assetId": "${uniqueId()}"}"""
        val STORAGE_QUOTA_PAYLOAD = """{"tenantId": "${uniqueId()}", "usedBytes": 9500000000, "quotaBytes": 10000000000}"""

        fun deviceOfflinePayload(deviceId: UUID) = """{"deviceId": "$deviceId", "lastSeen": "2024-01-01T00:00:00Z"}"""
        fun playbackErrorPayload(deviceId: UUID, assetId: UUID) = """{"deviceId": "$deviceId", "error": "CODEC_NOT_SUPPORTED", "assetId": "$assetId"}"""

        val allTypes = AlertType.entries.toList()
        val allStatuses = AlertStatus.entries.toList()
    }

    // ==================== Audit Fixtures ====================

    object Audit {
        // Common actions
        val CREATE_ACTION = "CREATE"
        val UPDATE_ACTION = "UPDATE"
        val DELETE_ACTION = "DELETE"
        val ARCHIVE_ACTION = "ARCHIVE"
        val LOGIN_ACTION = "LOGIN"
        val LOGOUT_ACTION = "LOGOUT"

        // Entity types
        val CHANNEL_ENTITY = "channel"
        val DEVICE_ENTITY = "device"
        val ASSET_ENTITY = "asset"
        val USER_ENTITY = "user"
        val SCHEDULE_ENTITY = "schedule"

        // Diff examples
        val SIMPLE_DIFF = """{"name": {"old": "Old Name", "new": "New Name"}}"""
        val STATUS_DIFF = """{"status": {"old": "ACTIVE", "new": "PAUSED"}}"""
        val EMPTY_DIFF = """{}"""

        val allActorTypes = ActorType.entries.toList()
    }

    // ==================== Asrun Fixtures ====================

    object Asrun {
        // Details examples
        val PLAY_START_DETAILS = """{"position": 0, "scheduled": true}"""
        val PLAY_END_DETAILS = """{"position": 30000, "completed": true}"""
        val SKIP_DETAILS = """{"reason": "USER_SKIP", "position": 15000}"""
        val ERROR_DETAILS = """{"error": "DECODE_ERROR", "position": 5000}"""
        val HEARTBEAT_DETAILS = """{"position": 10000, "bufferHealth": "good"}"""

        val allEventTypes = AsrunEventType.entries.toList()
    }

    // ==================== Job Fixtures ====================

    object Jobs {
        // Job types
        val ASSET_TRANSCODE_TYPE = "asset_transcode"
        val WEBHOOK_DELIVERY_TYPE = "webhook_delivery"
        val CLEANUP_TYPE = "cleanup"
        val NOTIFICATION_TYPE = "notification"

        // Payload examples
        val TRANSCODE_PAYLOAD = """{"assetId": "${uniqueId()}", "targetProfile": "NORMALIZED"}"""
        val WEBHOOK_PAYLOAD = """{"bindingId": "${uniqueId()}", "data": {"key": "value"}}"""
        val CLEANUP_PAYLOAD = """{"olderThan": "2024-01-01T00:00:00Z"}"""

        val DEFAULT_MAX_ATTEMPTS = 3
        val MIN_MAX_ATTEMPTS = 1
        val MAX_MAX_ATTEMPTS = 10

        val allStatuses = JobStatus.entries.toList()
    }

    // ==================== Webhook Log Fixtures ====================

    object WebhookLogs {
        // Status codes
        val SUCCESS_STATUS = 200
        val CREATED_STATUS = 201
        val BAD_REQUEST_STATUS = 400
        val UNAUTHORIZED_STATUS = 401
        val NOT_FOUND_STATUS = 404
        val SERVER_ERROR_STATUS = 500
        val TIMEOUT_STATUS = 504

        // Latencies (ms)
        val FAST_LATENCY = 50
        val NORMAL_LATENCY = 200
        val SLOW_LATENCY = 2000
        val TIMEOUT_LATENCY = 30000

        // Payload sizes
        val SMALL_PAYLOAD = 256
        val MEDIUM_PAYLOAD = 4096
        val LARGE_PAYLOAD = 65536

        // Error messages
        val TIMEOUT_ERROR = "Request timeout after 30000ms"
        val CONNECTION_ERROR = "Connection refused"
        val DNS_ERROR = "Could not resolve host"
    }

    // ==================== Contact Fixtures ====================

    object Contacts {
        val DEFAULT_NAME = "Test Contact"
        val DEFAULT_EMAIL = "contact@example.com"
        val DEFAULT_PHONE = "+1-555-0100"

        fun validName() = "Contact ${uniqueSuffix()}"
        fun validEmail() = "contact-${uniqueSuffix()}@example.com"

        // Phone formats
        val US_PHONE = "+1-555-0123"
        val UK_PHONE = "+44-20-7946-0958"
        val INTERNATIONAL_PHONE = "+81-3-1234-5678"

        val allTypes = ContactType.entries.toList()
    }
}
