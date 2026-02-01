package com.playoutedge.player.config

import kotlinx.serialization.Serializable

/**
 * Player configuration from server.
 */
@Serializable
data class PlayerConfig(
    val tenantId: String,
    val deviceId: String,
    val channelId: String?,
    val scheduleVersionId: String?,
    val assetBaseUrl: String,
    val signedUrlTtlSec: Int = 3600,
    val pollIntervalSec: Int = 60,
    val heartbeatIntervalSec: Int = 30,
    val offlineThresholdSec: Int = 300,
    val cacheQuotaMb: Int = 4096,
    val overlayEnabled: Boolean = true
)

/**
 * Config state.
 */
sealed class ConfigState {
    data object Loading : ConfigState()
    data class Loaded(val config: PlayerConfig) : ConfigState()
    data class Error(val message: String, val lastKnownConfig: PlayerConfig?) : ConfigState()
    data object NotEnrolled : ConfigState()
}

/**
 * Config change event.
 */
sealed class ConfigChange {
    data class ChannelChanged(val oldChannelId: String?, val newChannelId: String?) : ConfigChange()
    data class ScheduleVersionChanged(val oldVersion: String?, val newVersion: String?) : ConfigChange()
    data object NoChange : ConfigChange()
}
