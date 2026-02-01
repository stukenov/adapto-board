package com.playoutedge.server.views.devices

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*

/**
 * Device detail view.
 */
fun HTML.deviceDetailView(
    session: AdminClaims,
    device: DeviceDetailModel,
    channels: List<ChannelOption>
) {
    adminLayout(title = device.name, userName = "Admin") {
        // Header
        div("page-header") {
            div {
                a(href = "/admin/devices", classes = "link") { +"← Back to Devices" }
                h1("page-title") { +device.name }
            }
            div("header-actions") {
                if (device.isOnline) {
                    span("badge badge-success") { +"online" }
                } else {
                    span("badge badge-gray") { +"offline" }
                }
            }
        }

        // Info cards
        div("dashboard-grid mb-4") {
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
                            device.lastSeen?.let { +it.toString() } ?: span("text-muted") { +"Never" }
                        }
                    }
                }
            }

            // Channel card
            div("card") {
                div("card-header") {
                    h3 { +"Channel" }
                }
                div("card-body") {
                    if (device.channelId != null) {
                        a(href = "/admin/channels/${device.channelId}") {
                            +(device.channelName ?: "Unknown")
                        }
                    } else {
                        span("text-muted") { +"Not assigned" }
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
                        dd { +(device.appVersion ?: "—") }

                        dt { +"Android Version" }
                        dd { +(device.androidVersion ?: "—") }

                        dt { +"Model" }
                        dd { +(device.androidModel ?: "—") }
                    }
                }
            }
        }

        // Actions section
        div("card mb-4") {
            div("card-header") {
                h3 { +"Actions" }
            }
            div("card-body") {
                div("quick-actions") {
                    // Assign channel form
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
                            +"Assign Channel"
                        }
                    }
                }
            }
        }

        // Device info
        div("card") {
            div("card-header") {
                h3 { +"Details" }
            }
            div("card-body") {
                dl("info-list") {
                    dt { +"Device ID" }
                    dd {
                        code { +device.id.toString() }
                    }

                    dt { +"Created" }
                    dd { +device.createdAt.toString() }
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
