package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*

/**
 * Channels list view.
 */
fun HTML.channelsListView(
    session: AdminClaims,
    channels: List<ChannelListItem>,
    filters: ChannelFilters
) {
    adminLayout(title = "Channels", userName = "Admin") {
        div("page-header") {
            h1("page-title") { +"Channels" }
            a(href = "/admin/channels/new", classes = "btn btn-primary") {
                +"Create Channel"
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
                                +"All"
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
                            placeholder = "Channel name..."
                            value = filters.search ?: ""
                        }
                    }
                    button(type = ButtonType.submit, classes = "btn btn-secondary") {
                        +"Filter"
                    }
                }
            }
        }

        // Channels table
        div("card") {
            if (channels.isEmpty()) {
                div("empty-state") {
                    p("empty-state-title") { +"No channels found" }
                    p("empty-state-text") { +"Create your first channel to get started." }
                    a(href = "/admin/channels/new", classes = "btn btn-primary") {
                        +"Create Channel"
                    }
                }
            } else {
                channelTable(channels)
            }
        }
    }
}

/**
 * Channel table component.
 */
fun FlowContent.channelTable(channels: List<ChannelListItem>) {
    table("table") {
        thead {
            tr {
                th { +"Name" }
                th { +"Status" }
                th { +"Devices" }
                th { +"Last Publish" }
                th { +"Actions" }
            }
        }
        tbody {
            channels.forEach { channel ->
                tr {
                    td {
                        a(href = "/admin/channels/${channel.id}") {
                            +channel.name
                        }
                    }
                    td {
                        span("badge badge-${channelStatusBadge(channel.status)}") {
                            +channel.status.name.lowercase()
                        }
                    }
                    td { +"${channel.deviceCount}" }
                    td {
                        channel.lastPublish?.let { +it.toString() } ?: span("text-muted") { +"Never" }
                    }
                    td {
                        a(href = "/admin/channels/${channel.id}", classes = "btn btn-secondary btn-sm") {
                            +"View"
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
