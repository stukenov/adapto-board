package com.playoutedge.server.routes

import com.playoutedge.persistence.repositories.OverlayRepository
import com.playoutedge.server.services.OverlayEvent
import com.playoutedge.server.services.OverlaySubscribers
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

@Serializable
private data class VoteRequest(val optionIndex: Int)

fun Route.publicPollRoutes(
    overlayRepository: OverlayRepository,
    overlaySubscribers: OverlaySubscribers
) {
    route("/api/public/poll") {
        post("/{channelId}/vote") {
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }
            if (channelId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid channel ID"))
                return@post
            }

            val body = call.receiveText()
            val vote = try {
                Json.decodeFromString<VoteRequest>(body)
            } catch (_: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request"))
                return@post
            }

            // Get current overlay state
            val stateData = newSuspendedTransaction {
                val state = overlayRepository.getStateByChannel(channelId) ?: return@newSuspendedTransaction null
                Pair(state.stateJson.toString(), state.tenant.id.value)
            }
            if (stateData == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "No active poll"))
                return@post
            }

            val (stateJsonStr, tenantIdValue) = stateData

            // Update poll votes in state JSON
            try {
                val stateObj = Json.parseToJsonElement(stateJsonStr).jsonObject.toMutableMap()
                val poll = stateObj["poll"]?.jsonObject?.toMutableMap() ?: return@post run {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "No active poll"))
                }

                val votes = poll["votes"]?.jsonArray?.mapIndexed { index, el ->
                    if (index == vote.optionIndex) {
                        JsonPrimitive(el.jsonPrimitive.int + 1)
                    } else el
                } ?: return@post run {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "No poll votes"))
                }

                poll["votes"] = JsonArray(votes)
                stateObj["poll"] = JsonObject(poll)

                val updatedStateObj = JsonObject(stateObj)
                val updatedState = Json.encodeToString(updatedStateObj)
                overlayRepository.setState(
                    com.playoutedge.domain.tenant.TenantId(tenantIdValue),
                    channelId,
                    updatedState
                )

                overlaySubscribers.broadcast(channelId, OverlayEvent.State(updatedStateObj, System.currentTimeMillis()))

                call.respondText(
                    Json.encodeToString(JsonObject(mapOf("success" to JsonPrimitive(true)))),
                    contentType = ContentType.Application.Json
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to process vote"))
            }
        }
    }
}
