package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.AssetType
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
    val assetType: AssetType = AssetType.VIDEO,
    val assetStorageKey: String = "",
    val assetUrl: String = "",
    val durationMs: Int?,
    val orderIndex: Int,
    val timeStart: String?,
    val timeEnd: String?
)

/**
 * Live preview page with real media playback.
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
            span("badge badge-info") {
                id = "current-time"
                +currentTimeFormatted
            }
        }

        if (items.isEmpty()) {
            div("card") {
                div("card-body") {
                    emptyState(
                        icon = "monitor",
                        title = "No active schedule",
                        description = "Publish a schedule to see the live preview.",
                        actionHref = "/admin/channels/$channelId/schedule",
                        actionLabel = "Edit Schedule"
                    )
                }
            }
        } else {
            // Embed preview with overlay support
            div("card mb-4") {
                div("card-header") {
                    h3 { +"Live Player" }
                }
                div("card-body") {
                    div {
                        style = "position:relative;width:100%;padding-top:56.25%;background:#000;border-radius:8px;overflow:hidden;"
                        iframe {
                            src = "/embed/$channelId"
                            style = "position:absolute;top:0;left:0;width:100%;height:100%;border:none;"
                            attributes["allow"] = "autoplay"
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
                        id = "timeline"
                        val totalDuration = items.mapNotNull { it.durationMs }.sum().coerceAtLeast(1)
                        items.forEachIndexed { index, item ->
                            val width = ((item.durationMs ?: (totalDuration / items.size)) * 100.0 / totalDuration)
                            div("timeline-segment") {
                                attributes["data-index"] = index.toString()
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
                            th { +"Type" }
                            th { +"Duration" }
                            th { +"Time Window" }
                            th { +"Status" }
                        }
                    }
                    tbody {
                        id = "playlist-table"
                        items.forEachIndexed { index, item ->
                            tr {
                                attributes["data-index"] = index.toString()
                                td { +"${index + 1}" }
                                td { +item.assetName }
                                td { +item.assetType.name.lowercase() }
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
                                td("status-cell") { +"" }
                            }
                        }
                    }
                }
            }
        }

    }
}
