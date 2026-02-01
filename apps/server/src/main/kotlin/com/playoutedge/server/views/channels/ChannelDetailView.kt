package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*

/**
 * Channel detail view.
 */
fun HTML.channelDetailView(
    session: AdminClaims,
    channel: ChannelDetail,
    scheduleItems: List<ScheduleItemView>,
    devices: List<DeviceListItem>
) {
    adminLayout(title = channel.name, userName = "Admin") {
        // Header
        div("page-header") {
            div {
                a(href = "/admin/channels", classes = "link") { +"← Back to Channels" }
                h1("page-title") { +channel.name }
            }
            div("header-actions") {
                span("badge badge-${channelStatusBadge(channel.status)} mr-2") {
                    +channel.status.name.lowercase()
                }
            }
        }

        // Stats row
        div("dashboard-grid mb-4") {
            // Devices card
            div("card") {
                div("card-header") {
                    h3 { +"Devices" }
                }
                div("card-body") {
                    val onlineCount = devices.count { it.isOnline }
                    div("stat-row") {
                        div("stat") {
                            span("stat-value") { +"$onlineCount" }
                            span("stat-label") { +"Online" }
                        }
                        div("stat") {
                            span("stat-value") { +"${devices.size}" }
                            span("stat-label") { +"Total" }
                        }
                    }
                }
            }

            // Schedule card
            div("card") {
                div("card-header") {
                    h3 { +"Schedule" }
                }
                div("card-body") {
                    div("stat-row") {
                        div("stat") {
                            span("stat-value") { +"${scheduleItems.size}" }
                            span("stat-label") { +"Items" }
                        }
                    }
                }
            }
        }

        // Schedule section
        div("card mb-4") {
            div("card-header") {
                h3 { +"Schedule Items" }
                a(href = "/admin/channels/${channel.id}/schedule", classes = "btn btn-secondary btn-sm") {
                    +"Edit Schedule"
                }
            }
            div("card-body") {
                if (scheduleItems.isEmpty()) {
                    div("empty-state") {
                        p { +"No schedule items" }
                        a(href = "/admin/channels/${channel.id}/schedule", classes = "btn btn-primary") {
                            +"Add Items"
                        }
                    }
                } else {
                    scheduleTable(scheduleItems)
                }
            }
        }

        // Devices section
        div("card") {
            div("card-header") {
                h3 { +"Assigned Devices" }
            }
            div("card-body") {
                if (devices.isEmpty()) {
                    div("empty-state") {
                        p { +"No devices assigned to this channel" }
                    }
                } else {
                    deviceTable(devices)
                }
            }
        }
    }
}

/**
 * Schedule table component.
 */
fun FlowContent.scheduleTable(items: List<ScheduleItemView>) {
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
                tr {
                    td { +"${item.sortOrder}" }
                    td {
                        a(href = "/admin/assets/${item.assetId}") {
                            +item.assetName
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

/**
 * Device table component.
 */
fun FlowContent.deviceTable(devices: List<DeviceListItem>) {
    table("table") {
        thead {
            tr {
                th { +"Name" }
                th { +"Status" }
                th { +"Last Seen" }
            }
        }
        tbody {
            devices.forEach { device ->
                tr {
                    td {
                        a(href = "/admin/devices/${device.id}") {
                            +device.name
                        }
                    }
                    td {
                        if (device.isOnline) {
                            span("badge badge-success") { +"online" }
                        } else {
                            span("badge badge-gray") { +"offline" }
                        }
                    }
                    td {
                        device.lastSeen?.let { +it.toString() } ?: span("text-muted") { +"Never" }
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
