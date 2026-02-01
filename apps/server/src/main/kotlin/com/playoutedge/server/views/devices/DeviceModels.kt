package com.playoutedge.server.views.devices

import com.playoutedge.domain.enums.DeviceEnrollStatus
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Device item for list view.
 */
data class DeviceViewItem(
    val id: UUID,
    val name: String,
    val status: DeviceEnrollStatus,
    val isOnline: Boolean,
    val channelName: String?,
    val appVersion: String?,
    val lastSeen: Instant?
)

/**
 * Device stats for dashboard.
 */
data class DeviceStats(
    val total: Int,
    val online: Int,
    val offline: Int,
    val pending: Int
)

/**
 * Device detail model.
 */
data class DeviceDetailModel(
    val id: UUID,
    val name: String,
    val status: DeviceEnrollStatus,
    val isOnline: Boolean,
    val channelId: UUID?,
    val channelName: String?,
    val appVersion: String?,
    val androidModel: String?,
    val androidVersion: String?,
    val lastSeen: Instant?,
    val createdAt: Instant
)

/**
 * Filters for device list.
 */
data class DeviceFilters(
    val status: String? = null,
    val channelId: UUID? = null,
    val search: String? = null
)

/**
 * Enroll code item.
 */
data class EnrollCodeItem(
    val id: UUID,
    val code: String,
    val channelName: String?,
    val expiresAt: Instant,
    val isExpired: Boolean
)

/**
 * Channel option for dropdown.
 */
data class ChannelOption(
    val id: UUID,
    val name: String
)
