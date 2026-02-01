package com.playoutedge.player.overlay

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Overlay state.
 */
@Serializable
data class OverlayState(
    val version: Long,
    val widgets: Map<String, Widget> = emptyMap()
)

/**
 * Widget types.
 */
@Serializable
sealed class Widget {
    abstract val id: String
    abstract val position: Position
    abstract val visible: Boolean

    @Serializable
    data class Ticker(
        override val id: String,
        override val position: Position,
        override val visible: Boolean = true,
        val text: String,
        val speed: Int = 100,
        val backgroundColor: String = "#000000",
        val textColor: String = "#FFFFFF"
    ) : Widget()

    @Serializable
    data class Text(
        override val id: String,
        override val position: Position,
        override val visible: Boolean = true,
        val text: String,
        val fontSize: Int = 24,
        val textColor: String = "#FFFFFF"
    ) : Widget()

    @Serializable
    data class Table(
        override val id: String,
        override val position: Position,
        override val visible: Boolean = true,
        val headers: List<String>,
        val rows: List<List<String>>
    ) : Widget()

    @Serializable
    data class QrCode(
        override val id: String,
        override val position: Position,
        override val visible: Boolean = true,
        val url: String,
        val size: Int = 200,
        val label: String? = null
    ) : Widget()

    @Serializable
    data class Kpi(
        override val id: String,
        override val position: Position,
        override val visible: Boolean = true,
        val title: String,
        val value: String,
        val unit: String? = null,
        val trend: String? = null // "up", "down", "stable"
    ) : Widget()
}

/**
 * Position on screen.
 */
@Serializable
data class Position(
    val x: Int = 0,
    val y: Int = 0,
    val width: Int? = null,
    val height: Int? = null,
    val anchor: Anchor = Anchor.TOP_LEFT
)

enum class Anchor {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER
}

/**
 * SSE event types.
 */
sealed class SseEvent {
    data class State(val state: OverlayState) : SseEvent()
    data class Patch(val operations: List<PatchOperation>) : SseEvent()
    data object Keepalive : SseEvent()
}

/**
 * Patch operation.
 */
@Serializable
data class PatchOperation(
    val op: String, // "upsert" or "remove"
    val widgetId: String,
    val widget: JsonObject? = null
)
