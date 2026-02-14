package com.playoutedge.server.views.schedule

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.*
import kotlinx.html.*
import java.util.UUID

data class ScheduleDiffItem(
    val assetId: UUID,
    val assetName: String,
    val orderIndex: Int,
    val timeStart: String?,
    val timeEnd: String?,
    val daysOfWeek: Int?
)

fun HTML.scheduleDiffView(
    session: AdminClaims,
    channelId: UUID,
    channelName: String,
    fromItems: List<ScheduleDiffItem>,
    toItems: List<ScheduleDiffItem>
) {
    adminLayout(
        title = "Schedule Diff",
        userName = session.displayName,
        userRole = session.roleLabel,
        currentPath = "/admin/channels"
    ) {
        breadcrumb(
            "Channels" to "/admin/channels",
            channelName to "/admin/channels/$channelId",
            "History" to "/admin/channels/$channelId/schedule/history",
            "Compare" to null
        )
        pageHeader(
            title = "Schedule Comparison",
            subtitle = "Comparing two versions for $channelName",
            backHref = "/admin/channels/$channelId/schedule/history",
            backLabel = "Back to History"
        )

        // Compute diff
        val fromAssetIds = fromItems.map { it.assetId }.toSet()
        val toAssetIds = toItems.map { it.assetId }.toSet()
        val addedIds = toAssetIds - fromAssetIds
        val removedIds = fromAssetIds - toAssetIds
        val commonIds = fromAssetIds.intersect(toAssetIds)

        // Changed items: same asset but different timing/order
        val changedIds = commonIds.filter { id ->
            val fromItem = fromItems.first { it.assetId == id }
            val toItem = toItems.first { it.assetId == id }
            fromItem.orderIndex != toItem.orderIndex ||
                fromItem.timeStart != toItem.timeStart ||
                fromItem.timeEnd != toItem.timeEnd ||
                fromItem.daysOfWeek != toItem.daysOfWeek
        }.toSet()

        // Summary
        div("card mb-4") {
            div("card-body") {
                div("stats-grid") {
                    div("stat-card") {
                        span("stat-label") { +"Added" }
                        span("stat-value") {
                            style = "color: var(--success)"
                            +"${addedIds.size}"
                        }
                    }
                    div("stat-card") {
                        span("stat-label") { +"Removed" }
                        span("stat-value") {
                            style = "color: var(--danger)"
                            +"${removedIds.size}"
                        }
                    }
                    div("stat-card") {
                        span("stat-label") { +"Changed" }
                        span("stat-value") {
                            style = "color: var(--warning)"
                            +"${changedIds.size}"
                        }
                    }
                    div("stat-card") {
                        span("stat-label") { +"Unchanged" }
                        span("stat-value") {
                            +"${commonIds.size - changedIds.size}"
                        }
                    }
                }
            }
        }

        // Side by side comparison
        div("schedule-diff-container") {
            style = "display:grid;grid-template-columns:1fr 1fr;gap:var(--gap);"

            // From version (old)
            div("card") {
                div("card-header") {
                    h3 { +"Previous Version" }
                    span("badge badge-gray") { +"${fromItems.size} items" }
                }
                div("card-body") {
                    if (fromItems.isEmpty()) {
                        p("text-muted") { +"Empty schedule" }
                    } else {
                        diffTable(fromItems, removedIds, changedIds, isOld = true)
                    }
                }
            }

            // To version (new)
            div("card") {
                div("card-header") {
                    h3 { +"New Version" }
                    span("badge badge-gray") { +"${toItems.size} items" }
                }
                div("card-body") {
                    if (toItems.isEmpty()) {
                        p("text-muted") { +"Empty schedule" }
                    } else {
                        diffTable(toItems, addedIds, changedIds, isOld = false)
                    }
                }
            }
        }
    }
}

private fun FlowContent.diffTable(
    items: List<ScheduleDiffItem>,
    highlightIds: Set<UUID>,
    changedIds: Set<UUID>,
    isOld: Boolean
) {
    table("table") {
        thead {
            tr {
                th { +"#" }
                th { +"Asset" }
                th { +"Time Window" }
            }
        }
        tbody {
            items.forEach { item ->
                val bgStyle = when {
                    item.assetId in highlightIds && isOld -> "background:rgba(220,53,69,0.1);"
                    item.assetId in highlightIds && !isOld -> "background:rgba(40,167,69,0.1);"
                    item.assetId in changedIds -> "background:rgba(255,193,7,0.1);"
                    else -> ""
                }
                tr {
                    style = bgStyle
                    td { +"${item.orderIndex + 1}" }
                    td {
                        +item.assetName
                        when {
                            item.assetId in highlightIds && isOld ->
                                span("badge badge-danger ml-1") { +"removed" }
                            item.assetId in highlightIds && !isOld ->
                                span("badge badge-success ml-1") { +"added" }
                            item.assetId in changedIds ->
                                span("badge badge-warning ml-1") { +"changed" }
                        }
                    }
                    td {
                        if (item.timeStart != null || item.timeEnd != null) {
                            +"${item.timeStart ?: "00:00"} - ${item.timeEnd ?: "23:59"}"
                        } else {
                            span("text-muted") { +"All day" }
                        }
                    }
                }
            }
        }
    }
}
