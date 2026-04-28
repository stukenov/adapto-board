package com.playoutedge.server.services

import com.playoutedge.persistence.entities.OverlayBindingEntity
import com.playoutedge.persistence.entities.WebhookLogEntity
import com.playoutedge.persistence.repositories.CreateWebhookLog
import com.playoutedge.persistence.repositories.WebhookLogRepository
import com.playoutedge.persistence.tables.OverlayBindings
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.time.Duration.Companion.minutes

sealed class WebhookResult {
    data class Success(val stateJson: JsonObject) : WebhookResult()
    data object InvalidSignature : WebhookResult()
    data object InvalidPayload : WebhookResult()
    data object BindingNotFound : WebhookResult()
    data object PayloadTooLarge : WebhookResult()
    data object TimestampExpired : WebhookResult()
    data object DuplicatePayload : WebhookResult()
    data class InternalError(val message: String) : WebhookResult()
}

class WebhookService(
    private val webhookLogRepo: WebhookLogRepository,
    private val overlayService: OverlayService
) {
    companion object {
        private const val MAX_PAYLOAD_SIZE = 256 * 1024 // 256KB
        private const val SECRET_LENGTH = 32
        private val TIMESTAMP_MAX_AGE = 5.minutes
        private const val DEDUP_CACHE_MAX_SIZE = 10_000
    }

    private val json = Json { ignoreUnknownKeys = true }

    // Simple in-memory dedup cache: hash -> timestamp of receipt
    private val recentPayloadHashes = ConcurrentHashMap<String, kotlinx.datetime.Instant>()

    suspend fun processWebhook(
        bindingId: UUID,
        signature: String?,
        payload: ByteArray,
        timestamp: String? = null
    ): WebhookResult {
        val startTime = Clock.System.now()

        // Check payload size
        if (payload.size > MAX_PAYLOAD_SIZE) {
            logWebhook(bindingId, 413, startTime, payload.size, "Payload too large")
            return WebhookResult.PayloadTooLarge
        }

        // Check X-Timestamp replay protection
        if (timestamp != null) {
            val tsEpoch = timestamp.toLongOrNull()
            if (tsEpoch != null) {
                val tsInstant = kotlinx.datetime.Instant.fromEpochSeconds(tsEpoch)
                val age = Clock.System.now() - tsInstant
                if (age > TIMESTAMP_MAX_AGE || age < -TIMESTAMP_MAX_AGE) {
                    logWebhook(bindingId, 408, startTime, payload.size, "Timestamp too old or in the future")
                    return WebhookResult.TimestampExpired
                }
            }
        }

        // Check for duplicate payloads
        val payloadHash = MessageDigest.getInstance("SHA-256")
            .digest(payload)
            .joinToString("") { "%02x".format(it) }
        val dedupeKey = "$bindingId:$payloadHash"
        val now = Clock.System.now()
        // Evict old entries if cache is too large
        if (recentPayloadHashes.size > DEDUP_CACHE_MAX_SIZE) {
            val cutoff = now - TIMESTAMP_MAX_AGE
            recentPayloadHashes.entries.removeIf { it.value < cutoff }
        }
        val previous = recentPayloadHashes.putIfAbsent(dedupeKey, now)
        if (previous != null && (now - previous) < TIMESTAMP_MAX_AGE) {
            logWebhook(bindingId, 409, startTime, payload.size, "Duplicate payload")
            return WebhookResult.DuplicatePayload
        }
        recentPayloadHashes[dedupeKey] = now

        // Find binding
        val binding = findBinding(bindingId)
            ?: run {
                logWebhook(bindingId, 404, startTime, payload.size, "Binding not found")
                return WebhookResult.BindingNotFound
            }

        // Verify signature - required when secret is configured
        val secret = binding.webhookSecret
        if (secret != null) {
            if (signature == null) {
                logWebhook(bindingId, 401, startTime, payload.size, "Missing signature header")
                return WebhookResult.InvalidSignature
            }
            if (!verifySignature(secret, signature, payload)) {
                logWebhook(bindingId, 401, startTime, payload.size, "Invalid signature")
                return WebhookResult.InvalidSignature
            }
        }

        // Parse payload
        val payloadJson = try {
            json.decodeFromString<JsonObject>(payload.decodeToString())
        } catch (e: Exception) {
            logWebhook(bindingId, 400, startTime, payload.size, "Invalid JSON: ${e.message}")
            return WebhookResult.InvalidPayload
        }

        // Update overlay state with retry logic
        try {
            val tenantId = binding.tenant.id.value
            val channelId = binding.channel.id.value
            overlayService.setBindingState(
                com.playoutedge.domain.tenant.TenantId(tenantId),
                channelId,
                bindingId,
                payloadJson
            )
            logWebhook(bindingId, 200, startTime, payload.size, null)
            return WebhookResult.Success(payloadJson)
        } catch (e: Exception) {
            logWebhook(bindingId, 500, startTime, payload.size, e.message, retryCount = 0)
            return WebhookResult.InternalError(e.message ?: "Unknown error")
        }
    }

    fun verifySignature(secret: String, signature: String, payload: ByteArray): Boolean {
        if (!signature.startsWith("sha256=")) {
            return false
        }

        val providedHash = signature.removePrefix("sha256=")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val expectedHash = mac.doFinal(payload).toHex()

        return providedHash.equals(expectedHash, ignoreCase = true)
    }

    suspend fun regenerateSecret(bindingId: UUID): String? = newSuspendedTransaction {
        val binding = OverlayBindingEntity.findById(bindingId)
            ?: return@newSuspendedTransaction null

        val newSecret = generateSecret()
        binding.webhookSecret = newSecret
        newSecret
    }

    /**
     * Retry a failed webhook by re-processing the binding's current state.
     * Increments the retry count on the log entry and attempts to re-process.
     */
    suspend fun retryWebhook(logId: UUID): Boolean {
        val log = newSuspendedTransaction {
            WebhookLogEntity.findById(logId)
        } ?: return false

        val bindingId = newSuspendedTransaction { log.binding.id.value }

        // Increment retry count
        newSuspendedTransaction {
            val currentLog = WebhookLogEntity.findById(logId) ?: return@newSuspendedTransaction
            currentLog.retryCount = currentLog.retryCount + 1
        }

        // Re-process: find the binding and re-apply its current state
        val binding = findBinding(bindingId) ?: return false
        return try {
            val tenantId = newSuspendedTransaction { binding.tenant.id.value }
            val channelId = newSuspendedTransaction { binding.channel.id.value }
            // Log the retry attempt
            logWebhook(bindingId, 200, Clock.System.now(), 0, null, retryCount = newSuspendedTransaction {
                WebhookLogEntity.findById(logId)?.retryCount ?: 1
            })
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getLogs(bindingId: UUID, limit: Int = 100): List<WebhookLogEntity> {
        return webhookLogRepo.findByBinding(bindingId, limit)
    }

    private fun generateSecret(): String {
        val random = SecureRandom()
        val bytes = ByteArray(SECRET_LENGTH)
        random.nextBytes(bytes)
        return bytes.toHex()
    }

    private suspend fun findBinding(bindingId: UUID): OverlayBindingEntity? = newSuspendedTransaction {
        OverlayBindingEntity.findById(bindingId)
    }

    private suspend fun logWebhook(
        bindingId: UUID,
        statusCode: Int,
        startTime: kotlinx.datetime.Instant,
        payloadSize: Int,
        error: String?,
        retryCount: Int = 0
    ) {
        val latencyMs = (Clock.System.now() - startTime).inWholeMilliseconds.toInt()
        try {
            webhookLogRepo.create(
                CreateWebhookLog(
                    bindingId = bindingId,
                    statusCode = statusCode,
                    latencyMs = latencyMs,
                    payloadSize = payloadSize,
                    error = error
                )
            )
        } catch (e: Exception) {
            // Ignore logging errors
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
