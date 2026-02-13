package com.playoutedge.server.routes.landing

import com.playoutedge.server.views.landing.contactView
import com.playoutedge.server.views.landing.faqView
import com.playoutedge.server.views.landing.featuresView
import com.playoutedge.server.views.landing.landingView
import com.playoutedge.server.views.landing.pricingView
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*

fun Route.publicLandingRoutes() {
    get("/") {
        call.respondHtml { landingView() }
    }
    get("/features") {
        call.respondHtml { featuresView() }
    }
    get("/pricing") {
        call.respondHtml { pricingView() }
    }
    get("/faq") {
        call.respondHtml { faqView() }
    }
    get("/contact") {
        call.respondHtml { contactView() }
    }
}
