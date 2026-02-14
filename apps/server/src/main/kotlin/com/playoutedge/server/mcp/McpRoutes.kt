package com.playoutedge.server.mcp

import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.adminClaims
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Route.mcpRoutes(mcpServer: McpServer) {
    authenticate(AUTH_ADMIN) {
        post("/api/mcp") {
            val claims = call.adminClaims
            if (claims == null) {
                call.respondText(
                    Json.encodeToString(
                        JsonRpcResponse.serializer(),
                        JsonRpcResponse(error = JsonRpcError(McpErrorCodes.INTERNAL_ERROR, "Unauthorized"))
                    ),
                    ContentType.Application.Json,
                    HttpStatusCode.Unauthorized
                )
                return@post
            }

            val context = McpToolContext(
                tenantId = claims.tenantId,
                userId = claims.subject
            )

            val bodyText = call.receiveText()
            val request = try {
                Json.decodeFromString(JsonRpcRequest.serializer(), bodyText)
            } catch (e: Exception) {
                val errorResponse = JsonRpcResponse(
                    error = JsonRpcError(McpErrorCodes.PARSE_ERROR, "Invalid JSON: ${e.message}")
                )
                call.respondText(
                    Json.encodeToString(JsonRpcResponse.serializer(), errorResponse),
                    ContentType.Application.Json
                )
                return@post
            }

            val response = mcpServer.handleRequest(request, context)
            if (response != null) {
                call.respondText(
                    Json.encodeToString(JsonRpcResponse.serializer(), response),
                    ContentType.Application.Json
                )
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
