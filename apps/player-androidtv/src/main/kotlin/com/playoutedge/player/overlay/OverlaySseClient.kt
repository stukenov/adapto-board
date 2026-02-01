package com.playoutedge.player.overlay

import com.playoutedge.player.network.PlayerApiClient
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlin.math.min

/**
 * SSE client for overlay updates.
 */
class OverlaySseClient(
    private val apiClient: PlayerApiClient,
    private val scope: CoroutineScope
) {
    private val json = Json { ignoreUnknownKeys = true }

    private var connectionJob: Job? = null
    private var currentBackoffMs = INITIAL_BACKOFF_MS

    private val _overlayState = MutableStateFlow(OverlayState(version = 0))
    val overlayState: StateFlow<OverlayState> = _overlayState.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    companion object {
        private const val INITIAL_BACKOFF_MS = 1_000L
        private const val MAX_BACKOFF_MS = 30_000L
    }

    enum class ConnectionState {
        Connecting,
        Connected,
        Disconnected,
        Error
    }

    /**
     * Connect to overlay stream.
     */
    fun connect(channelId: String) {
        disconnect()

        connectionJob = scope.launch {
            while (isActive) {
                try {
                    _connectionState.value = ConnectionState.Connecting

                    apiClient.getOverlayStream(channelId).execute { response ->
                        if (response.status.value in 200..299) {
                            _connectionState.value = ConnectionState.Connected
                            currentBackoffMs = INITIAL_BACKOFF_MS

                            val channel = response.bodyAsChannel()
                            processStream(channel)
                        } else {
                            throw Exception("HTTP ${response.status.value}")
                        }
                    }
                } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _connectionState.value = ConnectionState.Error

                    // Backoff and retry
                    delay(currentBackoffMs)
                    currentBackoffMs = min(currentBackoffMs * 2, MAX_BACKOFF_MS)
                }
            }
        }
    }

    /**
     * Disconnect from stream.
     */
    fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        _connectionState.value = ConnectionState.Disconnected
    }

    private suspend fun processStream(channel: ByteReadChannel) {
        val buffer = StringBuilder()

        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: break

            if (line.isEmpty()) {
                // End of event
                processEvent(buffer.toString())
                buffer.clear()
            } else {
                buffer.appendLine(line)
            }
        }

        _connectionState.value = ConnectionState.Disconnected
    }

    private fun processEvent(rawEvent: String) {
        val lines = rawEvent.lines()
        var eventType = "message"
        var data = ""

        for (line in lines) {
            when {
                line.startsWith("event:") -> eventType = line.removePrefix("event:").trim()
                line.startsWith("data:") -> data = line.removePrefix("data:").trim()
            }
        }

        if (data.isEmpty()) return

        try {
            when (eventType) {
                "state" -> {
                    val state = json.decodeFromString<OverlayState>(data)
                    _overlayState.value = state
                }
                "patch" -> {
                    val operations = json.decodeFromString<List<PatchOperation>>(data)
                    applyPatch(operations)
                }
                "keepalive" -> {
                    // Just keep connection alive
                }
            }
        } catch (e: Exception) {
            // Log parse error
        }
    }

    private fun applyPatch(operations: List<PatchOperation>) {
        val currentState = _overlayState.value
        val updatedWidgets = currentState.widgets.toMutableMap()

        for (op in operations) {
            when (op.op) {
                "upsert" -> {
                    op.widget?.let { widgetJson ->
                        // Parse widget from JSON
                        try {
                            val widget = json.decodeFromString<Widget>(widgetJson.toString())
                            updatedWidgets[op.widgetId] = widget
                        } catch (e: Exception) {
                            // Skip invalid widget
                        }
                    }
                }
                "remove" -> {
                    updatedWidgets.remove(op.widgetId)
                }
            }
        }

        _overlayState.value = currentState.copy(
            version = currentState.version + 1,
            widgets = updatedWidgets
        )
    }
}
