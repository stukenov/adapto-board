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
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.time.measureTimedValue

sealed class WebhookResult {
    data class Success(val stateJson: JsonObject) : WebhookResult()
    data object InvalidSignature : WebhookResult()
    data object InvalidPayload : WebhookResult()
    data object BindingNotFound : WebhookResult()
    data object PayloadTooLarge : WebhookResult()
    data class InternalError(val message: String) : WebhookResult()
}

class WebhookService(
    private val webhookLogRepo: WebhookLogRepository,
    private val overlayService: OverlayService
) {
    companion object {
        private const val MAX_PAYLOAD_SIZE = 256 * 1024 // 256KB
        private const val SECRET_LENGTH = 32
    }

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun processWebhook(
        bindingId: UUID,
        signature: String?,
        payload: ByteArray
    ): WebhookResult {
        val startTime = Clock.System.now()

        // Check payload size
        if (payload.size > MAX_PAYLOAD_SIZE) {
            logWebhook(bindingId, 413, startTime, payload.size, "Payload too large")
            return WebhookResult.PayloadTooLarge
        }

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

        // Update overlay state
        try {
            val tenantId = binding.tenant.id.value
            val channelId = binding.channel.id.value
            overlayService.setState(
                com.playoutedge.domain.tenant.TenantId(tenantId),
                channelId,
                payloadJson
            )
            logWebhook(bindingId, 200, startTime, payload.size, null)
            return WebhookResult.Success(payloadJson)
        } catch (e: Exception) {
            logWebhook(bindingId, 500, startTime, payload.size, e.message)
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
        error: String?
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
