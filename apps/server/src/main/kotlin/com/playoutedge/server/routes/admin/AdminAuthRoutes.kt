package com.playoutedge.server.routes.admin

import com.playoutedge.auth.AdminClaims
import com.playoutedge.auth.EmailService
import com.playoutedge.auth.JwtService
import com.playoutedge.auth.PasswordService
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.persistence.repositories.TenantRepository
import com.playoutedge.persistence.repositories.UserRepository
import com.playoutedge.persistence.tables.PasswordResetTokens
import com.playoutedge.persistence.tables.UserSessions
import com.playoutedge.persistence.tables.Users
import com.playoutedge.server.views.auth.forgotPasswordFormView
import com.playoutedge.server.views.auth.loginView
import com.playoutedge.server.views.auth.resetPasswordSuccessView
import com.playoutedge.server.views.auth.resetPasswordView
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.security.SecureRandom

const val ADMIN_SESSION_COOKIE = "admin_session"
const val ADMIN_SESSION_MAX_AGE = 86400 // 24 hours
const val ADMIN_SESSION_MAX_AGE_REMEMBER = 2592000 // 30 days

fun Route.adminAuthRoutes(
    userRepository: UserRepository,
    jwtService: JwtService,
    passwordService: PasswordService,
    tenantRepository: TenantRepository,
    emailService: EmailService
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
            val remember = params["remember"] != null

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
                tenantId = user.tenantId,
                role = primaryRole
            )
            val token = jwtService.generateAdminAccessToken(claims)

            // Set cookie with remember me support
            val maxAge = if (remember) ADMIN_SESSION_MAX_AGE_REMEMBER else ADMIN_SESSION_MAX_AGE
            call.response.cookies.append(
                Cookie(
                    name = ADMIN_SESSION_COOKIE,
                    value = token,
                    maxAge = maxAge,
                    path = "/",
                    httpOnly = true,
                    secure = call.request.local.scheme == "https",
                    extensions = mapOf("SameSite" to "Lax")
                )
            )

            // Record session in user_sessions table
            try {
                val now = Clock.System.now()
                newSuspendedTransaction {
                    UserSessions.insert {
                        it[UserSessions.userId] = user.id
                        it[ipAddress] = call.request.local.remoteHost
                        it[userAgent] = call.request.headers["User-Agent"]?.take(500)
                        it[createdAt] = now
                        it[lastActiveAt] = now
                        it[isActive] = true
                    }
                }
            } catch (_: Exception) {
                // Non-critical: don't block login if session recording fails
            }

            call.respondRedirect(next)
        }

        // GET /admin/forgot-password - Forgot password page
        get("/forgot-password") {
            call.respondHtml {
                forgotPasswordFormView()
            }
        }

        // POST /admin/forgot-password - Process forgot password
        post("/forgot-password") {
            val params = call.receiveParameters()
            val email = params["email"] ?: ""

            val user = userRepository.findByEmail(email)

            if (user != null) {
                // Generate a secure random token
                val random = SecureRandom()
                val tokenBytes = ByteArray(32)
                random.nextBytes(tokenBytes)
                val resetToken = tokenBytes.joinToString("") { "%02x".format(it) }

                val now = Clock.System.now()
                val expiresAt = kotlinx.datetime.Instant.fromEpochMilliseconds(
                    now.toEpochMilliseconds() + 3600000 // 1 hour
                )

                // Store token in database
                newSuspendedTransaction {
                    PasswordResetTokens.insert {
                        it[PasswordResetTokens.userId] = user.id
                        it[PasswordResetTokens.token] = resetToken
                        it[PasswordResetTokens.expiresAt] = expiresAt
                        it[PasswordResetTokens.createdAt] = now
                    }
                }

                // Send email
                val baseUrl = "${call.request.local.scheme}://${call.request.local.serverHost}:${call.request.local.serverPort}"
                try {
                    emailService.sendPasswordResetEmail(email, resetToken, baseUrl)
                } catch (_: Exception) {
                    // Log but don't reveal failure to user
                }
            }

            // Always show success message to avoid revealing if email exists
            call.respondHtml {
                forgotPasswordFormView(
                    success = "If an account with that email exists, we've sent a password reset link."
                )
            }
        }

        // GET /admin/reset-password - Show reset password form
        get("/reset-password") {
            val token = call.request.queryParameters["token"]

            if (token.isNullOrBlank()) {
                call.respondRedirect("/admin/forgot-password")
                return@get
            }

            // Validate token exists and is not expired/used
            val valid = newSuspendedTransaction {
                val now = Clock.System.now()
                PasswordResetTokens.selectAll()
                    .where {
                        (PasswordResetTokens.token eq token) and
                        (PasswordResetTokens.expiresAt greater now) and
                        (PasswordResetTokens.usedAt.isNull())
                    }
                    .count() > 0
            }

            if (!valid) {
                call.respondHtml {
                    forgotPasswordFormView(error = "This reset link is invalid or has expired. Please request a new one.")
                }
                return@get
            }

            call.respondHtml {
                resetPasswordView(token = token)
            }
        }

        // POST /admin/reset-password - Process password reset
        post("/reset-password") {
            val params = call.receiveParameters()
            val token = params["token"] ?: ""
            val password = params["password"] ?: ""
            val confirmPassword = params["confirmPassword"] ?: ""

            if (password != confirmPassword) {
                call.respondHtml {
                    resetPasswordView(token = token, error = "Passwords do not match.")
                }
                return@post
            }

            if (password.length < 8) {
                call.respondHtml {
                    resetPasswordView(token = token, error = "Password must be at least 8 characters.")
                }
                return@post
            }

            // Find token and update password
            val success = newSuspendedTransaction {
                val now = Clock.System.now()
                val resetRow = PasswordResetTokens.selectAll()
                    .where {
                        (PasswordResetTokens.token eq token) and
                        (PasswordResetTokens.expiresAt greater now) and
                        (PasswordResetTokens.usedAt.isNull())
                    }
                    .firstOrNull() ?: return@newSuspendedTransaction false

                val userId = resetRow[PasswordResetTokens.userId]

                // Update password
                val hashedPassword = passwordService.hash(password)
                Users.update({ Users.id eq userId }) {
                    it[passwordHash] = hashedPassword
                }

                // Mark token as used
                PasswordResetTokens.update({ PasswordResetTokens.token eq token }) {
                    it[usedAt] = now
                }

                true
            }

            if (success) {
                call.respondHtml {
                    resetPasswordSuccessView()
                }
            } else {
                call.respondHtml {
                    forgotPasswordFormView(error = "This reset link is invalid or has expired. Please request a new one.")
                }
            }
        }

        // POST /admin/logout - Logout
        post("/logout") {
            // Deactivate user's active sessions
            try {
                val sessionCookie = call.request.cookies[ADMIN_SESSION_COOKIE]
                if (sessionCookie != null) {
                    val tokenClaims = try { jwtService.validateToken(sessionCookie) as? AdminClaims } catch (_: Exception) { null }
                    if (tokenClaims != null) {
                        newSuspendedTransaction {
                            UserSessions.update({ (UserSessions.userId eq tokenClaims.subject) and (UserSessions.isActive eq true) }) {
                                it[isActive] = false
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Non-critical
            }

            call.response.cookies.appendExpired(
                name = ADMIN_SESSION_COOKIE,
                path = "/admin"
            )
            call.respondRedirect("/admin/login")
        }
    }
}
