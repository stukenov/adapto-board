package com.playoutedge.server.plugins

import com.playoutedge.auth.Permission
import com.playoutedge.auth.RolePermissions
import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.domain.enums.UserRole
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*

private val RequiredRolesKey = AttributeKey<Set<UserRole>>("RequiredRoles")
private val RequiredPermissionsKey = AttributeKey<Set<Permission>>("RequiredPermissions")

val RbacPlugin = createRouteScopedPlugin(name = "RbacPlugin") {
    onCall { call ->
        val requiredRoles = call.attributes.getOrNull(RequiredRolesKey)
        val requiredPermissions = call.attributes.getOrNull(RequiredPermissionsKey)

        val adminClaims = call.adminClaims
        if (adminClaims == null && (requiredRoles != null || requiredPermissions != null)) {
            throw ApiException.Forbidden(
                code = ErrorCode.FORBIDDEN_ROLE,
                message = "Admin authentication required"
            )
        }

        if (adminClaims != null) {
            if (requiredRoles != null && adminClaims.role !in requiredRoles) {
                throw ApiException.Forbidden(
                    code = ErrorCode.FORBIDDEN_ROLE,
                    message = "Insufficient role: ${adminClaims.role} not in ${requiredRoles.joinToString()}"
                )
            }

            if (requiredPermissions != null) {
                val missingPermissions = requiredPermissions.filter {
                    !RolePermissions.hasPermission(adminClaims.role, it)
                }
                if (missingPermissions.isNotEmpty()) {
                    throw ApiException.Forbidden(
                        code = ErrorCode.FORBIDDEN_ROLE,
                        message = "Missing permissions: ${missingPermissions.joinToString()}"
                    )
                }
            }
        }
    }
}

fun Route.requireRole(vararg roles: UserRole) {
    attributes.put(RequiredRolesKey, roles.toSet())
    install(RbacPlugin)
}

fun Route.requirePermission(vararg permissions: Permission) {
    attributes.put(RequiredPermissionsKey, permissions.toSet())
    install(RbacPlugin)
}
