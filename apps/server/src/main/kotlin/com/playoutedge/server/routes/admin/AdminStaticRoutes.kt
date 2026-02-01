package com.playoutedge.server.routes.admin

import io.ktor.server.http.content.*
import io.ktor.server.routing.*

/**
 * Serves static files for admin UI.
 */
fun Route.adminStaticRoutes() {
    staticResources("/admin/static", "static")
}
