package com.playoutedge.server.views.settings

import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * User list item.
 */
data class UserListItem(
    val id: UUID,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val status: UserStatus,
    val createdAt: Instant,
    val inviteExpiresAt: Instant? = null
)

/**
 * User detail.
 */
data class UserDetail(
    val id: UUID,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val status: UserStatus,
    val createdAt: Instant,
    val inviteExpiresAt: Instant? = null
)

/**
 * Tenant settings.
 */
data class TenantSettingsView(
    val tenantId: UUID,
    val name: String,
    val timezone: String,
    val offlineThresholdMinutes: Int,
    val maintenanceMode: Boolean,
    val maintenanceReason: String?,
    val releaseRing: String
)

/**
 * API key for display.
 */
data class ApiKeyItem(
    val id: UUID,
    val name: String,
    val prefix: String,
    val scopes: List<String>,
    val lastUsed: Instant?,
    val expiresAt: Instant?,
    val createdAt: Instant
)

/**
 * Storage quota info.
 */
data class StorageQuotaView(
    val usedBytes: Long,
    val limitBytes: Long,
    val usedPercent: Int
) {
    val usedFormatted: String get() = formatBytes(usedBytes)
    val limitFormatted: String get() = formatBytes(limitBytes)
}

/**
 * Device quota info.
 */
data class DeviceQuotaView(
    val enrolled: Int,
    val limit: Int,
    val usedPercent: Int
)

/**
 * Notification preferences view model.
 */
data class NotificationPrefsView(
    val emailOnDeviceOffline: Boolean = true,
    val emailOnAlert: Boolean = true,
    val emailOnSchedulePublish: Boolean = false,
    val emailDailyDigest: Boolean = false
)

/**
 * Active session view model.
 */
data class SessionItem(
    val id: UUID,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: Instant,
    val lastActiveAt: Instant,
    val isCurrent: Boolean = false
)

/**
 * Available roles with descriptions.
 */
val USER_ROLES = mapOf(
    UserRole.TENANT_ADMIN to "Full access to all tenant settings and users",
    UserRole.OPERATOR to "Manage content, channels, schedules. No user management"
)

/**
 * Release rings.
 */
val RELEASE_RINGS = listOf("STABLE", "CANARY")

/**
 * All IANA timezones.
 */
val TIMEZONES: List<Pair<String, String>> by lazy {
    java.time.ZoneId.getAvailableZoneIds()
        .sorted()
        .map { it to it.replace("_", " ") }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024
    if (kb < 1024) return "$kb KB"
    val mb = kb / 1024
    if (mb < 1024) return "$mb MB"
    val gb = mb / 1024
    return "$gb GB"
}
