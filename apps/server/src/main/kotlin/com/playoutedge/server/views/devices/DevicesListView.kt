package com.playoutedge.server.views.devices

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import com.playoutedge.server.views.statCard
import kotlinx.html.*

/**
 * Devices list view with improved UX.
 */
fun HTML.devicesListView(
    session: AdminClaims,
    devices: List<DeviceViewItem>,
    stats: DeviceStats,
    filters: DeviceFilters,
    channels: List<ChannelOption>
) {
    adminLayout(title = "Devices", userName = session.displayName, currentPath = "/admin/devices") {
        pageHeader(
            title = "Devices",
            subtitle = "Manage your display fleet"
        ) {
            a(href = "/admin/devices/enroll", classes = "btn btn-primary") {
                +"+ Add Device"
            }
        }

        // Stats cards
        div("stats-grid mb-4") {
            div("stat-card") {
                div("stat-icon icon-primary") { +"📺" }
                span("stat-label") { +"Total" }
                span("stat-value") { +"${stats.total}" }
            }
            div("stat-card") {
                div("stat-icon icon-success") { +"✓" }
                span("stat-label") { +"Online" }
                span("stat-value text-success") { +"${stats.online}" }
            }
            div("stat-card") {
                div("stat-icon icon-danger") { +"!" }
                span("stat-label") { +"Offline" }
                span("stat-value text-danger") { +"${stats.offline}" }
            }
            div("stat-card") {
                div("stat-icon icon-warning") { +"⏳" }
                span("stat-label") { +"Pending" }
                span("stat-value text-warning") { +"${stats.pending}" }
            }
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
                                +"All Statuses"
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
                            placeholder = "Search devices..."
                            value = filters.search ?: ""
                        }
                    }
                    div("filter-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-secondary") {
                            +"Apply"
                        }
                        if (filters.hasActiveFilters()) {
                            a(href = "/admin/devices", classes = "btn btn-ghost") {
                                +"Clear"
                            }
                        }
                    }
                }
            }
        }

        // Devices table
        div("card") {
            if (devices.isEmpty()) {
                emptyState(
                    icon = "📺",
                    title = "No devices found",
                    description = if (filters.hasActiveFilters())
                        "Try adjusting your filters or add a new device."
                    else
                        "Generate an enrollment code to add your first device.",
                    actionHref = "/admin/devices/enroll",
                    actionLabel = "Add Device"
                )
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Device" }
                            th { +"Status" }
                            th { +"Channel" }
                            th { +"App Version" }
                            th { +"Last Seen" }
                            th { }
                        }
                    }
                    tbody {
                        devices.forEach { device ->
                            tr {
                                td {
                                    a(href = "/admin/devices/${device.id}", classes = "font-medium") {
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
                                    device.channelName?.let {
                                        span("font-medium") { +it }
                                    } ?: span("text-muted") { +"Not assigned" }
                                }
                                td {
                                    device.appVersion?.let {
                                        span("badge badge-gray badge-plain") { +it }
                                    } ?: span("text-muted") { +"—" }
                                }
                                td {
                                    device.lastSeen?.let {
                                        span("text-muted") { +formatDateTime(it) }
                                    } ?: span("text-muted") { +"Never" }
                                }
                                td {
                                    div("table-actions") {
                                        a(href = "/admin/devices/${device.id}", classes = "btn btn-secondary btn-sm") {
                                            +"View"
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

private fun formatDateTime(dateTime: Any): String {
    return dateTime.toString().replace("T", " ").substringBeforeLast(":")
}

fun DeviceFilters.hasActiveFilters(): Boolean =
    status != null || channelId != null || !search.isNullOrBlank()
