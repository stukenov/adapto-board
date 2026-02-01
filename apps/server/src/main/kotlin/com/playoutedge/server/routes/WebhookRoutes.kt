package com.playoutedge.server.routes

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.tenantId
import com.playoutedge.server.services.WebhookResult
import com.playoutedge.server.services.WebhookService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class WebhookLogResponse(
    val id: String,
    val statusCode: Int,
    val latencyMs: Int,
    val payloadSize: Int,
    val error: String?,
    val createdAt: String
)

@Serializable
data class WebhookLogsListResponse(
    val logs: List<WebhookLogResponse>
)

@Serializable
data class RegenerateSecretResponse(
    val secret: String,
    val webhookUrl: String
)

fun Route.webhookRoutes(webhookService: WebhookService) {
    // Public webhook endpoint - no auth required
    route("/api/webhook") {
        post("/{bindingId}") {
            val bindingId = call.parameters["bindingId"]?.let {
                try {
                    UUID.fromString(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            } ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid binding ID"))
                return@post
            }

            val signature = call.request.header("X-Signature-256")
            val payload = call.receiveChannel().toByteArray()

            when (val result = webhookService.processWebhook(bindingId, signature, payload)) {
                is WebhookResult.Success -> {
                    call.respond(HttpStatusCode.OK, mapOf("status" to "accepted"))
                }
                is WebhookResult.InvalidSignature -> {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid signature"))
                }
                is WebhookResult.InvalidPayload -> {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON payload"))
                }
                is WebhookResult.BindingNotFound -> {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Binding not found"))
                }
                is WebhookResult.PayloadTooLarge -> {
                    call.respond(HttpStatusCode.PayloadTooLarge, mapOf("error" to "Payload too large"))
                }
                is WebhookResult.InternalError -> {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to result.message))
                }
            }
        }
    }

    // Admin endpoints for webhook management
    authenticate(AUTH_ADMIN) {
        route("/api/admin/overlays/bindings/{id}/webhook") {
            // POST /api/admin/overlays/bindings/{id}/webhook/regenerate
            post("/regenerate") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val bindingId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid binding ID")

                val newSecret = webhookService.regenerateSecret(bindingId)
                    ?: throw ApiException.notFound(ErrorCode.NOT_FOUND, "Binding not found")

                val host = call.request.host()
                val scheme = if (call.request.local.scheme == "https") "https" else "http"
                val webhookUrl = "$scheme://$host/api/webhook/$bindingId"

                call.respond(RegenerateSecretResponse(
                    secret = newSecret,
                    webhookUrl = webhookUrl
                ))
            }

            // GET /api/admin/overlays/bindings/{id}/webhook/logs
            get("/logs") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val bindingId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid binding ID")

                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100

                val logs = webhookService.getLogs(bindingId, limit.coerceIn(1, 100))

                call.respond(WebhookLogsListResponse(
                    logs = logs.map { log ->
                        WebhookLogResponse(
                            id = log.id.value.toString(),
                            statusCode = log.statusCode,
                            latencyMs = log.latencyMs,
                            payloadSize = log.payloadSize,
                            error = log.error,
                            createdAt = log.createdAt.toString()
                        )
                    }
                ))
            }
        }
    }
}

private suspend fun io.ktor.utils.io.ByteReadChannel.toByteArray(): ByteArray {
    val buffer = java.io.ByteArrayOutputStream()
    val chunk = java.nio.ByteBuffer.allocate(8192)
    while (!isClosedForRead) {
        chunk.clear()
        val read = readAvailable(chunk)
        if (read > 0) {
            buffer.write(chunk.array(), 0, read)
        }
    }
    return buffer.toByteArray()
}
