package com.playoutedge.server.views.devices

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*

/**
 * Device detail view with improved layout.
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

        // Info cards
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

                    // Assign form
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
                            } ?: span("text-muted") { +"—" }
                        }

                        dt { +"Android Version" }
                        dd { +(device.androidVersion ?: "—") }

                        dt { +"Device Model" }
                        dd { +(device.androidModel ?: "—") }
                    }
                }
            }
        }

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
    }
}

private fun enrollStatusBadge(status: com.playoutedge.domain.enums.DeviceEnrollStatus): String = when (status) {
    com.playoutedge.domain.enums.DeviceEnrollStatus.ENROLLED -> "success"
    com.playoutedge.domain.enums.DeviceEnrollStatus.PENDING -> "warning"
    com.playoutedge.domain.enums.DeviceEnrollStatus.REJECTED -> "danger"
}
