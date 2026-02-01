package com.playoutedge.domain.enums

enum class TenantStatus {
    ACTIVE,
    SUSPENDED,
    MAINTENANCE
}

enum class SupportTier {
    BASIC,
    PREMIUM,
    ENTERPRISE
}

enum class ReleaseRing {
    STABLE,
    CANARY,
    BETA
}
