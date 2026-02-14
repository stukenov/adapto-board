package com.playoutedge.server.views.devices

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import kotlinx.datetime.Clock
import kotlinx.html.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Device detail view with location, power schedule, logs, and remote actions.
 */
fun HTML.deviceDetailView(
    session: AdminClaims,
    device: DeviceDetailModel,
    channels: List<ChannelOption>
) {
    adminLayout(title = device.name, userName = session.displayName, currentPath = "/admin/devices") {
        pageHeader(
            title = device.name,
            backHref = "/admin/devices",
            backLabel = "Back to Devices"
        ) {
            if (device.isOnline) {
                span("badge badge-success badge-lg") { +"Online" }
            } else {
                span("badge badge-gray badge-lg") { +"Offline" }
            }
        }

        // Info cards row 1
        div("stats-grid mb-4") {
            // Status card
            div("card") {
                div("card-header") {
                    h3 { +"Status" }
                }
                div("card-body") {
                    dl("info-list") {
                        dt { +"Enrollment" }
                        dd {
                            span("badge badge-${enrollStatusBadge(device.status)}") {
                                +device.status.name.lowercase()
                            }
                        }

                        dt { +"Last Seen" }
                        dd {
                            device.lastSeen?.let {
                                +it.toString().replace("T", " ").substringBeforeLast(":")
                            } ?: span("text-muted") { +"Never" }
                        }
                    }
                }
            }

            // Health Metrics card
            div("card") {
                div("card-header") {
                    h3 { +"Health Metrics" }
                }
                div("card-body") {
                    dl("info-list") {
                        dt { +"Last Heartbeat" }
                        dd {
                            device.lastSeen?.let {
                                val ago = Clock.System.now() - it
                                val agoStr = when {
                                    ago < 1.minutes -> "just now"
                                    ago < 1.hours -> "${ago.inWholeMinutes} min ago"
                                    else -> "${ago.inWholeHours}h ${ago.inWholeMinutes % 60}m ago"
                                }
                                +agoStr
                            } ?: span("text-muted") { +"Never" }
                        }

                        dt { +"Uptime Estimate" }
                        dd {
                            val now = Clock.System.now()
                            val (label, badge) = when {
                                device.lastSeen != null && device.lastSeen >= now - 5.minutes -> "99%+" to "badge-success"
                                device.lastSeen != null && device.lastSeen >= now - 1.hours -> "~95%" to "badge-warning"
                                else -> "Offline" to "badge-gray"
                            }
                            span("badge $badge") { +label }
                        }
                    }
                }
            }

            // Channel card
            div("card") {
                div("card-header") {
                    h3 { +"Channel Assignment" }
                }
                div("card-body") {
                    if (device.channelId != null) {
                        div("mb-3") {
                            span("text-muted text-sm") { +"Currently assigned to:" }
                            div("mt-1") {
                                a(href = "/admin/channels/${device.channelId}", classes = "font-medium text-lg") {
                                    +(device.channelName ?: "Unknown")
                                }
                            }
                        }
                    } else {
                        p("text-muted mb-3") { +"Not assigned to any channel" }
                    }

                    form(action = "/admin/devices/${device.id}/assign", method = FormMethod.post, classes = "inline-form") {
                        select("form-control") {
                            name = "channelId"
                            option {
                                value = ""
                                if (device.channelId == null) selected = true
                                +"Select channel..."
                            }
                            channels.forEach { channel ->
                                option {
                                    value = channel.id.toString()
                                    if (device.channelId == channel.id) selected = true
                                    +channel.name
                                }
                            }
                        }
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Update"
                        }
                    }
                }
            }
        }

        // Info cards row 2
        div("stats-grid mb-4") {
            // System info card
            div("card") {
                div("card-header") {
                    h3 { +"System Info" }
                }
                div("card-body") {
                    dl("info-list") {
                        dt { +"App Version" }
                        dd {
                            device.appVersion?.let {
                                span("badge badge-gray badge-plain") { +it }
                            } ?: span("text-muted") { +"\u2014" }
                        }

                        dt { +"Android Version" }
                        dd { +(device.androidVersion ?: "\u2014") }

                        dt { +"Device Model" }
                        dd { +(device.androidModel ?: "\u2014") }
                    }
                }
            }

            // Location card
            div("card") {
                div("card-header") {
                    h3 { +"Location" }
                }
                div("card-body") {
                    if (device.latitude != null && device.longitude != null) {
                        dl("info-list") {
                            dt { +"Coordinates" }
                            dd {
                                +"${device.latitude}, ${device.longitude}"
                                +" "
                                a(
                                    href = "https://www.google.com/maps?q=${device.latitude},${device.longitude}",
                                    classes = "btn btn-secondary btn-sm"
                                ) {
                                    target = "_blank"
                                    +"View on Map"
                                }
                            }
                            if (device.locationName != null) {
                                dt { +"Location Name" }
                                dd { +device.locationName }
                            }
                        }
                    } else {
                        p("text-muted mb-3") { +"No location set" }
                    }

                    // Edit location form
                    details {
                        summary { +"Edit Location" }
                        form(action = "/admin/devices/${device.id}/location", method = FormMethod.post, classes = "mt-2") {
                            div("form-group") {
                                label { htmlFor = "locationName"; +"Location Name" }
                                input(type = InputType.text, name = "locationName", classes = "form-control") {
                                    id = "locationName"
                                    placeholder = "e.g. Main Lobby"
                                    value = device.locationName ?: ""
                                }
                            }
                            div("form-row") {
                                div("form-group") {
                                    label { htmlFor = "latitude"; +"Latitude" }
                                    input(type = InputType.number, name = "latitude", classes = "form-control") {
                                        id = "latitude"
                                        placeholder = "e.g. 40.7128"
                                        attributes["step"] = "any"
                                        value = device.latitude?.toString() ?: ""
                                    }
                                }
                                div("form-group") {
                                    label { htmlFor = "longitude"; +"Longitude" }
                                    input(type = InputType.number, name = "longitude", classes = "form-control") {
                                        id = "longitude"
                                        placeholder = "e.g. -74.0060"
                                        attributes["step"] = "any"
                                        value = device.longitude?.toString() ?: ""
                                    }
                                }
                            }
                            button(type = ButtonType.submit, classes = "btn btn-primary") { +"Save Location" }
                        }
                    }
                }
            }

            // Power Schedule card
            div("card") {
                div("card-header") {
                    h3 { +"Power Schedule" }
                }
                div("card-body") {
                    if (device.powerOnTime != null || device.powerOffTime != null) {
                        dl("info-list") {
                            dt { +"Power On" }
                            dd { +(device.powerOnTime ?: "Not set") }
                            dt { +"Power Off" }
                            dd { +(device.powerOffTime ?: "Not set") }
                        }
                    } else {
                        p("text-muted mb-3") { +"No power schedule configured" }
                    }

                    details {
                        summary { +"Edit Power Schedule" }
                        form(action = "/admin/devices/${device.id}/power-schedule", method = FormMethod.post, classes = "mt-2") {
                            div("form-row") {
                                div("form-group") {
                                    label { htmlFor = "powerOnTime"; +"Power On Time" }
                                    input(type = InputType.time, name = "powerOnTime", classes = "form-control") {
                                        id = "powerOnTime"
                                        value = device.powerOnTime ?: ""
                                    }
                                }
                                div("form-group") {
                                    label { htmlFor = "powerOffTime"; +"Power Off Time" }
                                    input(type = InputType.time, name = "powerOffTime", classes = "form-control") {
                                        id = "powerOffTime"
                                        value = device.powerOffTime ?: ""
                                    }
                                }
                            }
                            button(type = ButtonType.submit, classes = "btn btn-primary") { +"Save Schedule" }
                        }
                    }
                }
            }
        }

        // Bottom section: Device Details, Logs link, Remote Actions
        div("stats-grid mb-4") {
            // Device details
            div("card") {
                div("card-header") {
                    h3 { +"Device Details" }
                }
                div("card-body") {
                    dl("info-list") {
                        dt { +"Device ID" }
                        dd {
                            code { +device.id.toString() }
                        }

                        dt { +"Enrolled At" }
                        dd { +device.createdAt.toString().replace("T", " ").substringBeforeLast(":") }
                    }
                }
            }

            // Logs section
            div("card") {
                div("card-header") {
                    h3 { +"Device Logs" }
                }
                div("card-body") {
                    p("text-muted mb-3") { +"View error logs and diagnostics reported by this device." }
                    a(href = "/admin/devices/${device.id}/logs", classes = "btn btn-secondary") {
                        +"View Logs"
                    }
                }
            }

            // Remote Actions section
            div("card") {
                div("card-header") {
                    h3 { +"Remote Actions" }
                }
                div("card-body") {
                    p("text-muted mb-3") { +"Send commands to this device. The device will execute the action on its next heartbeat." }
                    form(action = "/admin/devices/${device.id}/reboot", method = FormMethod.post, classes = "inline-form") {
                        button(type = ButtonType.submit, classes = "btn btn-danger") {
                            attributes["onclick"] = "return confirm('Are you sure you want to reboot this device?')"
                            +"Reboot Device"
                        }
                    }
                }
            }
        }
    }
}

private fun enrollStatusBadge(status: com.playoutedge.domain.enums.DeviceEnrollStatus): String = when (status) {
    com.playoutedge.domain.enums.DeviceEnrollStatus.ENROLLED -> "success"
    com.playoutedge.domain.enums.DeviceEnrollStatus.PENDING -> "warning"
    com.playoutedge.domain.enums.DeviceEnrollStatus.REJECTED -> "danger"
}
