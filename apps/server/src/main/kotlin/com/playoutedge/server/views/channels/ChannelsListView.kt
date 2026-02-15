package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.server.views.PaginationInfo
import com.playoutedge.server.views.*
import com.playoutedge.server.views.components.hxSearchInput
import com.playoutedge.server.views.components.hxPagination
import kotlinx.html.*

/**
 * Channels list view with improved UX.
 */
data class ChannelGroup(
    val id: java.util.UUID,
    val name: String
)

fun HTML.channelsListView(
    session: AdminClaims,
    channels: List<ChannelListItem>,
    filters: ChannelFilters,
    pagination: PaginationInfo? = null,
    groups: List<ChannelGroup> = emptyList()
) {
    adminLayout(title = "Channels", userName = session.displayName, currentPath = "/admin/channels") {
        // Page header
        pageHeader(
            title = "Channels",
            subtitle = "Manage your content playlists"
        ) {
            a(href = "/admin/channels/new", classes = "btn btn-primary") {
                +"+ Create Channel"
            }
        }

        // Tabs
        pageTabs(listOf(
            TabDef("All", "/admin/channels"),
            TabDef("Active", "/admin/channels?status=ACTIVE"),
            TabDef("Paused", "/admin/channels?status=PAUSED")
        ), "/admin/channels${if (filters.status != null) "?status=${filters.status.name}" else ""}")

        // Filters with HTMX
        div("card mb-4") {
            div("filter-row") {
                // Live search
                hxSearchInput(
                    url = "/admin/channels/table",
                    target = "#channels-table",
                    name = "search",
                    placeholder = "Search channels...",
                    currentValue = filters.search
                )
                // Status filter with HTMX
                div("form-group") {
                    label {
                        htmlFor = "filter-status"
                        +"Status"
                    }
                    select("form-control") {
                        id = "filter-status"
                        name = "status"
                        attributes["hx-get"] = "/admin/channels/table"
                        attributes["hx-target"] = "#channels-table"
                        attributes["hx-swap"] = "outerHTML"
                        attributes["hx-trigger"] = "change"
                        attributes["hx-include"] = "[name='search']"
                        option {
                            value = ""
                            if (filters.status == null) selected = true
                            +"All Statuses"
                        }
                        ChannelStatus.entries.forEach { status ->
                            option {
                                value = status.name
                                if (filters.status == status) selected = true
                                +status.name.lowercase().replaceFirstChar { it.uppercase() }
                            }
                        }
                    }
                }
            }
        }

        // Channels table (HTMX target)
        div {
            id = "channels-table"
            div("card") {
                if (channels.isEmpty()) {
                    emptyState(
                        icon = "monitor",
                        title = "No channels found",
                        description = if (filters.hasActiveFilters())
                            "Try adjusting your filters or create a new channel."
                        else
                            "Create your first channel to start organizing content for your displays.",
                        actionHref = "/admin/channels/new",
                        actionLabel = "Create Channel"
                    )
                } else {
                    channelTable(channels)
                }
            }
            // Pagination with HTMX
            if (pagination != null) {
                hxPagination(
                    currentPage = pagination.currentPage,
                    totalPages = pagination.totalPages,
                    totalItems = pagination.totalItems,
                    baseUrl = "/admin/channels/table",
                    target = "#channels-table"
                )
            }
        }
    }
}

/**
 * Channel table component with improved styling.
 */
fun FlowContent.channelTable(channels: List<ChannelListItem>) {
    table("table") {
        thead {
            tr {
                th { +"Channel Name" }
                th { +"Status" }
                th { +"Devices" }
                th { +"Schedule Items" }
                th { +"Last Published" }
                th { }
            }
        }
        tbody {
            channels.forEach { channel ->
                tr {
                    td {
                        a(href = "/admin/channels/${channel.id}", classes = "font-medium") {
                            +channel.name
                        }
                    }
                    td {
                                val badgeClass = when (channel.status) {
                                    ChannelStatus.ACTIVE -> "badge-success"
                                    ChannelStatus.PAUSED -> "badge-warning"
                                }
                                val dotClass = when (channel.status) {
                                    ChannelStatus.ACTIVE -> "status-active"
                                    ChannelStatus.PAUSED -> "status-paused"
                                }
                                span("badge $badgeClass") {
                                    span("status-dot $dotClass") {}
                                    +channel.status.name.lowercase()
                                }
                    }
                    td {
                        if (channel.deviceCount > 0) {
                            span("badge badge-gray badge-plain") { +"${channel.deviceCount}" }
                        } else {
                            span("text-muted") { +"—" }
                        }
                    }
                    td {
                        // Schedule info - view detail for full schedule
                        span("text-muted") { +"—" }
                    }
                    td {
                        channel.lastPublish?.let {
                            span("text-muted") { +formatDateTime(it) }
                        } ?: span("text-muted") { +"Never" }
                    }
                    td {
                        div("table-actions") {
                            a(href = "/admin/channels/${channel.id}", classes = "btn btn-secondary btn-sm") {
                                +"View"
                            }
                            a(href = "/admin/channels/${channel.id}/edit", classes = "btn btn-ghost btn-sm") {
                                +"Edit"
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun channelStatusBadge(status: ChannelStatus): String = when (status) {
    ChannelStatus.ACTIVE -> "success"
    ChannelStatus.PAUSED -> "warning"
}

private fun formatDateTime(dateTime: Any): String {
    return dateTime.toString().replace("T", " ").substringBeforeLast(":")
}

/**
 * Extension to check if filters are active.
 */
fun ChannelFilters.hasActiveFilters(): Boolean = status != null || !search.isNullOrBlank() || groupId != null
