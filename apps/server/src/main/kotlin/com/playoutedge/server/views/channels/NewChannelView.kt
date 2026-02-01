package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*

/**
 * New channel form view.
 */
fun HTML.newChannelView(
    session: AdminClaims,
    error: String? = null
) {
    adminLayout(title = "Create Channel", userName = "Admin") {
        div("page-header") {
            div {
                a(href = "/admin/channels", classes = "link") { +"← Back to Channels" }
                h1("page-title") { +"Create Channel" }
            }
        }

        div("card") {
            div("card-body") {
                if (error != null) {
                    div("alert alert-error") { +error }
                }

                form(action = "/admin/channels", method = FormMethod.post, classes = "auth-form") {
                    div("form-group") {
                        label { +"Channel Name" }
                        input(type = InputType.text, classes = "form-control") {
                            name = "name"
                            placeholder = "Enter channel name"
                            required = true
                            autoFocus = true
                        }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Create Channel"
                        }
                        a(href = "/admin/channels", classes = "btn btn-secondary") {
                            +"Cancel"
                        }
                    }
                }
            }
        }
    }
}

/**
 * Edit channel form view.
 */
fun HTML.editChannelView(
    session: AdminClaims,
    channel: ChannelDetail,
    error: String? = null
) {
    adminLayout(title = "Edit ${channel.name}", userName = "Admin") {
        div("page-header") {
            div {
                a(href = "/admin/channels/${channel.id}", classes = "link") { +"← Back to Channel" }
                h1("page-title") { +"Edit Channel" }
            }
        }

        div("card") {
            div("card-body") {
                if (error != null) {
                    div("alert alert-error") { +error }
                }

                form(action = "/admin/channels/${channel.id}", method = FormMethod.post, classes = "auth-form") {
                    div("form-group") {
                        label { +"Channel Name" }
                        input(type = InputType.text, classes = "form-control") {
                            name = "name"
                            value = channel.name
                            placeholder = "Enter channel name"
                            required = true
                            autoFocus = true
                        }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Save Changes"
                        }
                        a(href = "/admin/channels/${channel.id}", classes = "btn btn-secondary") {
                            +"Cancel"
                        }
                    }
                }
            }
        }

        // Danger zone
        div("card mt-4") {
            div("card-header") {
                h3 { +"Danger Zone" }
            }
            div("card-body") {
                div("danger-zone") {
                    div("danger-item") {
                        div {
                            strong { +"Delete Channel" }
                            p("text-muted") { +"Once deleted, this channel cannot be recovered." }
                        }
                        form(action = "/admin/channels/${channel.id}/delete", method = FormMethod.post, classes = "inline") {
                            button(type = ButtonType.submit, classes = "btn btn-danger") {
                                attributes["onclick"] = "return confirm('Are you sure you want to delete this channel?')"
                                +"Delete Channel"
                            }
                        }
                    }
                }
            }
        }
    }
}
