package com.playoutedge.server.plugins

import com.auth0.jwt.JWT
import com.playoutedge.auth.AdminClaims
import com.playoutedge.auth.AuthConfig
import com.playoutedge.auth.JwtService
import com.playoutedge.server.routes.admin.ADMIN_SESSION_COOKIE
import com.playoutedge.server.routes.admin.ADMIN_SESSION_MAX_AGE
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.*

/**
 * Attribute key for storing admin session in call.
 */
val AdminSessionKey = AttributeKey<AdminClaims>("AdminSession")

/**
 * Plugin configuration.
 */
class AdminSessionPluginConfig {
    var jwtService: JwtService? = null
    var authConfig: AuthConfig? = null
}

/**
 * Plugin that validates admin session cookies and protects admin routes.
 * Implements sliding session: auto-refreshes token when < 50% TTL remaining.
 */
val AdminSessionPlugin = createApplicationPlugin(name = "AdminSessionPlugin", ::AdminSessionPluginConfig) {
    val jwtService = pluginConfig.jwtService ?: error("JwtService not configured")
    val authConfig = pluginConfig.authConfig
    val ttlMs = authConfig?.adminAccessTokenTtl?.inWholeMilliseconds

    onCall { call ->
        val path = call.request.local.uri

        // Only check admin routes (except login, logout, static)
        if (!path.startsWith("/admin")) return@onCall
        if (path.startsWith("/admin/login")) return@onCall
        if (path.startsWith("/admin/logout")) return@onCall
        if (path.startsWith("/admin/forgot-password")) return@onCall
        if (path.startsWith("/admin/reset-password")) return@onCall
        if (path.startsWith("/admin/signup")) return@onCall
        if (path.startsWith("/admin/suspended")) return@onCall
        if (path.startsWith("/admin/static")) return@onCall

        val sessionCookie = call.request.cookies[ADMIN_SESSION_COOKIE]

        if (sessionCookie == null) {
            val next = path.encodeURLParameter()
            call.respondRedirect("/admin/login?next=$next")
            return@onCall
        }

        // Validate JWT
        val tokenClaims = try {
            jwtService.validateToken(sessionCookie)
        } catch (e: Exception) {
            val next = path.encodeURLParameter()
            call.respondRedirect("/admin/login?error=expired&next=$next")
            return@onCall
        }

        val claims = tokenClaims as? AdminClaims
        if (claims == null) {
            val next = path.encodeURLParameter()
            call.respondRedirect("/admin/login?error=expired&next=$next")
            return@onCall
        }

        // Sliding session: refresh token if < 50% TTL remaining
        try {
            val decoded = JWT.decode(sessionCookie)
            val expiresAt = decoded.expiresAt
            if (expiresAt != null) {
                val remainingMs = expiresAt.time - System.currentTimeMillis()
                if (ttlMs != null && remainingMs < ttlMs / 2) {
                    val newToken = jwtService.generateAdminAccessToken(claims)
                    call.response.cookies.append(
                        Cookie(
                            name = ADMIN_SESSION_COOKIE,
                            value = newToken,
                            maxAge = ADMIN_SESSION_MAX_AGE,
                            path = "/",
                            httpOnly = true,
                            secure = call.request.local.scheme == "https",
                            extensions = mapOf("SameSite" to "Lax")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Non-critical: if refresh fails, continue with existing token
        }

        // Add X-Session-Expires header with token expiration epoch seconds
        try {
            val decoded = JWT.decode(sessionCookie)
            val expiresAtDate = decoded.expiresAt
            if (expiresAtDate != null) {
                call.response.header("X-Session-Expires", (expiresAtDate.time / 1000).toString())
            }
        } catch (_: Exception) {
            // Non-critical
        }

        // Store claims in call attributes for later use
        call.attributes.put(AdminSessionKey, claims)
    }
}

/**
 * Extension to get admin session from call.
 */
val ApplicationCall.adminSession: AdminClaims?
    get() = attributes.getOrNull(AdminSessionKey)
