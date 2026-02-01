package com.playoutedge.server.routes

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.server.services.ChannelService
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.tenantId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CreateChannelRequest(
    val name: String
)

@Serializable
data class UpdateChannelRequest(
    val name: String? = null,
    val status: String? = null,
    val defaultOverlayProfileId: String? = null
)

@Serializable
data class ChannelResponse(
    val id: String,
    val name: String,
    val status: String,
    val defaultOverlayProfileId: String?,
    val createdAt: String
)

@Serializable
data class ChannelsListResponse(
    val channels: List<ChannelResponse>,
    val total: Int
)

fun Route.channelsRoutes(channelService: ChannelService) {
    authenticate(AUTH_ADMIN) {
        route("/api/admin/channels") {
            // POST /api/admin/channels - Create channel
            post {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val request = call.receive<CreateChannelRequest>()

                val channel = channelService.create(tenantId, request.name)

                call.respond(HttpStatusCode.Created, ChannelResponse(
                    id = channel.id.value.toString(),
                    name = channel.name,
                    status = channel.status.name,
                    defaultOverlayProfileId = channel.defaultOverlayProfileId?.toString(),
                    createdAt = channel.createdAt.toString()
                ))
            }

            // GET /api/admin/channels - List channels
            get {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val channels = channelService.findAll(tenantId)

                call.respond(ChannelsListResponse(
                    channels = channels.map { channel ->
                        ChannelResponse(
                            id = channel.id.value.toString(),
                            name = channel.name,
                            status = channel.status.name,
                            defaultOverlayProfileId = channel.defaultOverlayProfileId?.toString(),
                            createdAt = channel.createdAt.toString()
                        )
                    },
                    total = channels.size
                ))
            }

            // GET /api/admin/channels/{id} - Get single channel
            get("/{id}") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val channelId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid channel ID")

                val channel = channelService.findById(tenantId, channelId)
                    ?: throw ApiException.notFound(ErrorCode.CHANNEL_NOT_FOUND, "Channel not found")

                call.respond(ChannelResponse(
                    id = channel.id.value.toString(),
                    name = channel.name,
                    status = channel.status.name,
                    defaultOverlayProfileId = channel.defaultOverlayProfileId?.toString(),
                    createdAt = channel.createdAt.toString()
                ))
            }

            // PATCH /api/admin/channels/{id} - Update channel
            patch("/{id}") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val channelId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid channel ID")

                val request = call.receive<UpdateChannelRequest>()

                val channel = channelService.update(
                    tenantId,
                    channelId,
                    name = request.name,
                    status = request.status?.let { ChannelStatus.valueOf(it) },
                    defaultOverlayProfileId = request.defaultOverlayProfileId?.let { UUID.fromString(it) }
                ) ?: throw ApiException.notFound(ErrorCode.CHANNEL_NOT_FOUND, "Channel not found")

                call.respond(ChannelResponse(
                    id = channel.id.value.toString(),
                    name = channel.name,
                    status = channel.status.name,
                    defaultOverlayProfileId = channel.defaultOverlayProfileId?.toString(),
                    createdAt = channel.createdAt.toString()
                ))
            }

            // DELETE /api/admin/channels/{id} - Archive channel
            delete("/{id}") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val channelId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid channel ID")

                val archived = channelService.archive(tenantId, channelId)
                if (!archived) {
                    throw ApiException.notFound(ErrorCode.CHANNEL_NOT_FOUND, "Channel not found")
                }

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
