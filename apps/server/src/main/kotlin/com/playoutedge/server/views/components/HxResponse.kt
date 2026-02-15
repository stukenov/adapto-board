package com.playoutedge.server.views.components

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.html.*
import kotlinx.html.stream.appendHTML

/** Check if this is an HTMX request */
val ApplicationCall.isHtmx: Boolean
    get() = request.header("HX-Request") == "true"

/** Respond with HTML fragment (no html/head/body wrapper) */
suspend fun ApplicationCall.respondHxFragment(
    status: HttpStatusCode = HttpStatusCode.OK,
    block: FlowContent.() -> Unit
) {
    val html = buildString {
        appendHTML(prettyPrint = false).body {
            block()
        }
    }
    // Strip the <body>...</body> wrapper to get inner content
    val inner = html.substringAfter("<body>").substringBeforeLast("</body>")
    respondText(inner, ContentType.Text.Html, status)
}

/** Respond with 401 if no session (for HTMX fragment routes) */
suspend fun ApplicationCall.respondUnauthorized() {
    respondText("", ContentType.Text.Html, HttpStatusCode.Unauthorized)
}
