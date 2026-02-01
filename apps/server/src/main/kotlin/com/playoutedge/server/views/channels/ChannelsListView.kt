package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*

/**
 * Channels list view with improved UX.
 */
fun HTML.channelsListView(
    session: AdminClaims,
    channels: List<ChannelListItem>,
    filters: ChannelFilters
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
                        label { +"Status" }
                        select("form-control") {
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
                    div("form-group") {
                        label { +"Search" }
                        input(type = InputType.text, classes = "form-control") {
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
                    icon = "📺",
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
                        span("badge badge-${channelStatusBadge(channel.status)}") {
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
fun ChannelFilters.hasActiveFilters(): Boolean = status != null || !search.isNullOrBlank()
