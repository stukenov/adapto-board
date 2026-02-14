package com.playoutedge.server.views.schedule

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.*
import kotlinx.datetime.Instant
import kotlinx.html.*
import java.util.UUID

data class ScheduleVersionItem(
    val id: UUID,
    val state: String,
    val itemCount: Int,
    val createdAt: Instant
)

fun HTML.scheduleHistoryView(
    session: AdminClaims,
    channelId: UUID,
    channelName: String,
    versions: List<ScheduleVersionItem>
) {
    adminLayout(
        title = "Schedule History",
        userName = session.displayName,
        userRole = session.roleLabel,
        currentPath = "/admin/channels"
    ) {
        breadcrumb("Channels" to "/admin/channels", channelName to "/admin/channels/$channelId", "History" to null)
        pageHeader(title = "Schedule History", subtitle = "All schedule versions for $channelName",
            backHref = "/admin/channels/$channelId/schedule", backLabel = "Back to Schedule")

        if (versions.isEmpty()) {
            emptyState("\uD83D\uDCDC", "No History", "No schedule versions found.")
        } else {
            div("card") {
                div("card-body") {
                    div("table-responsive") {
                        table("table") {
                            thead {
                                tr {
                                    th { +"Version" }
                                    th { +"Status" }
                                    th { +"Items" }
                                    th { +"Created" }
                                    th { +"Actions" }
                                }
                            }
                            tbody {
                                versions.forEachIndexed { idx, v ->
                                    tr {
                                        td { +"v${versions.size - idx}" }
                                        td {
                                            span("badge badge-${if (v.state == "PUBLISHED") "success" else "secondary"}") {
                                                +v.state
                                            }
                                        }
                                        td { +"${v.itemCount}" }
                                        td { +v.createdAt.toString().take(16).replace("T", " ") }
                                        td {
                                            if (idx > 0) {
                                                a(href = "/admin/channels/$channelId/schedule/diff?from=${versions[idx].id}&to=${versions[idx-1].id}",
                                                    classes = "btn btn-secondary btn-sm") { +"Compare" }
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
    }
}
