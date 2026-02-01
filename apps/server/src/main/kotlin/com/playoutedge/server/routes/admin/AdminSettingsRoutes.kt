package com.playoutedge.server.routes.admin

import com.playoutedge.auth.PasswordService
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.persistence.repositories.UserRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.settings.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
    passwordService: PasswordService
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

            // Tenant settings (would come from tenant entity)
            val settings = TenantSettingsView(
                tenantId = session.tenantId,
                name = "Demo Tenant", // Would fetch from TenantRepository
                timezone = "UTC",
                offlineThresholdMinutes = 5,
                maintenanceMode = false,
                maintenanceReason = null,
                releaseRing = "STABLE"
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

            // Would update tenant settings here
            // tenantRepository.updateSettings(...)

            call.respondRedirect("/admin/settings?success=Settings+updated")
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
                        createdAt = user.createdAt
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

                // Would also update displayName and role here

                call.respondRedirect("/admin/settings/users/$userId?success=User+updated")
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

                // API keys would be fetched from repository
                val apiKeys = emptyList<ApiKeyItem>()

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

                // Generate API key
                val apiKey = "pk_${UUID.randomUUID().toString().replace("-", "")}"

                // Would store in repository with hashed key
                // apiKeyRepository.create(tenantId, name, hash(apiKey), scopes, expiresAt)

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

                val keyId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                // Would delete from repository
                // apiKeyRepository.delete(tenantId, keyId)

                call.respondRedirect("/admin/settings/api-keys")
            }
        }
    }
}
