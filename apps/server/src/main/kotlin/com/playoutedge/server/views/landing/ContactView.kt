package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.contactView() {
    publicLayout("Contact", currentPath = "/contact") {
        section("section") {
            div("section-inner") {
                div("section-header") {
                    h1 { +"Contact Us" }
                    p { +"We'd love to hear from you. Reach out to our team." }
                }
                div("contact-grid") {
                    div("contact-card") {
                        h3 { +"Sales" }
                        p { +"Interested in Playout Edge for your business? Our sales team can help you find the right plan." }
                        p { a(href = "mailto:sales@playoutedge.com") { +"sales@playoutedge.com" } }
                    }
                    div("contact-card") {
                        h3 { +"Support" }
                        p { +"Need help with your account or have a technical question? Our support team is here to help." }
                        p { a(href = "mailto:support@playoutedge.com") { +"support@playoutedge.com" } }
                    }
                    div("contact-card") {
                        h3 { +"Enterprise" }
                        p { +"Looking for custom solutions, on-premise deployment, or volume licensing? Let's talk." }
                        p { a(href = "mailto:enterprise@playoutedge.com") { +"enterprise@playoutedge.com" } }
                    }
                }
            }
        }
    }
}
