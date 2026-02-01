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
