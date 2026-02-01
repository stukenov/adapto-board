package com.playoutedge.domain.enums

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    LOCKED
}

enum class UserRole {
    TENANT_ADMIN,
    OPERATOR,
    SUPPORT_AGENT,
    SUPPORT_ADMIN
}
