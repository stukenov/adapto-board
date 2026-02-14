package com.playoutedge.server.services

import com.playoutedge.domain.enums.BindingStatus
import com.playoutedge.domain.enums.OverlaySourceType
import com.playoutedge.persistence.repositories.OverlayRepository
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import java.util.UUID

class OverlayPollService(
    private val overlayRepository: OverlayRepository,
    private val overlaySubscribers: OverlaySubscribers
) {
    private val logger = LoggerFactory.getLogger(OverlayPollService::class.java)
    private val httpClient = HttpClient()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val pollingJobs = mutableMapOf<String, Job>()
    private val json = Json { ignoreUnknownKeys = true }

    fun start() {
        scope.launch {
            while (isActive) {
                try {
                    refreshPollingJobs()
                } catch (e: Exception) {
                    logger.error("Error refreshing polling jobs", e)
                }
                delay(30_000) // Check for new bindings every 30s
            }
        }
    }

    private data class PollBinding(
        val id: String,
        val configStr: String,
        val channelId: UUID,
        val tenantId: UUID
    )

    private suspend fun refreshPollingJobs() {
        val activeBindings = newSuspendedTransaction {
            overlayRepository.findAllRestPullBindings().map { binding ->
                PollBinding(
                    id = binding.id.value.toString(),
                    configStr = binding.sourceConfigJson.toString(),
                    channelId = binding.channel.id.value,
                    tenantId = binding.tenant.id.value
                )
            }
        }

        val activeIds = activeBindings.map { it.id }.toSet()

        // Stop jobs for removed bindings
        pollingJobs.keys.filter { it !in activeIds }.forEach { id ->
            pollingJobs.remove(id)?.cancel()
        }

        // Start jobs for new bindings
        activeBindings.forEach { binding ->
            val id = binding.id
            if (id !in pollingJobs || pollingJobs[id]?.isActive != true) {
                val config = try {
                    json.decodeFromString<JsonObject>(binding.configStr)
                } catch (_: Exception) { return@forEach }

                val url = config["url"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                val intervalSec = config["intervalSeconds"]?.jsonPrimitive?.intOrNull ?: 60
                val channelId = binding.channelId
                val tenantId = binding.tenantId

                pollingJobs[id] = scope.launch {
                    while (isActive) {
                        try {
                            val response = httpClient.get(url)
                            val body = response.bodyAsText()

                            // Extract data using jsonPath if specified
                            val jsonPath = config["jsonPath"]?.jsonPrimitive?.contentOrNull
                            val stateData = if (jsonPath != null) {
                                extractJsonPath(body, jsonPath)
                            } else {
                                body
                            }

                            overlayRepository.setState(
                                com.playoutedge.domain.tenant.TenantId(tenantId),
                                channelId,
                                stateData
                            )

                            // Notify subscribers
                            try {
                                val stateObj = json.decodeFromString<JsonObject>(stateData)
                                overlaySubscribers.broadcast(channelId, OverlayEvent.State(stateObj, System.currentTimeMillis()))
                            } catch (_: Exception) {}
                        } catch (e: Exception) {
                            logger.warn("REST pull failed for binding $id: ${e.message}")
                        }
                        delay(intervalSec * 1000L)
                    }
                }
            }
        }
    }

    private fun extractJsonPath(jsonStr: String, path: String): String {
        // Simple JSON path extraction: $.data.value -> navigate object keys
        try {
            var current: JsonElement = json.parseToJsonElement(jsonStr)
            val parts = path.removePrefix("$").removePrefix(".").split(".")
            for (part in parts) {
                if (part.isBlank()) continue
                current = (current as? JsonObject)?.get(part) ?: return jsonStr
            }
            return current.toString()
        } catch (_: Exception) {
            return jsonStr
        }
    }

    fun stop() {
        scope.cancel()
        httpClient.close()
    }
}
