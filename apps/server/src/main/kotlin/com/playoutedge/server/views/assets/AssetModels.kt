package com.playoutedge.server.views.assets

import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.AssetType
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Asset item for list view.
 */
data class AssetViewItem(
    val id: UUID,
    val name: String,
    val type: AssetType,
    val status: AssetStatus,
    val fileSize: Long,
    val duration: Int?,
    val createdAt: Instant,
    val thumbnailStorageKey: String? = null,
    val approvalStatus: String = "APPROVED",
    val width: Int? = null,
    val height: Int? = null
) {
    val fileSizeFormatted: String
        get() = formatFileSize(fileSize)

    val durationFormatted: String?
        get() = duration?.let { formatDuration(it) }
}

/**
 * Quota info for display.
 */
data class QuotaInfo(
    val usedBytes: Long,
    val limitBytes: Long
) {
    val usedPercent: Int
        get() = if (limitBytes > 0) ((usedBytes * 100) / limitBytes).toInt() else 0

    val usedFormatted: String
        get() = formatFileSize(usedBytes)

    val limitFormatted: String
        get() = formatFileSize(limitBytes)
}

/**
 * Asset detail model.
 */
data class AssetDetailModel(
    val id: UUID,
    val name: String,
    val type: AssetType,
    val status: AssetStatus,
    val mimeType: String?,
    val fileSize: Long,
    val duration: Int?,
    val width: Int?,
    val height: Int?,
    val rejectionReason: String?,
    val createdAt: Instant
) {
    val fileSizeFormatted: String
        get() = formatFileSize(fileSize)

    val durationFormatted: String?
        get() = duration?.let { formatDuration(it) }

    val resolution: String?
        get() = if (width != null && height != null) "${width}x${height}" else null
}

/**
 * Filters for asset list.
 */
data class AssetFilters(
    val type: AssetType? = null,
    val status: AssetStatus? = null,
    val search: String? = null
)

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1_024 -> String.format("%.1f KB", bytes / 1_024.0)
        else -> "$bytes B"
    }
}

private fun formatDuration(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
