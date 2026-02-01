package com.playoutedge.player.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.configDataStore: DataStore<Preferences> by preferencesDataStore(name = "player_config")

/**
 * Repository for persisting player configuration.
 */
class ConfigRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val CONFIG = stringPreferencesKey("config")
        val LAST_FETCH_TIME = stringPreferencesKey("last_fetch_time")
    }

    /**
     * Flow of stored config.
     */
    val config: Flow<PlayerConfig?> = context.configDataStore.data.map { prefs ->
        prefs[Keys.CONFIG]?.let { json.decodeFromString<PlayerConfig>(it) }
    }

    /**
     * Get last known good config.
     */
    suspend fun getConfig(): PlayerConfig? = config.first()

    /**
     * Save config to storage.
     */
    suspend fun saveConfig(config: PlayerConfig) {
        context.configDataStore.edit { prefs ->
            prefs[Keys.CONFIG] = json.encodeToString(config)
            prefs[Keys.LAST_FETCH_TIME] = System.currentTimeMillis().toString()
        }
    }

    /**
     * Get last fetch time.
     */
    suspend fun getLastFetchTime(): Long? {
        return context.configDataStore.data.first()[Keys.LAST_FETCH_TIME]?.toLongOrNull()
    }

    /**
     * Clear stored config.
     */
    suspend fun clear() {
        context.configDataStore.edit { it.clear() }
    }

    /**
     * Detect changes between old and new config.
     */
    fun detectChanges(oldConfig: PlayerConfig?, newConfig: PlayerConfig): ConfigChange {
        if (oldConfig == null) return ConfigChange.NoChange

        return when {
            oldConfig.channelId != newConfig.channelId -> ConfigChange.ChannelChanged(
                oldChannelId = oldConfig.channelId,
                newChannelId = newConfig.channelId
            )
            oldConfig.scheduleVersionId != newConfig.scheduleVersionId -> ConfigChange.ScheduleVersionChanged(
                oldVersion = oldConfig.scheduleVersionId,
                newVersion = newConfig.scheduleVersionId
            )
            else -> ConfigChange.NoChange
        }
    }
}
