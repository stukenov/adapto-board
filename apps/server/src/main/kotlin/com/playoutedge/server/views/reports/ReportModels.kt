package com.playoutedge.server.views.reports

import com.playoutedge.domain.enums.AsrunEventType
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Audit log entry for display.
 */
data class AuditLogItem(
    val id: UUID,
    val actorName: String?,
    val actorType: String,
    val action: String,
    val entityType: String,
    val entityId: UUID,
    val hasDiff: Boolean,
    val createdAt: Instant
)

/**
 * Audit log filters.
 */
data class AuditLogFilters(
    val entityType: String? = null,
    val actorId: UUID? = null,
    val action: String? = null,
    val fromDate: String? = null,
    val toDate: String? = null
)

/**
 * As-run event for display.
 */
data class AsrunEventItem(
    val id: UUID,
    val deviceId: UUID,
    val deviceName: String,
    val channelId: UUID?,
    val channelName: String?,
    val assetId: UUID?,
    val assetName: String?,
    val eventType: AsrunEventType,
    val at: Instant
)

/**
 * As-run filters.
 */
data class AsrunFiltersView(
    val deviceId: UUID? = null,
    val channelId: UUID? = null,
    val eventType: AsrunEventType? = null,
    val fromDate: String? = null,
    val toDate: String? = null
)

/**
 * As-run summary stats.
 */
data class AsrunSummaryView(
    val totalEvents: Long,
    val uniqueAssets: Int,
    val totalPlayTime: String,
    val byAsset: List<AssetPlaybackView>
)

/**
 * Asset playback summary.
 */
data class AssetPlaybackView(
    val assetId: UUID,
    val assetName: String,
    val playCount: Long
)

/**
 * Device option for filters.
 */
data class DeviceFilterOption(
    val id: UUID,
    val name: String
)

/**
 * Channel option for filters.
 */
data class ChannelFilterOption(
    val id: UUID,
    val name: String
)

/**
 * Entity types for audit log filter.
 */
val ENTITY_TYPES = listOf(
    "asset",
    "channel",
    "device",
    "schedule",
    "overlay",
    "user"
)

/**
 * Action types for audit log filter.
 */
val ACTION_TYPES = listOf(
    "create",
    "update",
    "delete",
    "archive",
    "publish",
    "enroll",
    "reject"
)
