package com.playoutedge.server.views.devices

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.PaginationInfo
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import com.playoutedge.server.views.paginationNav
import com.playoutedge.server.views.statCard
import kotlinx.datetime.Clock
import kotlinx.html.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Devices list view with improved UX.
 */
fun HTML.devicesListView(
    session: AdminClaims,
    devices: List<DeviceViewItem>,
    stats: DeviceStats,
    filters: DeviceFilters,
    channels: List<ChannelOption>,
    pagination: PaginationInfo? = null
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
                        label {
                            htmlFor = "filter-channel"
                            +"Channel"
                        }
                        select("form-control") {
                            id = "filter-channel"
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
                        label {
                            htmlFor = "filter-search"
                            +"Search"
                        }
                        input(type = InputType.text, classes = "form-control") {
                            id = "filter-search"
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
                // Bulk action bar
                div("bulk-action-bar") {
                    id = "bulk-action-bar"
                    style = "display:none; padding: 0.75rem 1rem; background: var(--bg-secondary, #f5f5f5); border-bottom: 1px solid var(--border-color, #e0e0e0); align-items: center; gap: 1rem;"
                    span { id = "bulk-count"; +"0 selected" }
                    form(action = "/admin/devices/bulk-assign", method = FormMethod.post, classes = "inline-form") {
                        id = "bulk-form"
                        style = "display:inline-flex; align-items: center; gap: 0.5rem;"
                        div {
                            id = "bulk-device-ids"
                        }
                        select("form-control") {
                            name = "channelId"
                            style = "width: auto;"
                            option { value = ""; +"Unassign channel" }
                            channels.forEach { channel ->
                                option { value = channel.id.toString(); +channel.name }
                            }
                        }
                        button(type = ButtonType.submit, classes = "btn btn-primary btn-sm") { +"Assign Channel" }
                    }
                }
                table("table") {
                    thead {
                        tr {
                            th {
                                input(type = InputType.checkBox) {
                                    id = "select-all"
                                    attributes["onchange"] = "toggleAllDevices(this.checked)"
                                }
                            }
                            th { +"Device" }
                            th { +"Status" }
                            th { +"Uptime" }
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
                                    input(type = InputType.checkBox, classes = "device-checkbox") {
                                        value = device.id.toString()
                                        attributes["onchange"] = "updateBulkBar()"
                                    }
                                }
                                td {
                                    a(href = "/admin/devices/${device.id}", classes = "font-medium") {
                                        +device.name
                                    }
                                    if (device.latitude != null && device.longitude != null) {
                                        +" "
                                        a(
                                            href = "https://www.google.com/maps?q=${device.latitude},${device.longitude}",
                                            classes = "text-muted"
                                        ) {
                                            target = "_blank"
                                            title = "View on map"
                                            +"\uD83D\uDCCD"
                                        }
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
                                    val now = Clock.System.now()
                                    val uptimeLabel = when {
                                        device.lastSeen != null && device.lastSeen >= now - 5.minutes -> "99%+"
                                        device.lastSeen != null && device.lastSeen >= now - 1.hours -> "~95%"
                                        else -> "Offline"
                                    }
                                    val uptimeBadge = when {
                                        device.lastSeen != null && device.lastSeen >= now - 5.minutes -> "badge-success"
                                        device.lastSeen != null && device.lastSeen >= now - 1.hours -> "badge-warning"
                                        else -> "badge-gray"
                                    }
                                    span("badge $uptimeBadge") { +uptimeLabel }
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
                script {
                    unsafe {
                        +"""
                        function toggleAllDevices(checked) {
                            document.querySelectorAll('.device-checkbox').forEach(function(cb) { cb.checked = checked; });
                            updateBulkBar();
                        }
                        function updateBulkBar() {
                            var checked = document.querySelectorAll('.device-checkbox:checked');
                            var bar = document.getElementById('bulk-action-bar');
                            var count = document.getElementById('bulk-count');
                            var container = document.getElementById('bulk-device-ids');
                            container.innerHTML = '';
                            checked.forEach(function(cb) {
                                var input = document.createElement('input');
                                input.type = 'hidden';
                                input.name = 'deviceIds';
                                input.value = cb.value;
                                container.appendChild(input);
                            });
                            if (checked.length > 0) {
                                bar.style.display = 'flex';
                                count.textContent = checked.length + ' selected';
                            } else {
                                bar.style.display = 'none';
                            }
                        }
                        """.trimIndent()
                    }
                }
            }
        }

        // Pagination
        if (pagination != null) {
            paginationNav(pagination)
        }
    }
}

private fun formatDateTime(dateTime: Any): String {
    return dateTime.toString().replace("T", " ").substringBeforeLast(":")
}

fun DeviceFilters.hasActiveFilters(): Boolean =
    status != null || channelId != null || !search.isNullOrBlank()
