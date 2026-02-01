package com.playoutedge.server.plugins

import com.playoutedge.auth.AdminClaims
import com.playoutedge.auth.AuthConfig
import com.playoutedge.auth.JwtService
import com.playoutedge.server.routes.admin.ADMIN_SESSION_COOKIE
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
}

/**
 * Plugin that validates admin session cookies and protects admin routes.
 */
val AdminSessionPlugin = createApplicationPlugin(name = "AdminSessionPlugin", ::AdminSessionPluginConfig) {
    val jwtService = pluginConfig.jwtService ?: error("JwtService not configured")

    onCall { call ->
        val path = call.request.local.uri

        // Only check admin routes (except login, logout, static)
        if (!path.startsWith("/admin")) return@onCall
        if (path.startsWith("/admin/login")) return@onCall
        if (path.startsWith("/admin/logout")) return@onCall
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

        // Store claims in call attributes for later use
        call.attributes.put(AdminSessionKey, claims)
    }
}

/**
 * Extension to get admin session from call.
 */
val ApplicationCall.adminSession: AdminClaims?
    get() = attributes.getOrNull(AdminSessionKey)
