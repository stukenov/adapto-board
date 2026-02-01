package com.playoutedge.auth

import com.playoutedge.domain.enums.UserRole

object RolePermissions {
    private val permissions: Map<UserRole, Set<Permission>> = mapOf(
        UserRole.TENANT_ADMIN to setOf(
            Permission.USERS_READ,
            Permission.USERS_WRITE,
            Permission.CHANNELS_READ,
            Permission.CHANNELS_WRITE,
            Permission.ASSETS_READ,
            Permission.ASSETS_WRITE,
            Permission.ASSETS_DELETE,
            Permission.SCHEDULES_READ,
            Permission.SCHEDULES_WRITE,
            Permission.SCHEDULES_PUBLISH,
            Permission.DEVICES_READ,
            Permission.DEVICES_WRITE,
            Permission.DEVICES_ENROLL,
            Permission.DEVICES_REVOKE,
            Permission.OVERLAY_READ,
            Permission.OVERLAY_WRITE,
            Permission.TENANT_SETTINGS_READ,
            Permission.TENANT_SETTINGS_WRITE,
            Permission.REPORTS_READ,
            Permission.AUDIT_READ
        ),
        UserRole.OPERATOR to setOf(
            Permission.CHANNELS_READ,
            Permission.CHANNELS_WRITE,
            Permission.ASSETS_READ,
            Permission.ASSETS_WRITE,
            Permission.SCHEDULES_READ,
            Permission.SCHEDULES_WRITE,
            Permission.SCHEDULES_PUBLISH,
            Permission.DEVICES_READ,
            Permission.OVERLAY_READ,
            Permission.OVERLAY_WRITE,
            Permission.REPORTS_READ
        ),
        UserRole.SUPPORT_AGENT to setOf(
            Permission.SUPPORT_READ_ALL_TENANTS,
            Permission.CHANNELS_READ,
            Permission.ASSETS_READ,
            Permission.DEVICES_READ,
            Permission.REPORTS_READ,
            Permission.AUDIT_READ
        ),
        UserRole.SUPPORT_ADMIN to setOf(
            Permission.SUPPORT_READ_ALL_TENANTS,
            Permission.SUPPORT_WRITE_ALL_TENANTS,
            Permission.CHANNELS_READ,
            Permission.CHANNELS_WRITE,
            Permission.ASSETS_READ,
            Permission.ASSETS_WRITE,
            Permission.DEVICES_READ,
            Permission.DEVICES_WRITE,
            Permission.DEVICES_REVOKE,
            Permission.REPORTS_READ,
            Permission.AUDIT_READ
        )
    )

    fun getPermissions(role: UserRole): Set<Permission> {
        return permissions[role] ?: emptySet()
    }

    fun hasPermission(role: UserRole, permission: Permission): Boolean {
        return permission in getPermissions(role)
    }

    fun hasAnyPermission(role: UserRole, vararg permissions: Permission): Boolean {
        val rolePermissions = getPermissions(role)
        return permissions.any { it in rolePermissions }
    }

    fun hasAllPermissions(role: UserRole, vararg permissions: Permission): Boolean {
        val rolePermissions = getPermissions(role)
        return permissions.all { it in rolePermissions }
    }
}
