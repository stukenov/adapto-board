package com.playoutedge.server.plugins

import com.playoutedge.auth.AdminClaims
import com.playoutedge.auth.AuthConfig
import com.playoutedge.auth.DeviceClaims
import com.playoutedge.auth.TokenClaims
import com.playoutedge.auth.TokenType
import com.playoutedge.domain.enums.UserRole
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.*
import java.util.*

const val AUTH_ADMIN = "admin-jwt"
const val AUTH_DEVICE = "device-jwt"

private val AdminClaimsKey = AttributeKey<AdminClaims>("AdminClaims")
private val DeviceClaimsKey = AttributeKey<DeviceClaims>("DeviceClaims")

fun Application.configureJwtAuth(config: AuthConfig) {
    install(Authentication) {
        jwt(AUTH_ADMIN) {
            realm = "playout-edge-admin"
            verifier(
                com.auth0.jwt.JWT.require(com.auth0.jwt.algorithms.Algorithm.HMAC256(config.jwtSecret))
                    .withIssuer(config.jwtIssuer)
                    .withAudience(config.jwtAudience)
                    .withClaim("type", TokenType.ADMIN.name)
                    .build()
            )
            validate { credential ->
                try {
                    val payload = credential.payload
                    val subject = UUID.fromString(payload.subject)
                    val tenantId = UUID.fromString(payload.getClaim("tid").asString())
                    val role = UserRole.valueOf(payload.getClaim("role").asString())

                    val claims = AdminClaims(subject, tenantId, role)
                    attributes.put(AdminClaimsKey, claims)
                    JWTPrincipal(payload)
                } catch (e: Exception) {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Invalid or expired admin token")
            }
        }

        jwt(AUTH_DEVICE) {
            realm = "playout-edge-device"
            verifier(
                com.auth0.jwt.JWT.require(com.auth0.jwt.algorithms.Algorithm.HMAC256(config.jwtSecret))
                    .withIssuer(config.jwtIssuer)
                    .withAudience(config.jwtAudience)
                    .withClaim("type", TokenType.DEVICE.name)
                    .build()
            )
            validate { credential ->
                try {
                    val payload = credential.payload
                    val subject = UUID.fromString(payload.subject)
                    val tenantId = UUID.fromString(payload.getClaim("tid").asString())
                    val channelId = payload.getClaim("cid").asString()?.let { UUID.fromString(it) }

                    val claims = DeviceClaims(subject, tenantId, channelId)
                    attributes.put(DeviceClaimsKey, claims)
                    JWTPrincipal(payload)
                } catch (e: Exception) {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Invalid or expired device token")
            }
        }
    }
}

val ApplicationCall.adminClaims: AdminClaims?
    get() = attributes.getOrNull(AdminClaimsKey)

val ApplicationCall.deviceClaims: DeviceClaims?
    get() = attributes.getOrNull(DeviceClaimsKey)

val ApplicationCall.tokenClaims: TokenClaims?
    get() = adminClaims ?: deviceClaims
