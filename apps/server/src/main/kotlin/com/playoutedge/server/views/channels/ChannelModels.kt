package com.playoutedge.server.views.channels

import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.domain.enums.DeviceEnrollStatus
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Channel item for list view.
 */
data class ChannelListItem(
    val id: UUID,
    val name: String,
    val status: ChannelStatus,
    val deviceCount: Int,
    val lastPublish: Instant?
)

/**
 * Channel detail for detail view.
 */
data class ChannelDetail(
    val id: UUID,
    val name: String,
    val status: ChannelStatus,
    val createdAt: Instant
)

/**
 * Schedule item for schedule view.
 */
data class ScheduleItemView(
    val id: UUID,
    val assetId: UUID,
    val assetName: String,
    val sortOrder: Int,
    val timeStart: String?,
    val timeEnd: String?
)

/**
 * Device item for device list in channel detail.
 */
data class DeviceListItem(
    val id: UUID,
    val name: String,
    val status: DeviceEnrollStatus,
    val lastSeen: Instant?,
    val isOnline: Boolean
)

/**
 * Filters for channel list.
 */
data class ChannelFilters(
    val status: ChannelStatus? = null,
    val search: String? = null,
    val groupId: UUID? = null
)
