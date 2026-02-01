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
