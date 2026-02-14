package com.playoutedge.server.views.schedule

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.*
import kotlinx.html.*
import java.util.UUID

data class SchedulePreviewItem(
    val assetName: String,
    val startTime: String?,
    val endTime: String?,
    val daysOfWeek: String?,
    val duration: Int?
)

fun HTML.schedulePreviewView(
    session: AdminClaims,
    channelId: UUID,
    channelName: String,
    items: List<SchedulePreviewItem>
) {
    adminLayout(
        title = "Schedule Preview",
        userName = session.displayName,
        userRole = session.roleLabel,
        currentPath = "/admin/channels"
    ) {
        breadcrumb("Channels" to "/admin/channels", channelName to "/admin/channels/$channelId", "Schedule Preview" to null)
        pageHeader(title = "Schedule Preview", subtitle = "24-hour timeline for $channelName",
            backHref = "/admin/channels/$channelId/schedule", backLabel = "Back to Schedule")

        if (items.isEmpty()) {
            emptyState("📋", "No Schedule Items", "This channel has no scheduled content yet.",
                actionHref = "/admin/channels/$channelId/schedule", actionLabel = "Edit Schedule")
        } else {
            div("card") {
                div("card-body") {
                    div("table-responsive") {
                        table("table") {
                            thead {
                                tr {
                                    th { +"#" }
                                    th { +"Asset" }
                                    th { +"Start" }
                                    th { +"End" }
                                    th { +"Days" }
                                    th { +"Duration" }
                                }
                            }
                            tbody {
                                items.forEachIndexed { idx, item ->
                                    tr {
                                        td { +"${idx + 1}" }
                                        td { +item.assetName }
                                        td { +(item.startTime ?: "\u2014") }
                                        td { +(item.endTime ?: "\u2014") }
                                        td { +(item.daysOfWeek ?: "All") }
                                        td { +(item.duration?.let { "${it / 1000}s" } ?: "\u2014") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
