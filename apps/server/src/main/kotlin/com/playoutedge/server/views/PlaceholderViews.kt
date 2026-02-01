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

/**
 * Overlay list view placeholder.
 */
fun HTML.overlayListView(session: AdminClaims) {
    placeholderView(
        session = session,
        title = "Overlay Profiles",
        description = "Overlay profile management and data bindings will be available here."
    )
}

/**
 * Reports view placeholder.
 */
fun HTML.reportsView(session: AdminClaims) {
    placeholderView(
        session = session,
        title = "Reports",
        description = "As-run reports and analytics will be available here."
    )
}

/**
 * Settings view placeholder.
 */
fun HTML.settingsView(session: AdminClaims) {
    placeholderView(
        session = session,
        title = "Settings",
        description = "Tenant settings and user management will be available here."
    )
}

/**
 * Onboarding wizard view placeholder.
 */
fun HTML.onboardingView(session: AdminClaims) {
    placeholderView(
        session = session,
        title = "Getting Started",
        description = "The onboarding wizard will guide you through setting up your first channel."
    )
}
