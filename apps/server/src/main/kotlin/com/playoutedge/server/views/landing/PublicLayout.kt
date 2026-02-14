package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.publicLayout(
    title: String,
    currentPath: String = "",
    content: MAIN.() -> Unit
) {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title { +"$title - Playout Edge" }
        link(rel = "stylesheet", href = "/admin/static/styles.css")
        link(rel = "icon", type = "image/svg+xml", href = "data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><rect fill='%23d97706' rx='20' width='100' height='100'/><text y='.9em' x='50%' text-anchor='middle' font-size='60' fill='white'>P</text></svg>")
    }
    body {
        nav("public-nav") {
            div("public-nav-inner") {
                a(href = "/", classes = "nav-brand") {
                    div("nav-brand-icon") { +"P" }
                    +"Playout Edge"
                }
                div("public-nav-links") {
                    a(href = "/features", classes = if (currentPath == "/features") "active" else "") { +"Features" }
                    a(href = "/pricing", classes = if (currentPath == "/pricing") "active" else "") { +"Pricing" }
                    a(href = "/faq", classes = if (currentPath == "/faq") "active" else "") { +"FAQ" }
                    a(href = "/contact", classes = if (currentPath == "/contact") "active" else "") { +"Contact" }
                }
                div("public-nav-actions") {
                    a(href = "/admin/login", classes = "btn btn-ghost") { +"Sign In" }
                    a(href = "/signup", classes = "btn btn-primary") { +"Sign Up" }
                }
            }
        }

        main("public-main") {
            content()
        }

        footer("public-footer") {
            div("public-footer-inner") {
                div("footer-grid") {
                    div("footer-section") {
                        h4 { +"Product" }
                        ul {
                            li { a(href = "/features") { +"Features" } }
                            li { a(href = "/pricing") { +"Pricing" } }
                            li { a(href = "/faq") { +"FAQ" } }
                        }
                    }
                    div("footer-section") {
                        h4 { +"Company" }
                        ul {
                            li { a(href = "/contact") { +"Contact" } }
                        }
                    }
                    div("footer-section") {
                        h4 { +"Support" }
                        ul {
                            li { a(href = "/admin/login") { +"Sign In" } }
                            li { a(href = "/signup") { +"Create Account" } }
                        }
                    }
                }
                div("footer-bottom") {
                    p { +"© 2026 Playout Edge. All rights reserved." }
                }
            }
        }
    }
}
