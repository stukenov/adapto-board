package com.playoutedge.server.views.devices

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*

/**
 * Enroll codes view.
 */
fun HTML.enrollCodesView(
    session: AdminClaims,
    channels: List<ChannelOption>,
    activeCodes: List<EnrollCodeItem>,
    generatedCode: String? = null
) {
    adminLayout(title = "Enroll Codes", userName = "Admin") {
        div("page-header") {
            div {
                a(href = "/admin/devices", classes = "link") { +"← Back to Devices" }
                h1("page-title") { +"Enroll Codes" }
            }
        }

        // Show generated code if present
        if (generatedCode != null) {
            div("card mb-4") {
                div("card-header") {
                    h3 { +"Generated Code" }
                }
                div("card-body") {
                    div("generated-code") {
                        code("code-display") { +generatedCode }
                        p("text-muted mt-4") {
                            +"Share this code with the device operator. It will expire in 24 hours."
                        }
                    }
                }
            }
        }

        // Generate form
        div("card mb-4") {
            div("card-header") {
                h3 { +"Generate New Code" }
            }
            div("card-body") {
                form(action = "/admin/devices/enroll/generate", method = FormMethod.post, classes = "auth-form") {
                    div("form-group") {
                        label { +"Bind to Channel (optional)" }
                        select("form-control") {
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
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
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
            }
            div("card-body") {
                if (activeCodes.isEmpty()) {
                    div("empty-state") {
                        p { +"No active codes" }
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
                                        code { +code.code }
                                    }
                                    td {
                                        code.channelName?.let { +it } ?: span("text-muted") { +"Any" }
                                    }
                                    td { +code.expiresAt.toString() }
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
}
