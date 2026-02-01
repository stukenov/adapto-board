package com.playoutedge.server.views

import kotlinx.html.*

/**
 * Base layout for admin pages.
 */
fun HTML.adminLayout(
    title: String,
    userName: String? = null,
    content: MAIN.() -> Unit
) {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title { +"$title - Playout Edge" }
        link(rel = "stylesheet", href = "/admin/static/styles.css")
    }
    body {
        if (userName != null) {
            nav("admin-nav") {
                div("nav-brand") {
                    a(href = "/admin") { +"Playout Edge" }
                }
                div("nav-links") {
                    a(href = "/admin", classes = "nav-link") { +"Dashboard" }
                    a(href = "/admin/channels", classes = "nav-link") { +"Channels" }
                    a(href = "/admin/devices", classes = "nav-link") { +"Devices" }
                    a(href = "/admin/assets", classes = "nav-link") { +"Assets" }
                }
                div("nav-user") {
                    userDropdown(userName)
                }
            }
        }
        main("admin-main") {
            content()
        }
    }
}

/**
 * Minimal layout for login page.
 */
fun HTML.authLayout(
    title: String,
    content: MAIN.() -> Unit
) {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title { +"$title - Playout Edge" }
        link(rel = "stylesheet", href = "/admin/static/styles.css")
    }
    body("auth-body") {
        main("auth-main") {
            content()
        }
    }
}

/**
 * User dropdown menu.
 */
fun FlowContent.userDropdown(userName: String) {
    div("dropdown") {
        button(classes = "dropdown-toggle") {
            +userName
            span("caret") { +" ▼" }
        }
        div("dropdown-menu") {
            a(href = "/admin/profile", classes = "dropdown-item") { +"Profile" }
            div("dropdown-divider")
            form(action = "/admin/logout", method = FormMethod.post, classes = "dropdown-item") {
                button(type = ButtonType.submit, classes = "logout-btn") { +"Logout" }
            }
        }
    }
}
