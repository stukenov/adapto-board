package com.playoutedge.persistence.repositories

import com.playoutedge.persistence.entities.WebhookLogEntity
import java.util.UUID

interface WebhookLogRepository {
    suspend fun create(log: CreateWebhookLog): WebhookLogEntity
    suspend fun findByBinding(bindingId: UUID, limit: Int): List<WebhookLogEntity>
    suspend fun deleteOldLogs(retentionDays: Int): Long
}

data class CreateWebhookLog(
    val bindingId: UUID,
    val statusCode: Int,
    val latencyMs: Int,
    val payloadSize: Int,
    val error: String? = null
)
