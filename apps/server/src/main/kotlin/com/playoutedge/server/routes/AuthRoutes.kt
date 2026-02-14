package com.playoutedge.server.routes

import com.playoutedge.auth.AdminClaims
import com.playoutedge.auth.AuthConfig
import com.playoutedge.auth.JwtService
import com.playoutedge.auth.PasswordService
import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.persistence.entities.UserEntity
import com.playoutedge.persistence.repositories.RefreshTokenRepository
import com.playoutedge.persistence.tables.UserRoles
import com.playoutedge.persistence.tables.Users
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.adminClaims
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val accessToken: String, val expiresIn: Long)

@Serializable
data class UserInfoResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val tenantId: String
)

fun Route.authRoutes(
    jwtService: JwtService,
    passwordService: PasswordService,
    authConfig: AuthConfig,
    refreshTokenRepository: RefreshTokenRepository
) {
    route("/api/auth") {
        post("/login") {
            val request = call.receive<LoginRequest>()

            val (user, role) = transaction {
                val user = UserEntity.find {
                    Users.email eq request.email
                }.firstOrNull() ?: throw ApiException.Unauthorized(
                    code = ErrorCode.INVALID_CREDENTIALS,
                    message = "Invalid email or password"
                )

                val role = UserRoles.selectAll()
                    .where { UserRoles.userId eq user.id }
                    .map { it[UserRoles.role] }
                    .firstOrNull() ?: UserRole.OPERATOR

                Pair(user, role)
            }

            if (user.status != UserStatus.ACTIVE) {
                throw ApiException.Unauthorized(
                    code = ErrorCode.INVALID_CREDENTIALS,
                    message = "Account is not active"
                )
            }

            if (!passwordService.verify(request.password, user.passwordHash)) {
                throw ApiException.Unauthorized(
                    code = ErrorCode.INVALID_CREDENTIALS,
                    message = "Invalid email or password"
                )
            }

            val claims = AdminClaims(
                subject = user.id.value,
                tenantId = user.tenantId,
                role = role
            )

            val accessToken = jwtService.generateAdminAccessToken(claims)
            val refreshToken = jwtService.generateAdminRefreshToken()

            // Store refresh token hash
            val tokenHash = hashToken(refreshToken)
            refreshTokenRepository.store(
                tokenHash = tokenHash,
                subjectId = user.id.value,
                subjectType = "ADMIN",
                tenantId = user.tenantId,
                expiresAt = Clock.System.now().plus(authConfig.adminRefreshTokenTtl)
            )

            call.response.cookies.append(
                Cookie(
                    name = "refresh_token",
                    value = refreshToken,
                    httpOnly = true,
                    secure = true,
                    path = "/api/auth",
                    maxAge = authConfig.adminRefreshTokenTtl.inWholeSeconds.toInt()
                )
            )

            call.respond(LoginResponse(
                accessToken = accessToken,
                expiresIn = authConfig.adminAccessTokenTtl.inWholeSeconds
            ))
        }

        post("/refresh") {
            val refreshToken = call.request.cookies["refresh_token"]
                ?: throw ApiException.Unauthorized(
                    code = ErrorCode.TOKEN_INVALID,
                    message = "Refresh token not found"
                )

            val tokenHash = hashToken(refreshToken)
            val storedToken = refreshTokenRepository.findByHash(tokenHash)
                ?: throw ApiException.Unauthorized(
                    code = ErrorCode.TOKEN_EXPIRED,
                    message = "Refresh token is invalid or expired"
                )

            if (storedToken.expiresAt < Clock.System.now()) {
                refreshTokenRepository.revoke(tokenHash)
                throw ApiException.Unauthorized(
                    code = ErrorCode.TOKEN_EXPIRED,
                    message = "Refresh token has expired"
                )
            }

            // Look up user and generate new access token
            val (user, role) = transaction {
                val user = UserEntity.findById(storedToken.subjectId)
                    ?: throw ApiException.Unauthorized(
                        code = ErrorCode.TOKEN_INVALID,
                        message = "User not found"
                    )
                val role = UserRoles.selectAll()
                    .where { UserRoles.userId eq user.id }
                    .map { it[UserRoles.role] }
                    .firstOrNull() ?: UserRole.OPERATOR
                Pair(user, role)
            }

            val claims = AdminClaims(
                subject = user.id.value,
                tenantId = user.tenantId,
                role = role
            )

            val accessToken = jwtService.generateAdminAccessToken(claims)

            call.respond(LoginResponse(
                accessToken = accessToken,
                expiresIn = authConfig.adminAccessTokenTtl.inWholeSeconds
            ))
        }

        post("/logout") {
            val refreshToken = call.request.cookies["refresh_token"]
            if (refreshToken != null) {
                refreshTokenRepository.revoke(hashToken(refreshToken))
            }
            call.response.cookies.appendExpired("refresh_token", path = "/api/auth")
            call.respond(HttpStatusCode.NoContent)
        }

        authenticate(AUTH_ADMIN) {
            get("/me") {
                val claims = call.adminClaims
                    ?: throw ApiException.Unauthorized(
                        code = ErrorCode.TOKEN_INVALID,
                        message = "Invalid token"
                    )

                val user = transaction {
                    UserEntity.findById(claims.subject)
                } ?: throw ApiException.NotFound(
                    code = ErrorCode.NOT_FOUND,
                    message = "User not found"
                )

                call.respond(UserInfoResponse(
                    id = user.id.value.toString(),
                    email = user.email,
                    name = user.displayName,
                    role = claims.role.name,
                    tenantId = user.tenantId.toString()
                ))
            }
        }
    }
}

internal fun hashToken(token: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(token.toByteArray()).joinToString("") { "%02x".format(it) }
}
