package com.playoutedge.auth

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

data class AuthConfig(
    val jwtSecret: String,
    val jwtIssuer: String,
    val jwtAudience: String,
    val adminAccessTokenTtl: Duration,
    val adminRefreshTokenTtl: Duration,
    val deviceAccessTokenTtl: Duration,
    val deviceRefreshTokenTtl: Duration,
    val enrollCodeTtl: Duration
) {
    companion object {
        fun fromEnvironment(): AuthConfig = AuthConfig(
            jwtSecret = System.getenv("JWT_SECRET") ?: "dev-secret-change-in-production",
            jwtIssuer = System.getenv("JWT_ISSUER") ?: "playout-edge",
            jwtAudience = System.getenv("JWT_AUDIENCE") ?: "playout-edge-api",
            adminAccessTokenTtl = 15.minutes,
            adminRefreshTokenTtl = 7.days,
            deviceAccessTokenTtl = 1.hours,
            deviceRefreshTokenTtl = 90.days,
            enrollCodeTtl = 30.minutes
        )
    }
}
