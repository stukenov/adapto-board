package com.playoutedge.server.routes.admin

import com.playoutedge.auth.PasswordService
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.persistence.repositories.UserRepository
import com.playoutedge.persistence.tables.NotificationPreferences
import com.playoutedge.persistence.tables.UserSessions
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.settings.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.security.MessageDigest
import java.util.UUID

private const val DEFAULT_STORAGE_LIMIT_BYTES = 10_737_418_240L // 10 GB
private const val DEFAULT_DEVICE_LIMIT = 50

/**
 * Admin settings routes.
 */
fun Route.adminSettingsRoutes(
    userRepository: UserRepository,
    assetRepository: AssetRepository,
    deviceRepository: DeviceRepository,
    passwordService: PasswordService,
    tenantRepository: com.playoutedge.persistence.repositories.TenantRepository,
    apiKeyRepository: com.playoutedge.persistence.repositories.ApiKeyRepository
) {
    route("/admin/settings") {
        // GET /admin/settings - General settings
        get {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)

            // Get storage quota
            val usedBytes = assetRepository.getTotalStorageBytes(tenantId)
            val storageQuota = StorageQuotaView(
                usedBytes = usedBytes,
                limitBytes = DEFAULT_STORAGE_LIMIT_BYTES,
                usedPercent = if (DEFAULT_STORAGE_LIMIT_BYTES > 0) {
                    ((usedBytes * 100) / DEFAULT_STORAGE_LIMIT_BYTES).toInt()
                } else 0
            )

            // Get device quota
            val enrolledDevices = deviceRepository.findAll(tenantId).size
            val deviceQuota = DeviceQuotaView(
                enrolled = enrolledDevices,
                limit = DEFAULT_DEVICE_LIMIT,
                usedPercent = if (DEFAULT_DEVICE_LIMIT > 0) {
                    (enrolledDevices * 100) / DEFAULT_DEVICE_LIMIT
                } else 0
            )

            // Fetch actual tenant settings
            val tenant = tenantRepository.findById(session.tenantId)
            val settings = TenantSettingsView(
                tenantId = session.tenantId,
                name = tenant?.name ?: "Unknown",
                timezone = "UTC",
                offlineThresholdMinutes = 5,
                maintenanceMode = tenant?.maintenanceMode ?: false,
                maintenanceReason = tenant?.maintenanceReason,
                releaseRing = tenant?.releaseRing?.name ?: "STABLE"
            )

            call.respondHtml {
                settingsMainView(session, settings, storageQuota, deviceQuota)
            }
        }

        // POST /admin/settings - Update settings
        post {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val params = call.receiveParameters()
            val name = params["name"] ?: ""
            val timezone = params["timezone"] ?: "UTC"
            val offlineThreshold = params["offlineThresholdMinutes"]?.toIntOrNull() ?: 5
            val maintenanceMode = params["maintenanceMode"] != null
            val maintenanceReason = params["maintenanceReason"]?.takeIf { it.isNotBlank() }
            val releaseRing = params["releaseRing"] ?: "STABLE"

            // Update tenant settings
            val tenant = tenantRepository.findById(session.tenantId)
            if (tenant != null) {
                org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction {
                    tenant.name = name
                    tenant.maintenanceMode = maintenanceMode
                    tenant.maintenanceReason = maintenanceReason
                    tenant.releaseRing = com.playoutedge.domain.enums.ReleaseRing.valueOf(releaseRing)
                }
            }

            call.respondRedirect("/admin/settings?success=Settings+updated")
        }

        // === Profile ===

        // GET /admin/settings/profile
        get("/profile") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@get }
            val forceChange = call.request.queryParameters["force"] == "true"
            call.respondHtml {
                profileView(session = session, forceChange = forceChange)
            }
        }

        // POST /admin/settings/profile/change-password
        post("/profile/change-password") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
            val params = call.receiveParameters()
            val currentPassword = params["currentPassword"]
            val newPassword = params["newPassword"] ?: ""
            val confirmPassword = params["confirmPassword"] ?: ""
            val force = params["force"] == "true"

            if (newPassword.length < 8) {
                call.respondHtml { profileView(session, error = "Password must be at least 8 characters", forceChange = force) }
                return@post
            }
            if (newPassword != confirmPassword) {
                call.respondHtml { profileView(session, error = "Passwords do not match", forceChange = force) }
                return@post
            }

            val user = userRepository.findById(session.subject)
            if (user == null) {
                call.respondHtml { profileView(session, error = "User not found", forceChange = force) }
                return@post
            }

            if (!force && currentPassword != null) {
                if (!passwordService.verify(currentPassword, user.passwordHash)) {
                    call.respondHtml { profileView(session, error = "Current password is incorrect", forceChange = force) }
                    return@post
                }
            }

            val hashedPassword = passwordService.hash(newPassword)
            userRepository.updatePassword(session.subject, hashedPassword)

            if (force) {
                call.respondRedirect("/admin")
            } else {
                call.respondHtml { profileView(session, success = "Password changed successfully") }
            }
        }

        // === API Docs ===

        get("/api-docs") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@get }
            call.respondHtml { apiDocsView(session) }
        }

        // === Users ===

        route("/users") {
            // GET /admin/settings/users - List users
            get {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                val tenantId = TenantId(session.tenantId)
                val users = userRepository.findAllByTenant(tenantId)

                val userItems = users.map { user ->
                    UserListItem(
                        id = user.id.value,
                        email = user.email,
                        displayName = user.displayName,
                        role = userRepository.getRoles(user.id.value).firstOrNull() ?: UserRole.OPERATOR,
                        status = user.status,
                        createdAt = user.createdAt,
                        inviteExpiresAt = user.inviteExpiresAt
                    )
                }

                call.respondHtml {
                    usersListView(session, userItems)
                }
            }

            // GET /admin/settings/users/invite - Invite form
            get("/invite") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                call.respondHtml {
                    inviteUserView(session)
                }
            }

            // POST /admin/settings/users/invite - Create user
            post("/invite") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val tenantId = TenantId(session.tenantId)
                val params = call.receiveParameters()
                val email = params["email"] ?: ""
                val displayName = params["displayName"] ?: ""
                val password = params["password"] ?: ""
                val role = params["role"]?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
                    ?: UserRole.OPERATOR

                if (email.isBlank() || displayName.isBlank() || password.length < 8) {
                    call.respondHtml {
                        inviteUserView(session, error = "All fields are required. Password must be at least 8 characters.")
                    }
                    return@post
                }

                // Check if email already exists
                val existing = userRepository.findByTenantAndEmail(tenantId, email)
                if (existing != null) {
                    call.respondHtml {
                        inviteUserView(session, error = "A user with this email already exists.")
                    }
                    return@post
                }

                val passwordHash = passwordService.hash(password)
                userRepository.create(tenantId, email, displayName, passwordHash, role)

                call.respondRedirect("/admin/settings/users")
            }

            // GET /admin/settings/users/:id - Edit user
            get("/{id}") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                val userId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                if (userId == null) {
                    call.respondRedirect("/admin/settings/users")
                    return@get
                }

                val user = userRepository.findById(userId)
                if (user == null) {
                    call.respondRedirect("/admin/settings/users")
                    return@get
                }

                val userDetail = UserDetail(
                    id = user.id.value,
                    email = user.email,
                    displayName = user.displayName,
                    role = userRepository.getRoles(userId).firstOrNull() ?: UserRole.OPERATOR,
                    status = user.status,
                    createdAt = user.createdAt
                )

                call.respondHtml {
                    editUserView(session, userDetail)
                }
            }

            // POST /admin/settings/users/:id - Update user
            post("/{id}") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val userId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                if (userId == null) {
                    call.respondRedirect("/admin/settings/users")
                    return@post
                }

                val params = call.receiveParameters()
                val status = params["status"]?.let { runCatching { UserStatus.valueOf(it) }.getOrNull() }

                if (status != null) {
                    userRepository.updateStatus(userId, status)
                }

                val displayName = params["displayName"]?.takeIf { it.isNotBlank() }
                val role = params["role"]?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
                if (displayName != null || role != null) {
                    org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction {
                        val user = com.playoutedge.persistence.entities.UserEntity.findById(userId)
                        if (user != null) {
                            if (displayName != null) user.displayName = displayName
                            if (role != null) {
                                com.playoutedge.persistence.tables.UserRoles.deleteWhere { com.playoutedge.persistence.tables.UserRoles.userId eq userId }
                                com.playoutedge.persistence.tables.UserRoles.insert {
                                    it[com.playoutedge.persistence.tables.UserRoles.userId] = user.id
                                    it[com.playoutedge.persistence.tables.UserRoles.role] = role
                                }
                            }
                        }
                    }
                }

                call.respondRedirect("/admin/settings/users/$userId?success=User+updated")
            }

            // POST /admin/settings/users/:id/resend-invite - Resend invite (reset expiration)
            post("/{id}/resend-invite") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val userId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                if (userId == null) {
                    call.respondRedirect("/admin/settings/users")
                    return@post
                }

                newSuspendedTransaction {
                    val user = com.playoutedge.persistence.entities.UserEntity.findById(userId)
                    if (user != null) {
                        user.inviteExpiresAt = Clock.System.now().plus(kotlin.time.Duration.parse("7d"))
                    }
                }

                call.respondRedirect("/admin/settings/users")
            }

            // POST /admin/settings/users/:id/reset-password - Reset password
            post("/{id}/reset-password") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val userId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                if (userId == null) {
                    call.respondRedirect("/admin/settings/users")
                    return@post
                }

                val params = call.receiveParameters()
                val password = params["password"] ?: ""

                if (password.length < 8) {
                    call.respondRedirect("/admin/settings/users/$userId?error=Password+must+be+at+least+8+characters")
                    return@post
                }

                val passwordHash = passwordService.hash(password)
                userRepository.updatePassword(userId, passwordHash)

                call.respondRedirect("/admin/settings/users/$userId?success=Password+reset")
            }
        }

        // === API Keys (placeholder) ===

        route("/api-keys") {
            // GET /admin/settings/api-keys - List API keys
            get {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                val tenantId = TenantId(session.tenantId)
                val keys = apiKeyRepository.findByTenant(tenantId)
                val apiKeys = keys.map { key ->
                    ApiKeyItem(
                        id = key.id.value,
                        name = key.name,
                        prefix = key.keyPrefix,
                        scopes = key.scopes.split(",").filter { it.isNotBlank() },
                        lastUsed = null,
                        createdAt = key.createdAt,
                        expiresAt = key.expiresAt
                    )
                }

                call.respondHtml {
                    apiKeysView(session, apiKeys)
                }
            }

            // GET /admin/settings/api-keys/new - New API key form
            get("/new") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                call.respondHtml {
                    newApiKeyView(session)
                }
            }

            // POST /admin/settings/api-keys - Create API key
            post {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val params = call.receiveParameters()
                val name = params["name"] ?: ""
                val scopes = params.getAll("scopes") ?: listOf("read")
                val expiresIn = params["expiresIn"] ?: "90"

                if (name.isBlank()) {
                    call.respondHtml {
                        newApiKeyView(session, error = "Name is required")
                    }
                    return@post
                }

                val tenantId = TenantId(session.tenantId)

                // Generate API key
                val apiKey = "pk_${UUID.randomUUID().toString().replace("-", "")}"
                val keyHash = MessageDigest.getInstance("SHA-256")
                    .digest(apiKey.toByteArray())
                    .joinToString("") { "%02x".format(it) }
                val keyPrefix = apiKey.take(12)
                val expiresAt = expiresIn.toLongOrNull()?.let {
                    kotlinx.datetime.Instant.fromEpochSeconds(kotlinx.datetime.Clock.System.now().epochSeconds + it * 86400)
                }

                apiKeyRepository.create(tenantId, name, keyHash, keyPrefix, scopes, expiresAt)

                call.respondHtml {
                    newApiKeyView(session, createdKey = apiKey)
                }
            }

            // POST /admin/settings/api-keys/:id/revoke - Revoke API key
            post("/{id}/revoke") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val tenantId = TenantId(session.tenantId)
                val keyId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                if (keyId != null) {
                    apiKeyRepository.revoke(tenantId, keyId)
                }

                call.respondRedirect("/admin/settings/api-keys")
            }
        }

        // === Notification Preferences ===

        get("/notifications") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@get }
            val success = call.request.queryParameters["success"] == "true"
            val prefs = newSuspendedTransaction {
                NotificationPreferences.selectAll()
                    .where { NotificationPreferences.userId eq session.subject }
                    .firstOrNull()
            }
            val prefsView = if (prefs != null) {
                NotificationPrefsView(
                    emailOnDeviceOffline = prefs[NotificationPreferences.emailOnDeviceOffline],
                    emailOnAlert = prefs[NotificationPreferences.emailOnAlert],
                    emailOnSchedulePublish = prefs[NotificationPreferences.emailOnSchedulePublish],
                    emailDailyDigest = prefs[NotificationPreferences.emailDailyDigest]
                )
            } else {
                NotificationPrefsView()
            }
            call.respondHtml {
                notificationPreferencesView(session, prefsView, success)
            }
        }

        post("/notifications") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
            val params = call.receiveParameters()
            newSuspendedTransaction {
                val exists = NotificationPreferences.selectAll()
                    .where { NotificationPreferences.userId eq session.subject }.count() > 0
                if (exists) {
                    NotificationPreferences.update({ NotificationPreferences.userId eq session.subject }) {
                        it[emailOnDeviceOffline] = params["emailOnDeviceOffline"] != null
                        it[emailOnAlert] = params["emailOnAlert"] != null
                        it[emailOnSchedulePublish] = params["emailOnSchedulePublish"] != null
                        it[emailDailyDigest] = params["emailDailyDigest"] != null
                        it[updatedAt] = Clock.System.now()
                    }
                } else {
                    NotificationPreferences.insert {
                        it[userId] = session.subject
                        it[emailOnDeviceOffline] = params["emailOnDeviceOffline"] != null
                        it[emailOnAlert] = params["emailOnAlert"] != null
                        it[emailOnSchedulePublish] = params["emailOnSchedulePublish"] != null
                        it[emailDailyDigest] = params["emailDailyDigest"] != null
                        it[createdAt] = Clock.System.now()
                        it[updatedAt] = Clock.System.now()
                    }
                }
            }
            call.respondRedirect("/admin/settings/notifications?success=true")
        }

        // === Security / Sessions ===

        get("/security") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@get }
            val success = call.request.queryParameters["success"]
            val sessions = newSuspendedTransaction {
                UserSessions.selectAll()
                    .where { (UserSessions.userId eq session.subject) }
                    .map { row ->
                        SessionItem(
                            id = row[UserSessions.id].value,
                            ipAddress = row[UserSessions.ipAddress],
                            userAgent = row[UserSessions.userAgent],
                            createdAt = row[UserSessions.createdAt],
                            lastActiveAt = row[UserSessions.lastActiveAt],
                            isCurrent = false // Could be enhanced with session token matching
                        )
                    }
                    .filter { it.id != UUID(0, 0) } // Filter active only
            }
            call.respondHtml {
                securityView(session, sessions, success)
            }
        }

        post("/security/terminate/{sessionId}") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
            val sessionId = call.parameters["sessionId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            if (sessionId != null) {
                newSuspendedTransaction {
                    UserSessions.update({ (UserSessions.id eq sessionId) and (UserSessions.userId eq session.subject) }) {
                        it[isActive] = false
                    }
                }
            }
            call.respondRedirect("/admin/settings/security?success=Session+terminated")
        }
    }
}
