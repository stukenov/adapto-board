package com.playoutedge.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.playoutedge.domain.enums.UserRole
import java.security.SecureRandom
import java.util.*
import kotlin.time.Duration

class JwtService(private val config: AuthConfig) {
    private val algorithm = Algorithm.HMAC256(config.jwtSecret)
    private val verifier = JWT.require(algorithm)
        .withIssuer(config.jwtIssuer)
        .withAudience(config.jwtAudience)
        .build()
    private val random = SecureRandom()

    fun generateAdminAccessToken(claims: AdminClaims): String {
        return generateToken(
            subject = claims.subject.toString(),
            tenantId = claims.tenantId.toString(),
            type = TokenType.ADMIN.name,
            ttl = config.adminAccessTokenTtl,
            extraClaims = mapOf("role" to claims.role.name)
        )
    }

    fun generateDeviceAccessToken(claims: DeviceClaims): String {
        val extra = mutableMapOf<String, String>("type" to TokenType.DEVICE.name)
        claims.channelId?.let { extra["cid"] = it.toString() }
        return generateToken(
            subject = claims.subject.toString(),
            tenantId = claims.tenantId.toString(),
            type = TokenType.DEVICE.name,
            ttl = config.deviceAccessTokenTtl,
            extraClaims = extra
        )
    }

    fun generateAdminRefreshToken(): String {
        return generateOpaqueToken()
    }

    fun generateDeviceRefreshToken(): String {
        return generateOpaqueToken()
    }

    fun validateToken(token: String): TokenClaims? {
        return try {
            val decoded = verifier.verify(token)
            val type = decoded.getClaim("type").asString()
            val subject = UUID.fromString(decoded.subject)
            val tenantId = UUID.fromString(decoded.getClaim("tid").asString())

            when (type) {
                TokenType.ADMIN.name -> {
                    val role = UserRole.valueOf(decoded.getClaim("role").asString())
                    AdminClaims(subject, tenantId, role)
                }
                TokenType.DEVICE.name -> {
                    val channelId = decoded.getClaim("cid").asString()?.let { UUID.fromString(it) }
                    DeviceClaims(subject, tenantId, channelId)
                }
                else -> null
            }
        } catch (e: JWTVerificationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun generateToken(
        subject: String,
        tenantId: String,
        type: String,
        ttl: Duration,
        extraClaims: Map<String, String> = emptyMap()
    ): String {
        val now = Date()
        val expiry = Date(now.time + ttl.inWholeMilliseconds)

        val builder = JWT.create()
            .withIssuer(config.jwtIssuer)
            .withAudience(config.jwtAudience)
            .withSubject(subject)
            .withClaim("tid", tenantId)
            .withClaim("type", type)
            .withIssuedAt(now)
            .withExpiresAt(expiry)

        extraClaims.forEach { (key, value) ->
            builder.withClaim(key, value)
        }

        return builder.sign(algorithm)
    }

    private fun generateOpaqueToken(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
