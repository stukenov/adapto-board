package com.playoutedge.server.routes.admin

import com.playoutedge.auth.AdminClaims
import com.playoutedge.auth.JwtService
import com.playoutedge.auth.PasswordService
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.persistence.repositories.UserRepository
import com.playoutedge.server.views.auth.loginView
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val ADMIN_SESSION_COOKIE = "admin_session"
const val ADMIN_SESSION_MAX_AGE = 86400 // 24 hours

fun Route.adminAuthRoutes(
    userRepository: UserRepository,
    jwtService: JwtService,
    passwordService: PasswordService
) {
    route("/admin") {
        // GET /admin/login - Show login form
        get("/login") {
            val error = call.request.queryParameters["error"]
            val next = call.request.queryParameters["next"]

            call.respondHtml {
                loginView(
                    error = when (error) {
                        "invalid" -> "Invalid email or password"
                        "expired" -> "Your session has expired. Please sign in again."
                        else -> null
                    },
                    next = next
                )
            }
        }

        // POST /admin/login - Process login
        post("/login") {
            val params = call.receiveParameters()
            val email = params["email"] ?: ""
            val password = params["password"] ?: ""
            val next = params["next"] ?: "/admin"

            // Find user by email
            val user = userRepository.findByEmail(email)

            if (user == null || !passwordService.verify(password, user.passwordHash)) {
                call.respondRedirect("/admin/login?error=invalid&next=${next.encodeURLParameter()}")
                return@post
            }

            // Get user's role (default to OPERATOR if none)
            val roles = userRepository.getRoles(user.id.value)
            val primaryRole = roles.maxByOrNull { it.ordinal } ?: UserRole.OPERATOR

            // Generate JWT token
            val claims = AdminClaims(
                subject = user.id.value,
                tenantId = user.tenant.id.value,
                role = primaryRole
            )
            val token = jwtService.generateAdminAccessToken(claims)

            // Set cookie
            call.response.cookies.append(
                Cookie(
                    name = ADMIN_SESSION_COOKIE,
                    value = token,
                    maxAge = ADMIN_SESSION_MAX_AGE,
                    path = "/admin",
                    httpOnly = true,
                    secure = call.request.local.scheme == "https",
                    extensions = mapOf("SameSite" to "Lax")
                )
            )

            call.respondRedirect(next)
        }

        // POST /admin/logout - Logout
        post("/logout") {
            call.response.cookies.appendExpired(
                name = ADMIN_SESSION_COOKIE,
                path = "/admin"
            )
            call.respondRedirect("/admin/login")
        }
    }
}
