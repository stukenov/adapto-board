package com.playoutedge.server.routes

import com.playoutedge.auth.AuthConfig
import com.playoutedge.auth.DeviceClaims
import com.playoutedge.auth.JwtService
import com.playoutedge.auth.Permission
import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.domain.enums.DeviceEnrollStatus
import com.playoutedge.domain.enums.EnrollCodeStatus
import com.playoutedge.persistence.entities.DeviceEntity
import com.playoutedge.persistence.entities.EnrollCodeEntity
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.UserEntity
import com.playoutedge.persistence.tables.Devices
import com.playoutedge.persistence.tables.EnrollCodes
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.adminClaims
import com.playoutedge.server.plugins.requirePermission
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.SecureRandom
import java.util.*

@Serializable
data class EnrollCodeResponse(
    val code: String,
    val expiresAt: String
)

@Serializable
data class DeviceEnrollRequest(
    val code: String,
    val deviceName: String,
    val deviceModel: String?,
    val osVersion: String?,
    val appVersion: String?
)

@Serializable
data class DeviceEnrollResponse(
    val deviceId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

@Serializable
data class DeviceRefreshRequest(
    val refreshToken: String
)

@Serializable
data class DeviceRefreshResponse(
    val accessToken: String,
    val expiresIn: Long
)

private val random = SecureRandom()
private val codeChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

fun generateEnrollCode(): String {
    return (1..6).map { codeChars[random.nextInt(codeChars.length)] }.joinToString("")
}

fun Route.deviceAuthRoutes(
    jwtService: JwtService,
    authConfig: AuthConfig
) {
    authenticate(AUTH_ADMIN) {
        route("/api/admin/enroll-codes") {
            requirePermission(Permission.DEVICES_ENROLL)

            post {
                val claims = call.adminClaims!!
                val code = generateEnrollCode()
                val expiresAt = Clock.System.now().plus(authConfig.enrollCodeTtl)

                transaction {
                    EnrollCodeEntity.new {
                        this.tenant = TenantEntity[claims.tenantId]
                        this.code = code
                        this.status = EnrollCodeStatus.PENDING
                        this.expiresAt = expiresAt
                        this.createdBy = UserEntity[claims.subject]
                        this.createdAt = Clock.System.now()
                    }
                }

                call.respond(HttpStatusCode.Created, EnrollCodeResponse(
                    code = code,
                    expiresAt = expiresAt.toString()
                ))
            }
        }

        route("/api/admin/devices/{id}/revoke") {
            requirePermission(Permission.DEVICES_REVOKE)

            post {
                val claims = call.adminClaims!!
                val deviceId = UUID.fromString(call.parameters["id"])

                transaction {
                    val device = DeviceEntity.find {
                        (Devices.id eq deviceId) and (Devices.tenantId eq claims.tenantId)
                    }.firstOrNull() ?: throw ApiException.NotFound(
                        code = ErrorCode.DEVICE_NOT_FOUND,
                        message = "Device not found"
                    )

                    device.enrollStatus = DeviceEnrollStatus.REJECTED
                    device.revokedAt = Clock.System.now()
                }

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }

    route("/api/device") {
        post("/enroll") {
            val request = call.receive<DeviceEnrollRequest>()

            val result = transaction {
                val enrollCode = EnrollCodeEntity.find {
                    EnrollCodes.code eq request.code
                }.firstOrNull() ?: throw ApiException.Unauthorized(
                    code = ErrorCode.DEVICE_NOT_ENROLLED,
                    message = "Invalid enroll code"
                )

                if (enrollCode.status == EnrollCodeStatus.USED) {
                    throw ApiException.Unauthorized(
                        code = ErrorCode.ENROLL_CODE_USED,
                        message = "Enroll code has already been used"
                    )
                }

                if (enrollCode.expiresAt < Clock.System.now()) {
                    throw ApiException.Unauthorized(
                        code = ErrorCode.ENROLL_CODE_EXPIRED,
                        message = "Enroll code has expired"
                    )
                }

                val device = DeviceEntity.new {
                    tenant = enrollCode.tenant
                    displayName = request.deviceName
                    enrollStatus = DeviceEnrollStatus.ENROLLED
                    androidModel = request.deviceModel
                    androidVersion = request.osVersion
                    appVersion = request.appVersion
                    lastSeenAt = Clock.System.now()
                    assignedChannelId = enrollCode.channel?.id?.value
                    createdAt = Clock.System.now()
                }

                enrollCode.status = EnrollCodeStatus.USED
                enrollCode.usedAt = Clock.System.now()
                enrollCode.usedByDevice = device

                Triple(device.id.value, enrollCode.tenant.id.value, enrollCode.channel?.id?.value)
            }

            val (deviceId, tenantId, channelId) = result

            val claims = DeviceClaims(
                subject = deviceId,
                tenantId = tenantId,
                channelId = channelId
            )

            val accessToken = jwtService.generateDeviceAccessToken(claims)
            val refreshToken = jwtService.generateDeviceRefreshToken()

            call.respond(HttpStatusCode.Created, DeviceEnrollResponse(
                deviceId = deviceId.toString(),
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = authConfig.deviceAccessTokenTtl.inWholeSeconds
            ))
        }

        post("/refresh") {
            val request = call.receive<DeviceRefreshRequest>()

            throw ApiException.Unauthorized(
                code = ErrorCode.TOKEN_EXPIRED,
                message = "Device token refresh requires implementation of refresh token storage"
            )
        }
    }
}
