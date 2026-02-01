package com.playoutedge.server.routes.admin

import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Placeholder routes for features under construction.
 */
fun Route.adminPlaceholderRoutes() {
    // Overlay routes
    route("/admin/overlay") {
        get {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }
            call.respondHtml {
                overlayListView(session)
            }
        }
    }

    // Reports routes
    route("/admin/reports") {
        get {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }
            call.respondHtml {
                reportsView(session)
            }
        }
    }

    // Settings routes
    route("/admin/settings") {
        get {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }
            call.respondHtml {
                settingsView(session)
            }
        }
    }

    // Onboarding routes
    route("/admin/onboarding") {
        get {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }
            call.respondHtml {
                onboardingView(session)
            }
        }
    }
}
