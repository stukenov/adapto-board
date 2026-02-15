package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.server.views.PaginationInfo
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import com.playoutedge.server.views.paginationNav
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

        // Filters
        div("card mb-4") {
            form(action = "/admin/channels", method = FormMethod.get, classes = "filter-form") {
                div("filter-row") {
                    div("form-group") {
                        label {
                            htmlFor = "filter-status"
                            +"Status"
                        }
                        select("form-control") {
                            id = "filter-status"
                            name = "status"
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
                    if (groups.isNotEmpty()) {
                        div("form-group") {
                            label {
                                htmlFor = "filter-group"
                                +"Group"
                            }
                            select("form-control") {
                                id = "filter-group"
                                name = "group"
                                option {
                                    value = ""
                                    if (filters.groupId == null) selected = true
                                    +"All Groups"
                                }
                                groups.forEach { group ->
                                    option {
                                        value = group.id.toString()
                                        if (filters.groupId == group.id) selected = true
                                        +group.name
                                    }
                                }
                            }
                        }
                    }
                    div("form-group") {
                        label {
                            htmlFor = "filter-search"
                            +"Search"
                        }
                        input(type = InputType.text, classes = "form-control") {
                            id = "filter-search"
                            name = "search"
                            placeholder = "Search channels..."
                            value = filters.search ?: ""
                        }
                    }
                    div("filter-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-secondary") {
                            +"Apply"
                        }
                        if (filters.hasActiveFilters()) {
                            a(href = "/admin/channels", classes = "btn btn-ghost") {
                                +"Clear"
                            }
                        }
                    }
                }
            }
        }

        // Channels table
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

        // Pagination
        if (pagination != null) {
            paginationNav(pagination)
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
