package com.playoutedge.player.playlist

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

private val Context.playlistDataStore: DataStore<Preferences> by preferencesDataStore(name = "playlist")

/**
 * Repository for persisting playlist data.
 */
class PlaylistRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val MANIFEST = stringPreferencesKey("manifest")
        val VERSION = stringPreferencesKey("version")
    }

    /**
     * Flow of stored manifest.
     */
    val manifest: Flow<PlaylistManifest?> = context.playlistDataStore.data.map { prefs ->
        prefs[Keys.MANIFEST]?.let { json.decodeFromString<PlaylistManifest>(it) }
    }

    /**
     * Get current manifest.
     */
    suspend fun getManifest(): PlaylistManifest? = manifest.first()

    /**
     * Save manifest to storage.
     */
    suspend fun saveManifest(manifest: PlaylistManifest) {
        context.playlistDataStore.edit { prefs ->
            prefs[Keys.MANIFEST] = json.encodeToString(manifest)
            prefs[Keys.VERSION] = manifest.scheduleVersionId
        }
    }

    /**
     * Get current version ID.
     */
    suspend fun getCurrentVersion(): String? =
        context.playlistDataStore.data.first()[Keys.VERSION]

    /**
     * Check if version changed.
     */
    suspend fun hasVersionChanged(newVersion: String): Boolean {
        val current = getCurrentVersion()
        return current != null && current != newVersion
    }

    /**
     * Clear stored playlist.
     */
    suspend fun clear() {
        context.playlistDataStore.edit { it.clear() }
    }
}
