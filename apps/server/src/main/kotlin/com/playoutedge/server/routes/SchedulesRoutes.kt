package com.playoutedge.server.routes

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.server.services.PublishResult
import com.playoutedge.server.services.RollbackResult
import com.playoutedge.server.services.ScheduleItemInput
import com.playoutedge.server.services.ScheduleService
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.adminClaims
import com.playoutedge.server.plugins.tenantId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ScheduleVersionResponse(
    val id: String,
    val channelId: String,
    val version: Int,
    val state: String,
    val publishedAt: String?,
    val createdAt: String
)

@Serializable
data class ScheduleVersionsListResponse(
    val versions: List<ScheduleVersionResponse>,
    val total: Int
)

@Serializable
data class ScheduleItemResponse(
    val id: String,
    val assetId: String,
    val orderIndex: Int,
    val validFrom: String?,
    val validTo: String?,
    val daysOfWeek: Int?,
    val timeStart: String?,
    val timeEnd: String?,
    val weight: Int
)

@Serializable
data class ScheduleVersionDetailResponse(
    val id: String,
    val channelId: String,
    val version: Int,
    val state: String,
    val publishedAt: String?,
    val createdAt: String,
    val items: List<ScheduleItemResponse>
)

@Serializable
data class ScheduleItemInputDto(
    val assetId: String,
    val orderIndex: Int,
    val validFrom: String? = null,
    val validTo: String? = null,
    val daysOfWeek: Int? = null,
    val timeStart: String? = null,
    val timeEnd: String? = null,
    val weight: Int = 1
)

@Serializable
data class ReplaceItemsRequest(
    val items: List<ScheduleItemInputDto>
)

@Serializable
data class RollbackRequest(
    val reason: String? = null
)

@Serializable
data class PublishResponse(
    val id: String,
    val version: Int,
    val state: String,
    val publishedAt: String
)

@Serializable
data class PublishErrorResponse(
    val error: String,
    val code: String,
    val assetIds: List<String>? = null
)

fun Route.schedulesRoutes(scheduleService: ScheduleService) {
    authenticate(AUTH_ADMIN) {
        // Routes under /api/admin/channels/{channelId}/schedules
        route("/api/admin/channels/{channelId}/schedules") {
            // POST /draft - Create draft
            post("/draft") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )
                val adminClaims = call.adminClaims

                val channelId = call.parameters["channelId"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid channel ID")

                val existingDraft = scheduleService.getDraftVersion(tenantId, channelId)
                if (existingDraft != null) {
                    throw ApiException.conflict(
                        ErrorCode.SCHEDULE_DRAFT_EXISTS,
                        "A draft already exists for this channel"
                    )
                }

                val version = scheduleService.createDraft(tenantId, channelId, adminClaims?.subject)

                call.respond(HttpStatusCode.Created, ScheduleVersionResponse(
                    id = version.id.value.toString(),
                    channelId = channelId.toString(),
                    version = version.version,
                    state = version.state.name,
                    publishedAt = version.publishedAt?.toString(),
                    createdAt = version.createdAt.toString()
                ))
            }

            // GET - List versions
            get {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val channelId = call.parameters["channelId"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid channel ID")

                val versions = scheduleService.findVersionsByChannel(tenantId, channelId)

                call.respond(ScheduleVersionsListResponse(
                    versions = versions.map { version ->
                        ScheduleVersionResponse(
                            id = version.id.value.toString(),
                            channelId = channelId.toString(),
                            version = version.version,
                            state = version.state.name,
                            publishedAt = version.publishedAt?.toString(),
                            createdAt = version.createdAt.toString()
                        )
                    },
                    total = versions.size
                ))
            }

            // POST /{version}/rollback - Rollback to version
            post("/{version}/rollback") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val channelId = call.parameters["channelId"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid channel ID")

                val toVersion = call.parameters["version"]?.toIntOrNull()
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid version number")

                when (val result = scheduleService.rollback(tenantId, channelId, toVersion)) {
                    is RollbackResult.Success -> {
                        call.respond(ScheduleVersionResponse(
                            id = result.version.id.value.toString(),
                            channelId = channelId.toString(),
                            version = result.version.version,
                            state = result.version.state.name,
                            publishedAt = result.version.publishedAt?.toString(),
                            createdAt = result.version.createdAt.toString()
                        ))
                    }
                    is RollbackResult.VersionNotFound -> {
                        throw ApiException.notFound(
                            ErrorCode.SCHEDULE_VERSION_NOT_FOUND,
                            "Version not found"
                        )
                    }
                    is RollbackResult.AssetsUnavailable -> {
                        throw ApiException.conflict(
                            ErrorCode.ROLLBACK_ASSETS_UNAVAILABLE,
                            "Some assets are no longer available"
                        )
                    }
                }
            }
        }

        // Routes under /api/admin/schedules/{versionId}
        route("/api/admin/schedules/{versionId}") {
            // GET - Get version with items
            get {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val versionId = call.parameters["versionId"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid version ID")

                val version = scheduleService.findVersionById(tenantId, versionId)
                    ?: throw ApiException.notFound(ErrorCode.SCHEDULE_VERSION_NOT_FOUND, "Version not found")

                val items = scheduleService.getItemsByVersion(tenantId, versionId)

                call.respond(ScheduleVersionDetailResponse(
                    id = version.id.value.toString(),
                    channelId = version.channel.id.value.toString(),
                    version = version.version,
                    state = version.state.name,
                    publishedAt = version.publishedAt?.toString(),
                    createdAt = version.createdAt.toString(),
                    items = items.map { item ->
                        ScheduleItemResponse(
                            id = item.id.value.toString(),
                            assetId = item.asset.id.value.toString(),
                            orderIndex = item.orderIndex,
                            validFrom = item.validFrom?.toString(),
                            validTo = item.validTo?.toString(),
                            daysOfWeek = item.daysOfWeek,
                            timeStart = item.timeStart?.toString(),
                            timeEnd = item.timeEnd?.toString(),
                            weight = item.weight
                        )
                    }
                ))
            }

            // PUT /items - Replace items
            put("/items") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val versionId = call.parameters["versionId"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid version ID")

                val request = call.receive<ReplaceItemsRequest>()

                val version = scheduleService.findVersionById(tenantId, versionId)
                    ?: throw ApiException.notFound(ErrorCode.SCHEDULE_VERSION_NOT_FOUND, "Version not found")

                if (version.state.name != "DRAFT") {
                    throw ApiException.conflict(
                        ErrorCode.SCHEDULE_VERSION_IMMUTABLE,
                        "Cannot modify published schedule version"
                    )
                }

                val items = scheduleService.replaceItems(
                    tenantId,
                    versionId,
                    request.items.map { dto ->
                        ScheduleItemInput(
                            assetId = UUID.fromString(dto.assetId),
                            orderIndex = dto.orderIndex,
                            validFrom = dto.validFrom?.let { LocalDate.parse(it) },
                            validTo = dto.validTo?.let { LocalDate.parse(it) },
                            daysOfWeek = dto.daysOfWeek,
                            timeStart = dto.timeStart?.let { LocalTime.parse(it) },
                            timeEnd = dto.timeEnd?.let { LocalTime.parse(it) },
                            weight = dto.weight
                        )
                    }
                )

                call.respond(items.map { item ->
                    ScheduleItemResponse(
                        id = item.id.value.toString(),
                        assetId = item.asset.id.value.toString(),
                        orderIndex = item.orderIndex,
                        validFrom = item.validFrom?.toString(),
                        validTo = item.validTo?.toString(),
                        daysOfWeek = item.daysOfWeek,
                        timeStart = item.timeStart?.toString(),
                        timeEnd = item.timeEnd?.toString(),
                        weight = item.weight
                    )
                })
            }

            // POST /publish - Publish schedule
            post("/publish") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val versionId = call.parameters["versionId"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid version ID")

                when (val result = scheduleService.publish(tenantId, versionId)) {
                    is PublishResult.Success -> {
                        call.respond(PublishResponse(
                            id = result.version.id.value.toString(),
                            version = result.version.version,
                            state = result.version.state.name,
                            publishedAt = result.version.publishedAt.toString()
                        ))
                    }
                    is PublishResult.VersionNotFound -> {
                        throw ApiException.notFound(
                            ErrorCode.SCHEDULE_VERSION_NOT_FOUND,
                            "Version not found"
                        )
                    }
                    is PublishResult.VersionNotDraft -> {
                        throw ApiException.conflict(
                            ErrorCode.SCHEDULE_VERSION_IMMUTABLE,
                            "Only draft versions can be published"
                        )
                    }
                    is PublishResult.ScheduleEmpty -> {
                        throw ApiException.badRequest(
                            ErrorCode.SCHEDULE_EMPTY,
                            "Cannot publish empty schedule"
                        )
                    }
                    is PublishResult.AssetsNotReady -> {
                        throw ApiException.conflict(
                            ErrorCode.ASSET_NOT_READY,
                            "Some assets are not ready"
                        )
                    }
                }
            }
        }
    }
}
