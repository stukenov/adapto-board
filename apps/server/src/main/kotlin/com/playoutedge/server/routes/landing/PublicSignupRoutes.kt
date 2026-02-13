package com.playoutedge.server.routes.landing

import com.playoutedge.auth.AdminClaims
import com.playoutedge.auth.JwtService
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.server.routes.admin.ADMIN_SESSION_COOKIE
import com.playoutedge.server.routes.admin.ADMIN_SESSION_MAX_AGE
import com.playoutedge.server.services.RegistrationService
import com.playoutedge.server.views.landing.signupView
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.publicSignupRoutes(
    registrationService: RegistrationService,
    jwtService: JwtService
) {
    get("/signup") {
        val error = call.request.queryParameters["error"]
        val orgName = call.request.queryParameters["orgName"]
        val email = call.request.queryParameters["email"]
        val displayName = call.request.queryParameters["displayName"]

        call.respondHtml {
            signupView(
                error = error,
                orgName = orgName,
                email = email,
                displayName = displayName
            )
        }
    }

    post("/signup") {
        val params = call.receiveParameters()
        val orgName = params["orgName"] ?: ""
        val email = params["email"] ?: ""
        val displayName = params["displayName"] ?: ""
        val password = params["password"] ?: ""
        val confirmPassword = params["confirmPassword"] ?: ""

        // Check passwords match
        if (password != confirmPassword) {
            val qs = parametersOf(
                "error" to listOf("Passwords do not match"),
                "orgName" to listOf(orgName),
                "email" to listOf(email),
                "displayName" to listOf(displayName)
            ).formUrlEncode()
            call.respondRedirect("/signup?$qs")
            return@post
        }

        val result = registrationService.registerTenant(
            tenantName = orgName,
            email = email,
            displayName = displayName,
            password = password
        )

        val registration = result.getOrNull()
        if (registration != null) {
            // Generate JWT and set cookie
            val claims = AdminClaims(
                subject = registration.user.id.value,
                tenantId = registration.tenant.id.value,
                role = UserRole.TENANT_ADMIN
            )
            val token = jwtService.generateAdminAccessToken(claims)

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

            call.respondRedirect("/admin/onboarding")
        } else {
            val errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
            val qs = parametersOf(
                "error" to listOf(errorMessage),
                "orgName" to listOf(orgName),
                "email" to listOf(email),
                "displayName" to listOf(displayName)
            ).formUrlEncode()
            call.respondRedirect("/signup?$qs")
        }
    }
}
