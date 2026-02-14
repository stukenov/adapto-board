package com.playoutedge.server.views.auth

import com.playoutedge.server.views.authLayout
import kotlinx.html.*

fun HTML.suspendedView() {
    authLayout("Account Suspended") {
        div("auth-container") {
            div("auth-card") {
                div("auth-logo") { +"P" }
                div("auth-header") {
                    h1 { +"Account Suspended" }
                    p { +"Your organization's account has been suspended." }
                }
                div("card") {
                    div("card-body") {
                        p { +"Please contact support to resolve this issue." }
                    }
                }
                div("auth-footer") {
                    form(action = "/admin/logout", method = FormMethod.post) {
                        button(type = ButtonType.submit, classes = "btn btn-secondary") { +"Sign Out" }
                    }
                }
            }
        }
    }
}
