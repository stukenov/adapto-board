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
        appendHTML(prettyPrint = false).div {
            block()
        }
    }
    // Remove the outer <div> wrapper, return just inner content
    val inner = html.removePrefix("<div>").removeSuffix("</div>")
    respondText(inner, ContentType.Text.Html, status)
}
