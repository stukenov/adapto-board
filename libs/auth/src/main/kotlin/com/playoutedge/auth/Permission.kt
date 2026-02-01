package com.playoutedge.auth

enum class Permission {
    // User management
    USERS_READ,
    USERS_WRITE,

    // Channel management
    CHANNELS_READ,
    CHANNELS_WRITE,

    // Asset management
    ASSETS_READ,
    ASSETS_WRITE,
    ASSETS_DELETE,

    // Schedule management
    SCHEDULES_READ,
    SCHEDULES_WRITE,
    SCHEDULES_PUBLISH,

    // Device management
    DEVICES_READ,
    DEVICES_WRITE,
    DEVICES_ENROLL,
    DEVICES_REVOKE,

    // Overlay management
    OVERLAY_READ,
    OVERLAY_WRITE,

    // Tenant settings
    TENANT_SETTINGS_READ,
    TENANT_SETTINGS_WRITE,

    // Reports and audit
    REPORTS_READ,
    AUDIT_READ,

    // Support operations (internal)
    SUPPORT_READ_ALL_TENANTS,
    SUPPORT_WRITE_ALL_TENANTS
}
