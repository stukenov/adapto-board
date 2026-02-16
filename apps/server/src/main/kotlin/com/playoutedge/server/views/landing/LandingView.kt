package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.landingView() {
    publicLayout("Digital Signage Platform", currentPath = "/") {
        // Hero section
        section("hero") {
            div("hero-content") {
                h1 { +"Digital Signage Made Simple" }
                p("hero-subtitle") {
                    +"Manage screens, schedule content, and engage your audience — all from one powerful platform."
                }
                div("hero-actions") {
                    a(href = "/signup", classes = "btn btn-primary btn-lg") { +"Get Started Free" }
                    a(href = "/showcase", classes = "btn btn-ghost btn-lg") { +"See It In Action →" }
                }
            }
        }

        // Features preview
        section("section") {
            div("section-inner") {
                div("section-header") {
                    h2 { +"Why Playout Edge?" }
                    p { +"Everything you need to power your digital signage network." }
                }
                div("feature-grid") {
                    featureCard(
                        "M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z",
                        "Channel Management",
                        "Organize content into channels and push updates to any screen instantly."
                    )
                    featureCard(
                        "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z",
                        "Smart Scheduling",
                        "Schedule content by time, date, or event. Automate your entire signage workflow."
                    )
                    featureCard(
                        "M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z",
                        "Device Fleet",
                        "Monitor and manage all your screens from a single dashboard."
                    )
                }
            }
        }

        // CTA section
        section("cta-section") {
            div("section-inner") {
                div("section-header") {
                    h2 { +"Ready to Get Started?" }
                    p { +"Create your account in seconds. No credit card required." }
                }
                a(href = "/signup", classes = "btn btn-primary btn-lg") { +"Sign Up Now" }
            }
        }
    }
}

private fun FlowContent.featureCard(icon: String, title: String, description: String) {
    div("feature-card") {
        div("feature-icon") {
            unsafe {
                +"""<svg fill="none" stroke="currentColor" viewBox="0 0 24 24" width="32" height="32"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="$icon"></path></svg>"""
            }
        }
        h3 { +title }
        p { +description }
    }
}
