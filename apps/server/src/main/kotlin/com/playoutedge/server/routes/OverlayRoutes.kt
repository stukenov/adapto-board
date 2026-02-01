package com.playoutedge.server.routes

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.tenantId
import com.playoutedge.server.services.OverlayService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.UUID

@Serializable
data class OverlayProfileResponse(
    val id: String,
    val name: String,
    val definition: JsonObject,
    val createdAt: String
)

@Serializable
data class OverlayProfilesListResponse(
    val profiles: List<OverlayProfileResponse>,
    val total: Int
)

@Serializable
data class CreateProfileRequest(
    val name: String,
    val definition: JsonObject
)

@Serializable
data class OverlayStateResponse(
    val channelId: String,
    val state: JsonObject,
    val version: Long,
    val updatedAt: String
)

@Serializable
data class SetStateRequest(
    val state: JsonObject
)

@Serializable
data class PatchStateRequest(
    val upsert: List<JsonObject>? = null,
    val remove: List<String>? = null
)

fun Route.overlayRoutes(overlayService: OverlayService) {
    authenticate(AUTH_ADMIN) {
        route("/api/admin/overlay") {
            // Profiles
            route("/profiles") {
                // GET /api/admin/overlay/profiles - List profiles
                get {
                    val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                        ErrorCode.TOKEN_INVALID,
                        "Missing tenant context"
                    )

                    val profiles = overlayService.findAllProfiles(tenantId)

                    call.respond(OverlayProfilesListResponse(
                        profiles = profiles.map { profile ->
                            OverlayProfileResponse(
                                id = profile.id.value.toString(),
                                name = profile.name,
                                definition = profile.definitionJson,
                                createdAt = profile.createdAt.toString()
                            )
                        },
                        total = profiles.size
                    ))
                }

                // POST /api/admin/overlay/profiles - Create profile
                post {
                    val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                        ErrorCode.TOKEN_INVALID,
                        "Missing tenant context"
                    )

                    val request = call.receive<CreateProfileRequest>()

                    val profile = overlayService.createProfile(tenantId, request.name, request.definition)

                    call.respond(HttpStatusCode.Created, OverlayProfileResponse(
                        id = profile.id.value.toString(),
                        name = profile.name,
                        definition = profile.definitionJson,
                        createdAt = profile.createdAt.toString()
                    ))
                }

                // GET /api/admin/overlay/profiles/{id}
                get("/{id}") {
                    val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                        ErrorCode.TOKEN_INVALID,
                        "Missing tenant context"
                    )

                    val profileId = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid profile ID")

                    val profile = overlayService.findProfileById(tenantId, profileId)
                        ?: throw ApiException.notFound(ErrorCode.NOT_FOUND, "Profile not found")

                    call.respond(OverlayProfileResponse(
                        id = profile.id.value.toString(),
                        name = profile.name,
                        definition = profile.definitionJson,
                        createdAt = profile.createdAt.toString()
                    ))
                }
            }

            // State
            route("/state/{channelId}") {
                // GET /api/admin/overlay/state/{channelId}
                get {
                    val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                        ErrorCode.TOKEN_INVALID,
                        "Missing tenant context"
                    )

                    val channelId = call.parameters["channelId"]?.let { UUID.fromString(it) }
                        ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid channel ID")

                    val state = overlayService.getState(tenantId, channelId)
                        ?: throw ApiException.notFound(ErrorCode.NOT_FOUND, "No state for channel")

                    call.respond(OverlayStateResponse(
                        channelId = channelId.toString(),
                        state = state.stateJson,
                        version = state.version,
                        updatedAt = state.updatedAt.toString()
                    ))
                }

                // PUT /api/admin/overlay/state/{channelId} - Replace state
                put {
                    val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                        ErrorCode.TOKEN_INVALID,
                        "Missing tenant context"
                    )

                    val channelId = call.parameters["channelId"]?.let { UUID.fromString(it) }
                        ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid channel ID")

                    val request = call.receive<SetStateRequest>()

                    try {
                        val state = overlayService.setState(tenantId, channelId, request.state)

                        call.respond(OverlayStateResponse(
                            channelId = channelId.toString(),
                            state = state.stateJson,
                            version = state.version,
                            updatedAt = state.updatedAt.toString()
                        ))
                    } catch (e: IllegalArgumentException) {
                        throw ApiException.badRequest(ErrorCode.OVERLAY_STATE_TOO_LARGE, e.message ?: "State too large")
                    }
                }

                // PATCH /api/admin/overlay/state/{channelId} - Patch state
                patch {
                    val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                        ErrorCode.TOKEN_INVALID,
                        "Missing tenant context"
                    )

                    val channelId = call.parameters["channelId"]?.let { UUID.fromString(it) }
                        ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid channel ID")

                    val request = call.receive<PatchStateRequest>()
                    val patch = kotlinx.serialization.json.buildJsonObject {
                        request.upsert?.let { list ->
                            put("upsert", kotlinx.serialization.json.JsonArray(list))
                        }
                        request.remove?.let { list ->
                            put("remove", kotlinx.serialization.json.JsonArray(list.map { kotlinx.serialization.json.JsonPrimitive(it) }))
                        }
                    }

                    try {
                        val state = overlayService.patchState(tenantId, channelId, patch)
                            ?: throw ApiException.notFound(ErrorCode.NOT_FOUND, "No state for channel")

                        call.respond(OverlayStateResponse(
                            channelId = channelId.toString(),
                            state = state.stateJson,
                            version = state.version,
                            updatedAt = state.updatedAt.toString()
                        ))
                    } catch (e: IllegalArgumentException) {
                        throw ApiException.badRequest(ErrorCode.OVERLAY_STATE_TOO_LARGE, e.message ?: "State too large")
                    }
                }
            }
        }
    }
}
