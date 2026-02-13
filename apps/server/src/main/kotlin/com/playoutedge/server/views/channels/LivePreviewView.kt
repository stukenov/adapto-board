package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*
import java.util.UUID

/**
 * Live preview item for timeline.
 */
data class LivePreviewItem(
    val assetId: UUID,
    val assetName: String,
    val durationMs: Int?,
    val orderIndex: Int,
    val timeStart: String?,
    val timeEnd: String?
)

/**
 * Live preview page showing current playback state.
 */
fun HTML.livePreviewView(
    session: AdminClaims,
    channelId: UUID,
    channelName: String,
    scheduleVersion: Int?,
    items: List<LivePreviewItem>,
    currentIndex: Int,
    currentTimeFormatted: String
) {
    adminLayout(title = "Live Preview - $channelName", userName = session.displayName, currentPath = "/admin/channels") {
        pageHeader(
            title = "Live Preview",
            subtitle = channelName,
            backHref = "/admin/channels/$channelId",
            backLabel = "Back to Channel"
        ) {
            if (scheduleVersion != null) {
                span("badge badge-success mr-2") { +"v$scheduleVersion" }
            }
            span("badge badge-info") { +currentTimeFormatted }
        }

        if (items.isEmpty()) {
            div("card") {
                div("card-body") {
                    emptyState(
                        icon = "📺",
                        title = "No active schedule",
                        description = "Publish a schedule to see the live preview.",
                        actionHref = "/admin/channels/$channelId/schedule",
                        actionLabel = "Edit Schedule"
                    )
                }
            }
        } else {
            // Now Playing card
            if (currentIndex in items.indices) {
                val current = items[currentIndex]
                div("now-playing card mb-4") {
                    div("card-body") {
                        div("now-playing-header") {
                            span("now-playing-badge") { +"NOW PLAYING" }
                            span("now-playing-index") { +"${currentIndex + 1} / ${items.size}" }
                        }
                        h2("now-playing-title") { +current.assetName }
                        div("now-playing-meta") {
                            current.durationMs?.let { ms ->
                                val s = ms / 1000
                                span { +"Duration: ${s / 60}:${String.format("%02d", s % 60)}" }
                            }
                            if (current.timeStart != null || current.timeEnd != null) {
                                span { +"Time window: ${current.timeStart ?: "00:00"} - ${current.timeEnd ?: "23:59"}" }
                            }
                        }
                        // Next up
                        val nextIndex = (currentIndex + 1) % items.size
                        if (items.size > 1) {
                            div("next-up") {
                                span("next-up-label") { +"Next up:" }
                                span("next-up-name") { +items[nextIndex].assetName }
                            }
                        }
                    }
                }
            }

            // Timeline
            div("card mb-4") {
                div("card-header") {
                    h3 { +"Timeline" }
                }
                div("card-body") {
                    div("timeline") {
                        val totalDuration = items.mapNotNull { it.durationMs }.sum().coerceAtLeast(1)
                        items.forEachIndexed { index, item ->
                            val width = ((item.durationMs ?: (totalDuration / items.size)) * 100.0 / totalDuration)
                            div("timeline-segment${if (index == currentIndex) " active" else ""}") {
                                style = "width: ${width.coerceAtLeast(2.0)}%"
                                title = "${item.assetName} (${item.durationMs?.let { "${it/1000}s" } ?: "?"})"
                                span("timeline-segment-label") {
                                    +item.assetName.take(15)
                                }
                            }
                        }
                    }
                }
            }

            // Full playlist
            div("card") {
                div("card-header") {
                    h3 { +"Schedule Items" }
                }
                table("table") {
                    thead {
                        tr {
                            th { +"#" }
                            th { +"Asset" }
                            th { +"Duration" }
                            th { +"Time Window" }
                            th { +"Status" }
                        }
                    }
                    tbody {
                        items.forEachIndexed { index, item ->
                            tr(if (index == currentIndex) "table-row-active" else "") {
                                td { +"${index + 1}" }
                                td { +item.assetName }
                                td {
                                    item.durationMs?.let { ms ->
                                        val s = ms / 1000
                                        +"${s / 60}:${String.format("%02d", s % 60)}"
                                    } ?: span("text-muted") { +"—" }
                                }
                                td {
                                    if (item.timeStart != null || item.timeEnd != null) {
                                        +"${item.timeStart ?: "00:00"} - ${item.timeEnd ?: "23:59"}"
                                    } else {
                                        span("text-muted") { +"All day" }
                                    }
                                }
                                td {
                                    when (index) {
                                        currentIndex -> span("badge badge-success") { +"Playing" }
                                        (currentIndex + 1) % items.size -> span("badge badge-info") { +"Next" }
                                        else -> span("badge badge-gray") { +"Queued" }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Auto-refresh script
        script {
            unsafe {
                +"""setTimeout(function() { location.reload(); }, 30000);"""
            }
        }
    }
}
