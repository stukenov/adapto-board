package com.playoutedge.server.views.overlay

import com.playoutedge.domain.enums.BindingStatus
import com.playoutedge.domain.enums.OverlaySourceType
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Overlay profile list item.
 */
data class OverlayProfileItem(
    val id: UUID,
    val name: String,
    val widgetTypes: List<String>,
    val bindingsCount: Int,
    val createdAt: Instant
)

/**
 * Overlay profile detail.
 */
data class OverlayProfileDetail(
    val id: UUID,
    val name: String,
    val definitionJson: String,
    val createdAt: Instant
)

/**
 * Overlay binding list item.
 */
data class OverlayBindingItem(
    val id: UUID,
    val profileId: UUID,
    val profileName: String,
    val channelId: UUID,
    val channelName: String,
    val sourceType: OverlaySourceType,
    val status: BindingStatus,
    val createdAt: Instant
)

/**
 * Overlay binding detail.
 */
data class OverlayBindingDetail(
    val id: UUID,
    val profileId: UUID,
    val profileName: String,
    val channelId: UUID,
    val channelName: String,
    val sourceType: OverlaySourceType,
    val sourceConfigJson: String,
    val status: BindingStatus,
    val webhookSecret: String?,
    val webhookUrl: String?,
    val createdAt: Instant
)

/**
 * Webhook log entry.
 */
data class WebhookLogItem(
    val id: UUID,
    val statusCode: Int,
    val latencyMs: Int,
    val payloadSize: Int,
    val error: String?,
    val createdAt: Instant
) {
    val isSuccess: Boolean get() = statusCode in 200..299
}

/**
 * Overlay state for manual editor.
 */
data class OverlayStateView(
    val channelId: UUID,
    val channelName: String,
    val stateJson: String,
    val version: Long,
    val updatedAt: Instant
)

/**
 * Predefined overlay templates.
 */
enum class OverlayTemplate(val displayName: String, val description: String) {
    TICKER("Ticker", "Scrolling text ticker at bottom of screen"),
    KPI_TILES("KPI Tiles", "Grid of key performance indicators"),
    QUEUE_TABLE("Queue Table", "Dynamic table showing queue data"),
    QR_CARD("QR Card", "Card with QR code and call to action"),
    POLL("Poll", "Interactive poll with voting"),
    REACTION("Reactions", "Emoji reaction overlay"),
    WEATHER("Weather", "Live weather widget"),
    CLOCK("Clock", "Current time display"),
    NEWS_TICKER("News Ticker", "Scrolling news feed from RSS/URL")
}
