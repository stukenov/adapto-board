package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.server.views.*
import kotlinx.html.*

/**
 * Channel detail view with improved layout.
 */
fun HTML.channelDetailView(
    session: AdminClaims,
    channel: ChannelDetail,
    scheduleItems: List<ScheduleItemView>,
    devices: List<DeviceListItem>,
    validationWarnings: List<String> = emptyList(),
    embedHost: String = "tv.adapto.kz"
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
            a(href = "/admin/channels/${channel.id}/live", classes = "btn btn-secondary") {
                +"Live Preview"
            }
            a(href = "/admin/channels/${channel.id}/edit", classes = "btn btn-secondary") {
                +"Edit Channel"
            }
        }

        // Tabs
        pageTabs(listOf(
            TabDef("Overview", "/admin/channels/${channel.id}"),
            TabDef("Schedule", "/admin/channels/${channel.id}/schedule", scheduleItems.size),
            TabDef("Devices", "/admin/channels/${channel.id}", devices.size)
        ), "/admin/channels/${channel.id}")

        // Two-column layout
        detailLayout(
            main = {
                // Schedule section
                sectionCard("Schedule", actions = {
                    a(href = "/admin/channels/${channel.id}/schedule", classes = "btn btn-secondary btn-sm") {
                        +"Edit Schedule"
                    }
                }) {
                    if (scheduleItems.isEmpty()) {
                        emptyState(
                            icon = "clipboard",
                            title = "No schedule items",
                            description = "Add videos or images to this channel's schedule.",
                            actionHref = "/admin/channels/${channel.id}/schedule",
                            actionLabel = "Add Items"
                        )
                    } else {
                        scheduleTable(scheduleItems, channel.id)
                    }
                }

                // Validation warnings
                if (validationWarnings.isNotEmpty()) {
                    sectionCard("Schedule Warnings") {
                        div("alert alert-warning") {
                            strong { +"${validationWarnings.size} issue(s) found:" }
                            ul {
                                style = "margin:0.5rem 0 0;padding-left:1.2rem;"
                                validationWarnings.forEach { w -> li { +w } }
                            }
                        }
                    }
                }

                // Embed Player
                sectionCard("Embed Player", actions = {
                    button(classes = "btn btn-secondary btn-sm") {
                        id = "copy-embed-btn"
                        +"Copy Embed Code"
                    }
                }) {
                    div("embed-preview-container") {
                        iframe {
                            id = "embed-preview-iframe"
                            src = "/embed/${channel.id}"
                            attributes["width"] = "100%"
                            attributes["height"] = "400"
                            attributes["frameborder"] = "0"
                            attributes["allowfullscreen"] = "true"
                            attributes["style"] = "border-radius: var(--radius); background: #000;"
                        }
                    }
                    div("mt-3 mb-3") {
                        h4("text-sm font-medium mb-2") { +"Customize Embed" }
                        div("filter-row") {
                            div("form-group") {
                                label { +"Background Color" }
                                input(type = InputType.color, classes = "form-control") {
                                    id = "embed-bg-color"
                                    value = "#000000"
                                }
                            }
                            div("form-group") {
                                label("day-checkbox") {
                                    input(type = InputType.checkBox) { id = "embed-muted"; checked = true }
                                    +"Muted"
                                }
                            }
                            div("form-group") {
                                label("day-checkbox") {
                                    input(type = InputType.checkBox) { id = "embed-controls" }
                                    +"Show Controls"
                                }
                            }
                        }
                    }
                    div("embed-code-box mt-3") {
                        id = "embed-code"
                        code {
                            id = "embed-code-text"
                            +"""<iframe src="https://$embedHost/embed/${channel.id}" width="1920" height="1080" frameborder="0" allowfullscreen></iframe>"""
                        }
                    }
                }
            },
            sidebar = {
                // Channel Info
                sectionCard("Channel Info") {
                    dl("info-list") {
                        dt { +"Status" }
                        dd {
                            span("badge badge-${channelStatusBadge(channel.status)}") {
                                +channel.status.name.lowercase()
                            }
                        }
                        dt { +"Schedule Items" }
                        dd { +"${scheduleItems.size}" }
                        dt { +"Total Duration" }
                        dd { +calculateTotalDuration(scheduleItems) }
                    }
                }

                // Devices
                sectionCard("Assigned Devices", actions = {
                    a(href = "/admin/devices?channel=${channel.id}", classes = "btn btn-secondary btn-sm") { +"Manage" }
                }) {
                    if (devices.isEmpty()) {
                        emptyState(
                            icon = "monitor",
                            title = "No devices assigned",
                            description = "Assign devices to this channel.",
                            actionHref = "/admin/devices/enroll",
                            actionLabel = "Add Device"
                        )
                    } else {
                        deviceTable(devices)
                    }
                }
            }
        )

        script {
            unsafe {
                +"""
                (function() {
                    var channelId = '${channel.id}';
                    var embedHost = '${embedHost}';
                    var codeEl = document.getElementById('embed-code-text');
                    var bgInput = document.getElementById('embed-bg-color');
                    var mutedInput = document.getElementById('embed-muted');
                    var controlsInput = document.getElementById('embed-controls');

                    function updateEmbedCode() {
                        var params = [];
                        if (bgInput.value !== '#000000') params.push('bg=' + encodeURIComponent(bgInput.value));
                        if (!mutedInput.checked) params.push('muted=false');
                        if (controlsInput.checked) params.push('controls=true');
                        var qs = params.length > 0 ? '?' + params.join('&') : '';
                        var url = 'https://' + embedHost + '/embed/' + channelId + qs;
                        codeEl.textContent = '<iframe src="' + url + '" width="1920" height="1080" frameborder="0" allowfullscreen></iframe>';
                    }

                    bgInput.addEventListener('input', updateEmbedCode);
                    mutedInput.addEventListener('change', updateEmbedCode);
                    controlsInput.addEventListener('change', updateEmbedCode);

                    document.getElementById('copy-embed-btn').addEventListener('click', function() {
                        var code = codeEl.textContent || '';
                        navigator.clipboard.writeText(code).then(function() {
                            var btn = document.getElementById('copy-embed-btn');
                            btn.textContent = 'Copied!';
                            setTimeout(function() { btn.textContent = 'Copy Embed Code'; }, 2000);
                        });
                    });
                })();
                """.trimIndent()
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
