package com.playoutedge.server.routes.landing

import com.playoutedge.server.views.landing.DEMO_CHANNELS
import com.playoutedge.server.views.landing.showcaseDemoView
import com.playoutedge.server.views.landing.showcaseView
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*

fun Route.showcaseRoutes() {
    get("/showcase") {
        call.respondHtml { showcaseView() }
    }
    get("/showcase/demo/{id}") {
        val id = call.parameters["id"] ?: ""
        call.respondHtml { showcaseDemoView(id) }
    }
}
