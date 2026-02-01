package com.playoutedge.player.telemetry

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.playoutedge.player.config.ConfigManager
import com.playoutedge.player.network.PlayerApiClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentLinkedQueue

private val Context.asrunDataStore: DataStore<Preferences> by preferencesDataStore(name = "asrun_buffer")

/**
 * Collects and batches as-run events.
 */
class AsrunCollector(
    private val context: Context,
    private val apiClient: PlayerApiClient,
    private val configManager: ConfigManager,
    private val scope: CoroutineScope
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val eventQueue = ConcurrentLinkedQueue<AsrunEvent>()
    private var uploadJob: Job? = null

    companion object {
        private const val BATCH_SIZE = 50
        private const val MAX_BUFFER_SIZE = 1000
        private const val UPLOAD_INTERVAL_MS = 30_000L
        private val BUFFER_KEY = stringPreferencesKey("buffered_events")
    }

    init {
        // Load buffered events on init
        scope.launch {
            loadBufferedEvents()
        }
    }

    /**
     * Start periodic upload.
     */
    fun start() {
        if (uploadJob?.isActive == true) return

        uploadJob = scope.launch {
            while (isActive) {
                uploadBatch()
                delay(UPLOAD_INTERVAL_MS)
            }
        }
    }

    /**
     * Stop collector.
     */
    fun stop() {
        uploadJob?.cancel()
        uploadJob = null
        // Persist remaining events
        scope.launch {
            persistEvents()
        }
    }

    /**
     * Record play start event.
     */
    fun recordPlayStart(assetId: String, channelId: String?, scheduleVersionId: String?) {
        addEvent(AsrunEvent(
            eventType = AsrunEventType.PLAY_START,
            assetId = assetId,
            channelId = channelId,
            scheduleVersionId = scheduleVersionId,
            timestamp = System.currentTimeMillis()
        ))
    }

    /**
     * Record play end event.
     */
    fun recordPlayEnd(assetId: String, channelId: String?, durationMs: Long) {
        addEvent(AsrunEvent(
            eventType = AsrunEventType.PLAY_END,
            assetId = assetId,
            channelId = channelId,
            timestamp = System.currentTimeMillis(),
            durationMs = durationMs
        ))
    }

    /**
     * Record skip event.
     */
    fun recordSkip(assetId: String, channelId: String?, reason: String) {
        addEvent(AsrunEvent(
            eventType = AsrunEventType.SKIP,
            assetId = assetId,
            channelId = channelId,
            timestamp = System.currentTimeMillis(),
            details = mapOf("reason" to reason)
        ))
    }

    /**
     * Record error event.
     */
    fun recordError(assetId: String?, errorCode: String, message: String) {
        addEvent(AsrunEvent(
            eventType = AsrunEventType.ERROR,
            assetId = assetId,
            timestamp = System.currentTimeMillis(),
            details = mapOf("code" to errorCode, "message" to message)
        ))
    }

    private fun addEvent(event: AsrunEvent) {
        eventQueue.add(event)

        // Enforce max buffer size
        while (eventQueue.size > MAX_BUFFER_SIZE) {
            eventQueue.poll() // Remove oldest
        }
    }

    private suspend fun uploadBatch() {
        if (eventQueue.isEmpty()) return

        val batch = mutableListOf<AsrunEvent>()
        repeat(BATCH_SIZE) {
            eventQueue.poll()?.let { batch.add(it) } ?: return@repeat
        }

        if (batch.isEmpty()) return

        val result = apiClient.sendAsrunBatch(batch)

        result.onFailure {
            // Re-queue failed events
            batch.forEach { eventQueue.add(it) }
        }
    }

    private suspend fun persistEvents() {
        if (eventQueue.isEmpty()) return

        val events = eventQueue.toList()
        context.asrunDataStore.edit { prefs ->
            prefs[BUFFER_KEY] = json.encodeToString(events)
        }
    }

    private suspend fun loadBufferedEvents() {
        val data = context.asrunDataStore.data.first()[BUFFER_KEY] ?: return
        try {
            val events = json.decodeFromString<List<AsrunEvent>>(data)
            events.forEach { eventQueue.add(it) }
            // Clear persisted data
            context.asrunDataStore.edit { it.remove(BUFFER_KEY) }
        } catch (e: Exception) {
            // Ignore parse errors
        }
    }
}
