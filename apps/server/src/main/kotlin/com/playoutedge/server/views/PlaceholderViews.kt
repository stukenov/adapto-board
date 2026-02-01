package com.playoutedge.server.views

import com.playoutedge.auth.AdminClaims
import kotlinx.html.*

/**
 * Placeholder view for features under construction.
 */
fun HTML.placeholderView(
    session: AdminClaims,
    title: String,
    description: String,
    backLink: String = "/admin"
) {
    adminLayout(title = title, userName = "Admin") {
        div("page-header") {
            div {
                a(href = backLink, classes = "link") { +"← Back" }
                h1("page-title") { +title }
            }
        }

        div("card") {
            div("empty-state") {
                div("empty-state-icon") { +"🚧" }
                p("empty-state-title") { +"Coming Soon" }
                p("empty-state-text") { +description }
            }
        }
    }
}
