package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*

/**
 * Channel detail view with improved layout.
 */
fun HTML.channelDetailView(
    session: AdminClaims,
    channel: ChannelDetail,
    scheduleItems: List<ScheduleItemView>,
    devices: List<DeviceListItem>
) {
    adminLayout(title = channel.name, userName = session.displayName, currentPath = "/admin/channels") {
        // Page header with breadcrumb
        pageHeader(
            title = channel.name,
            backHref = "/admin/channels",
            backLabel = "Back to Channels"
        ) {
            span("badge badge-${channelStatusBadge(channel.status)} mr-2") {
                +channel.status.name.lowercase()
            }
            a(href = "/admin/channels/${channel.id}/live", classes = "btn btn-info") {
                +"Live Preview"
            }
            a(href = "/admin/channels/${channel.id}/edit", classes = "btn btn-secondary") {
                +"Edit Channel"
            }
        }

        // Stats row
        div("stats-grid mb-4") {
            // Devices card
            div("stat-card") {
                val onlineCount = devices.count { it.isOnline }
                div("stat-icon icon-success") { +"📺" }
                span("stat-label") { +"Devices" }
                span("stat-value") {
                    +"$onlineCount"
                    span("text-muted font-normal text-lg") { +" / ${devices.size}" }
                }
                if (devices.isNotEmpty()) {
                    span("text-sm text-muted") { +"$onlineCount online" }
                }
            }

            // Schedule items card
            div("stat-card") {
                div("stat-icon icon-primary") { +"📋" }
                span("stat-label") { +"Schedule" }
                span("stat-value") { +"${scheduleItems.size}" }
                span("text-sm text-muted") { +"items" }
            }

            // Duration card
            div("stat-card") {
                div("stat-icon icon-info") { +"⏱" }
                span("stat-label") { +"Total Duration" }
                span("stat-value") { +calculateTotalDuration(scheduleItems) }
            }
        }

        // Schedule section
        div("card mb-4") {
            div("card-header") {
                h3 { +"Schedule" }
                a(href = "/admin/channels/${channel.id}/schedule", classes = "btn btn-secondary btn-sm") {
                    +"Edit Schedule"
                }
            }
            if (scheduleItems.isEmpty()) {
                div("card-body") {
                    emptyState(
                        icon = "📋",
                        title = "No schedule items",
                        description = "Add videos or images to this channel's schedule.",
                        actionHref = "/admin/channels/${channel.id}/schedule",
                        actionLabel = "Add Items"
                    )
                }
            } else {
                scheduleTable(scheduleItems, channel.id)
            }
        }

        // Embed Player section
        div("card mb-4") {
            div("card-header") {
                h3 { +"Embed Player" }
                button(classes = "btn btn-secondary btn-sm") {
                    id = "copy-embed-btn"
                    +"Copy Embed Code"
                }
            }
            div("card-body") {
                div("embed-preview-container") {
                    iframe {
                        src = "/embed/${channel.id}"
                        attributes["width"] = "100%"
                        attributes["height"] = "400"
                        attributes["frameborder"] = "0"
                        attributes["allowfullscreen"] = "true"
                        attributes["style"] = "border-radius: var(--radius); background: #000;"
                    }
                }
                div("embed-code-box mt-3") {
                    id = "embed-code"
                    code {
                        +"""<iframe src="${"https://tv.adapto.kz/embed/${channel.id}"}" width="1920" height="1080" frameborder="0" allowfullscreen></iframe>"""
                    }
                }
            }
        }

        script {
            unsafe {
                +"""
                document.getElementById('copy-embed-btn')?.addEventListener('click', function() {
                    const code = document.getElementById('embed-code')?.textContent || '';
                    navigator.clipboard.writeText(code).then(function() {
                        const btn = document.getElementById('copy-embed-btn');
                        btn.textContent = 'Copied!';
                        setTimeout(function() { btn.textContent = 'Copy Embed Code'; }, 2000);
                    });
                });
                """.trimIndent()
            }
        }

        // Devices section
        div("card") {
            div("card-header") {
                h3 { +"Assigned Devices" }
                a(href = "/admin/devices?channel=${channel.id}", classes = "btn btn-secondary btn-sm") {
                    +"Manage"
                }
            }
            if (devices.isEmpty()) {
                div("card-body") {
                    emptyState(
                        icon = "📺",
                        title = "No devices assigned",
                        description = "Assign devices to this channel to start playback.",
                        actionHref = "/admin/devices/enroll",
                        actionLabel = "Add Device"
                    )
                }
            } else {
                deviceTable(devices)
            }
        }
    }
}

/**
 * Schedule table component.
 */
fun FlowContent.scheduleTable(items: List<ScheduleItemView>, channelId: Any) {
    table("table") {
        thead {
            tr {
                th { +"#" }
                th { +"Asset" }
                th { +"Type" }
                th { +"Duration" }
                th { +"Time Window" }
            }
        }
        tbody {
            items.forEachIndexed { index, item ->
                tr {
                    td {
                        span("badge badge-gray badge-plain") { +"${index + 1}" }
                    }
                    td {
                        a(href = "/admin/assets/${item.assetId}", classes = "font-medium") {
                            +item.assetName
                        }
                    }
                    td {
                        span("badge badge-gray badge-plain") { +"#${item.sortOrder}" }
                    }
                    td {
                        span("text-muted") { +"—" }
                    }
                    td {
                        if (item.timeStart != null || item.timeEnd != null) {
                            +"${item.timeStart ?: "00:00"} – ${item.timeEnd ?: "23:59"}"
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
 * Device table component for channel detail.
 */
fun FlowContent.deviceTable(devices: List<DeviceListItem>) {
    table("table") {
        thead {
            tr {
                th { +"Device" }
                th { +"Status" }
                th { +"Last Seen" }
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
                        device.lastSeen?.let {
                            span("text-muted") { +it.toString().replace("T", " ").substringBeforeLast(":") }
                        } ?: span("text-muted") { +"Never" }
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

private fun calculateTotalDuration(items: List<ScheduleItemView>): String {
    // Duration not available in the model
    return if (items.isEmpty()) "—" else "${items.size} items"
}
