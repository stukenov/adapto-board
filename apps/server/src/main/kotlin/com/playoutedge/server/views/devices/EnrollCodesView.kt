package com.playoutedge.server.views.devices

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.alertBox
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*

/**
 * Enroll codes view with improved UX.
 */
fun HTML.enrollCodesView(
    session: AdminClaims,
    channels: List<ChannelOption>,
    activeCodes: List<EnrollCodeItem>,
    generatedCode: String? = null
) {
    adminLayout(title = "Add Device", userName = session.displayName, currentPath = "/admin/devices") {
        pageHeader(
            title = "Add Device",
            subtitle = "Generate enrollment codes for new devices",
            backHref = "/admin/devices",
            backLabel = "Back to Devices"
        )

        // Show generated code prominently
        if (generatedCode != null) {
            div("card mb-4") {
                div("card-header") {
                    h3 { +"Enrollment Code Generated" }
                }
                div("card-body") {
                    div("enroll-code-display mb-4") {
                        div("code-box") { +generatedCode }
                    }
                    alertBox(
                        "Share this code with the device operator. Enter it in the Playout Edge app to enroll the device. The code expires in 24 hours.",
                        "info"
                    )
                }
            }
        }

        // Generate form
        div("card mb-4") {
            div("card-header") {
                h3 { +"Generate New Code" }
            }
            div("card-body") {
                form(action = "/admin/devices/enroll/generate", method = FormMethod.post) {
                    div("form-group") {
                        label {
                            htmlFor = "channelId"
                            +"Bind to Channel"
                        }
                        select("form-control") {
                            id = "channelId"
                            name = "channelId"
                            option {
                                value = ""
                                selected = true
                                +"No channel (assign later)"
                            }
                            channels.forEach { channel ->
                                option {
                                    value = channel.id.toString()
                                    +channel.name
                                }
                            }
                        }
                        small("form-helper") {
                            +"Optionally bind the device to a channel automatically upon enrollment."
                        }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary btn-lg") {
                            +"Generate Code"
                        }
                    }
                }
            }
        }

        // Active codes list
        div("card") {
            div("card-header") {
                h3 { +"Active Codes" }
                if (activeCodes.isNotEmpty()) {
                    span("badge badge-gray badge-plain") { +"${activeCodes.size}" }
                }
            }
            if (activeCodes.isEmpty()) {
                div("card-body") {
                    emptyState(
                        icon = "🔑",
                        title = "No active codes",
                        description = "Generate a code above to start enrolling devices."
                    )
                }
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Code" }
                            th { +"Channel" }
                            th { +"Expires" }
                            th { +"Status" }
                        }
                    }
                    tbody {
                        activeCodes.forEach { code ->
                            tr {
                                td {
                                    code("font-medium") { +code.code }
                                }
                                td {
                                    code.channelName?.let { +it } ?: span("text-muted") { +"Any channel" }
                                }
                                td {
                                    span("text-muted") {
                                        +code.expiresAt.toString().replace("T", " ").substringBeforeLast(":")
                                    }
                                }
                                td {
                                    if (code.isExpired) {
                                        span("badge badge-gray") { +"expired" }
                                    } else {
                                        span("badge badge-success") { +"active" }
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
