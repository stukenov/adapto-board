package com.playoutedge.server.views.devices

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.DeviceEnrollStatus
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*

/**
 * Devices list view.
 */
fun HTML.devicesListView(
    session: AdminClaims,
    devices: List<DeviceViewItem>,
    stats: DeviceStats,
    filters: DeviceFilters,
    channels: List<ChannelOption>
) {
    adminLayout(title = "Devices", userName = "Admin") {
        div("page-header") {
            h1("page-title") { +"Devices" }
            a(href = "/admin/devices/enroll", classes = "btn btn-primary") {
                +"Generate Enroll Code"
            }
        }

        // Stats cards
        div("dashboard-grid mb-4") {
            statCard("Total", stats.total.toString(), "gray")
            statCard("Online", stats.online.toString(), "success")
            statCard("Offline", stats.offline.toString(), "danger")
            statCard("Pending", stats.pending.toString(), "warning")
        }

        // Filters
        div("card mb-4") {
            form(action = "/admin/devices", method = FormMethod.get, classes = "filter-form") {
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
                            option {
                                value = "online"
                                if (filters.status == "online") selected = true
                                +"Online"
                            }
                            option {
                                value = "offline"
                                if (filters.status == "offline") selected = true
                                +"Offline"
                            }
                        }
                    }
                    div("form-group") {
                        label { +"Channel" }
                        select("form-control") {
                            name = "channel"
                            option {
                                value = ""
                                if (filters.channelId == null) selected = true
                                +"All Channels"
                            }
                            channels.forEach { channel ->
                                option {
                                    value = channel.id.toString()
                                    if (filters.channelId == channel.id) selected = true
                                    +channel.name
                                }
                            }
                        }
                    }
                    div("form-group") {
                        label { +"Search" }
                        input(type = InputType.text, classes = "form-control") {
                            name = "search"
                            placeholder = "Device name..."
                            value = filters.search ?: ""
                        }
                    }
                    button(type = ButtonType.submit, classes = "btn btn-secondary") {
                        +"Filter"
                    }
                }
            }
        }

        // Devices table
        div("card") {
            if (devices.isEmpty()) {
                div("empty-state") {
                    p("empty-state-title") { +"No devices found" }
                    p("empty-state-text") { +"Generate an enroll code to add devices." }
                    a(href = "/admin/devices/enroll", classes = "btn btn-primary") {
                        +"Generate Enroll Code"
                    }
                }
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Name" }
                            th { +"Status" }
                            th { +"Channel" }
                            th { +"App Version" }
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
                                    device.channelName?.let { +it } ?: span("text-muted") { +"—" }
                                }
                                td {
                                    device.appVersion?.let { +it } ?: span("text-muted") { +"—" }
                                }
                                td {
                                    device.lastSeen?.let { +it.toString() } ?: span("text-muted") { +"Never" }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Stat card component.
 */
fun FlowContent.statCard(label: String, value: String, variant: String) {
    div("stat-card") {
        span("stat-label") { +label }
        span("stat-value text-$variant") { +value }
    }
}
