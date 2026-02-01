package com.playoutedge.server.services

import com.playoutedge.contracts.compatibility.DefaultFeatures
import com.playoutedge.contracts.compatibility.FeatureFlags
import com.playoutedge.contracts.compatibility.VersionInfo

/**
 * Service for checking player-server version compatibility.
 */
class CompatibilityService {
    companion object {
        // Server version - update on releases
        const val SERVER_VERSION = "1.0.0"
        const val API_VERSION = "v1"

        // Player version requirements
        const val MIN_SUPPORTED_PLAYER_VERSION = "1.0.0"
        const val RECOMMENDED_PLAYER_VERSION = "1.0.0"
    }

    /**
     * Get current version info for player config responses.
     */
    fun getVersionInfo(): VersionInfo {
        return VersionInfo(
            serverVersion = SERVER_VERSION,
            apiVersion = API_VERSION,
            minSupportedPlayerVersion = MIN_SUPPORTED_PLAYER_VERSION,
            recommendedPlayerVersion = RECOMMENDED_PLAYER_VERSION,
            forceUpdateRequired = false,
            forceUpdateDeadline = null,
            deprecationWarnings = emptyList()
        )
    }

    /**
     * Get feature flags for a tenant.
     * Currently returns defaults, can be extended for per-tenant configuration.
     */
    fun getFeatureFlags(tenantId: java.util.UUID? = null): FeatureFlags {
        // For now, return defaults. In future, can load tenant-specific flags.
        return FeatureFlags(DefaultFeatures.defaults)
    }

    /**
     * Check if a player version is supported.
     *
     * @param playerVersion The player app version string (e.g., "1.0.0")
     * @return CompatibilityResult with status and recommendations
     */
    fun checkCompatibility(playerVersion: String?): CompatibilityResult {
        if (playerVersion == null) {
            return CompatibilityResult(
                compatible = true,
                updateRecommended = false,
                message = "Unknown player version"
            )
        }

        val playerParts = parseVersion(playerVersion)
        val minParts = parseVersion(MIN_SUPPORTED_PLAYER_VERSION)
        val recommendedParts = parseVersion(RECOMMENDED_PLAYER_VERSION)

        // Check if player is below minimum
        if (compareVersions(playerParts, minParts) < 0) {
            return CompatibilityResult(
                compatible = false,
                updateRecommended = true,
                message = "Player version $playerVersion is no longer supported. Please update to $RECOMMENDED_PLAYER_VERSION or later."
            )
        }

        // Check if update is recommended
        if (compareVersions(playerParts, recommendedParts) < 0) {
            return CompatibilityResult(
                compatible = true,
                updateRecommended = true,
                message = "A newer player version is available: $RECOMMENDED_PLAYER_VERSION"
            )
        }

        return CompatibilityResult(
            compatible = true,
            updateRecommended = false,
            message = null
        )
    }

    private fun parseVersion(version: String): List<Int> {
        return version.split(".")
            .mapNotNull { it.toIntOrNull() }
            .let { if (it.size < 3) it + List(3 - it.size) { 0 } else it.take(3) }
    }

    private fun compareVersions(a: List<Int>, b: List<Int>): Int {
        for (i in 0 until minOf(a.size, b.size)) {
            if (a[i] != b[i]) return a[i] - b[i]
        }
        return a.size - b.size
    }
}

data class CompatibilityResult(
    val compatible: Boolean,
    val updateRecommended: Boolean,
    val message: String?
)
