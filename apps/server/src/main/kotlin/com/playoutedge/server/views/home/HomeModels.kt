package com.playoutedge.server.views.home

import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Fleet health summary.
 */
data class FleetHealth(
    val onlineCount: Int,
    val totalCount: Int,
    val offlineDevices: List<OfflineDevice>
) {
    val onlineRate: Int
        get() = if (totalCount > 0) (onlineCount * 100 / totalCount) else 0
}

/**
 * Device that is currently offline.
 */
data class OfflineDevice(
    val id: UUID,
    val name: String,
    val lastSeen: Instant?
)

/**
 * Publish health summary.
 */
data class PublishHealth(
    val lastPublish: Instant?,
    val pendingCount: Int
)

/**
 * Alert summary for dashboard.
 */
data class AlertSummary(
    val id: UUID,
    val type: String,
    val message: String,
    val createdAt: Instant
)

/**
 * Dashboard content stats.
 */
data class ContentStats(
    val channelCount: Int,
    val assetCount: Int,
    val storageUsedBytes: Long
) {
    val storageDisplay: String
        get() {
            val gb = storageUsedBytes / (1024.0 * 1024.0 * 1024.0)
            val mb = storageUsedBytes / (1024.0 * 1024.0)
            return if (gb >= 1.0) "%.1f GB".format(gb) else "%.0f MB".format(mb)
        }
}

/**
 * Recent activity item for dashboard.
 */
data class RecentActivity(
    val action: String,
    val entityType: String,
    val actorName: String?,
    val createdAt: Instant
)
