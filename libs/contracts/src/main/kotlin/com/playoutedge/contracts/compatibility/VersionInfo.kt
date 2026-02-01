package com.playoutedge.contracts.compatibility

import kotlinx.serialization.Serializable

/**
 * Server version information for client compatibility checking.
 */
@Serializable
data class VersionInfo(
    val serverVersion: String,
    val apiVersion: String,
    val minSupportedPlayerVersion: String,
    val recommendedPlayerVersion: String,
    val forceUpdateRequired: Boolean = false,
    val forceUpdateDeadline: String? = null,
    val deprecationWarnings: List<String> = emptyList()
)

/**
 * Feature flags to enable/disable functionality per-tenant.
 */
@Serializable
data class FeatureFlags(
    val flags: Map<String, Boolean> = emptyMap()
) {
    fun isEnabled(feature: String): Boolean = flags[feature] ?: false
}

/**
 * Default feature flags for the system.
 */
object DefaultFeatures {
    const val SSE_OVERLAY = "sseOverlay"
    const val ENHANCED_CACHE = "enhancedCache"
    const val OFFLINE_MODE = "offlineMode"

    val defaults = mapOf(
        SSE_OVERLAY to true,
        ENHANCED_CACHE to false,
        OFFLINE_MODE to true
    )
}
