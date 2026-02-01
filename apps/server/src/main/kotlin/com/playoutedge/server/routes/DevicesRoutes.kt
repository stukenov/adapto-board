package com.playoutedge.server.routes

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.domain.enums.ActionType
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.tenantContext
import com.playoutedge.server.plugins.tenantId
import com.playoutedge.server.services.DeviceActionService
import com.playoutedge.server.services.DeviceService
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
data class DeviceResponse(
    val id: String,
    val displayName: String,
    val enrollStatus: String,
    val assignedChannelId: String?,
    val lastSeenAt: String?,
    val appVersion: String?,
    val androidModel: String?,
    val androidVersion: String?,
    val createdAt: String,
    val isOnline: Boolean
)

@Serializable
data class DevicesListResponse(
    val devices: List<DeviceResponse>,
    val total: Int,
    val onlineCount: Int
)

@Serializable
data class UpdateDeviceRequestDto(
    val displayName: String? = null
)

@Serializable
data class AssignChannelRequest(
    val channelId: String?
)

@Serializable
data class CreateActionRequest(
    val action: String,
    val params: JsonObject? = null
)

@Serializable
data class DeviceActionResponse(
    val id: String,
    val action: String,
    val status: String,
    val params: JsonObject?,
    val createdAt: String,
    val ackAt: String?,
    val expiresAt: String?
)

@Serializable
data class DeviceActionsListResponse(
    val actions: List<DeviceActionResponse>
)

fun Route.devicesRoutes(deviceService: DeviceService, deviceActionService: DeviceActionService) {
    authenticate(AUTH_ADMIN) {
        route("/api/admin/devices") {
            // GET /api/admin/devices - List all devices
            get {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val devices = deviceService.findAll(tenantId)
                val onlineCount = devices.count { deviceService.isOnline(it.lastSeenAt) }

                call.respond(DevicesListResponse(
                    devices = devices.map { device ->
                        DeviceResponse(
                            id = device.id.value.toString(),
                            displayName = device.displayName,
                            enrollStatus = device.enrollStatus.name,
                            assignedChannelId = device.assignedChannelId?.toString(),
                            lastSeenAt = device.lastSeenAt?.toString(),
                            appVersion = device.appVersion,
                            androidModel = device.androidModel,
                            androidVersion = device.androidVersion,
                            createdAt = device.createdAt.toString(),
                            isOnline = deviceService.isOnline(device.lastSeenAt)
                        )
                    },
                    total = devices.size,
                    onlineCount = onlineCount
                ))
            }

            // GET /api/admin/devices/{id} - Get device details
            get("/{id}") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val deviceId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid device ID")

                val device = deviceService.findById(tenantId, deviceId)
                    ?: throw ApiException.notFound(ErrorCode.DEVICE_NOT_FOUND, "Device not found")

                call.respond(DeviceResponse(
                    id = device.id.value.toString(),
                    displayName = device.displayName,
                    enrollStatus = device.enrollStatus.name,
                    assignedChannelId = device.assignedChannelId?.toString(),
                    lastSeenAt = device.lastSeenAt?.toString(),
                    appVersion = device.appVersion,
                    androidModel = device.androidModel,
                    androidVersion = device.androidVersion,
                    createdAt = device.createdAt.toString(),
                    isOnline = deviceService.isOnline(device.lastSeenAt)
                ))
            }

            // PATCH /api/admin/devices/{id} - Update device
            patch("/{id}") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val deviceId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid device ID")

                val request = call.receive<UpdateDeviceRequestDto>()

                val device = deviceService.update(tenantId, deviceId, displayName = request.displayName)
                    ?: throw ApiException.notFound(ErrorCode.DEVICE_NOT_FOUND, "Device not found")

                call.respond(DeviceResponse(
                    id = device.id.value.toString(),
                    displayName = device.displayName,
                    enrollStatus = device.enrollStatus.name,
                    assignedChannelId = device.assignedChannelId?.toString(),
                    lastSeenAt = device.lastSeenAt?.toString(),
                    appVersion = device.appVersion,
                    androidModel = device.androidModel,
                    androidVersion = device.androidVersion,
                    createdAt = device.createdAt.toString(),
                    isOnline = deviceService.isOnline(device.lastSeenAt)
                ))
            }

            // POST /api/admin/devices/{id}/assign-channel - Assign channel
            post("/{id}/assign-channel") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val deviceId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid device ID")

                val request = call.receive<AssignChannelRequest>()
                val channelId = request.channelId?.let { UUID.fromString(it) }

                val device = deviceService.assignChannel(tenantId, deviceId, channelId)
                    ?: throw ApiException.notFound(ErrorCode.DEVICE_NOT_FOUND, "Device not found")

                call.respond(DeviceResponse(
                    id = device.id.value.toString(),
                    displayName = device.displayName,
                    enrollStatus = device.enrollStatus.name,
                    assignedChannelId = device.assignedChannelId?.toString(),
                    lastSeenAt = device.lastSeenAt?.toString(),
                    appVersion = device.appVersion,
                    androidModel = device.androidModel,
                    androidVersion = device.androidVersion,
                    createdAt = device.createdAt.toString(),
                    isOnline = deviceService.isOnline(device.lastSeenAt)
                ))
            }

            // POST /api/admin/devices/{id}/actions - Create device action
            post("/{id}/actions") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val deviceId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid device ID")

                val userId = call.tenantContext?.userId

                val request = call.receive<CreateActionRequest>()
                val actionType = try {
                    ActionType.valueOf(request.action)
                } catch (e: IllegalArgumentException) {
                    throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid action type")
                }

                val action = deviceActionService.createAction(
                    tenantId = tenantId,
                    deviceId = deviceId,
                    actionType = actionType,
                    params = request.params,
                    createdBy = userId
                )

                call.respond(HttpStatusCode.Created, DeviceActionResponse(
                    id = action.id.value.toString(),
                    action = action.action.name,
                    status = action.status.name,
                    params = action.paramsJson,
                    createdAt = action.createdAt.toString(),
                    ackAt = action.ackAt?.toString(),
                    expiresAt = action.expiresAt?.toString()
                ))
            }

            // GET /api/admin/devices/{id}/actions - Get action history
            get("/{id}/actions") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val deviceId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid device ID")

                val actions = deviceActionService.getActionHistory(tenantId, deviceId)

                call.respond(DeviceActionsListResponse(
                    actions = actions.map { action ->
                        DeviceActionResponse(
                            id = action.id.value.toString(),
                            action = action.action.name,
                            status = action.status.name,
                            params = action.paramsJson,
                            createdAt = action.createdAt.toString(),
                            ackAt = action.ackAt?.toString(),
                            expiresAt = action.expiresAt?.toString()
                        )
                    }
                ))
            }
        }
    }
}
