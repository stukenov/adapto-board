package com.playoutedge.server.jobs

import com.playoutedge.persistence.repositories.AuditRepository
import com.playoutedge.persistence.repositories.WebhookLogRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

class CleanupJobHandler(
    private val auditRepo: AuditRepository,
    private val webhookLogRepo: WebhookLogRepository
) : JobHandler {
    override val type = "CLEANUP"

    override suspend fun handle(payload: JsonObject): JobResult {
        val target = payload["target"]?.jsonPrimitive?.content ?: return JobResult.Failure("Missing target")
        val retentionDays = payload["retentionDays"]?.jsonPrimitive?.int ?: 7

        return try {
            when (target) {
                "audit" -> {
                    val deleted = auditRepo.deleteOlderThan(retentionDays)
                    JobResult.Success
                }
                "webhook_logs" -> {
                    val deleted = webhookLogRepo.deleteOldLogs(retentionDays)
                    JobResult.Success
                }
                else -> JobResult.Failure("Unknown cleanup target: $target")
            }
        } catch (e: Exception) {
            JobResult.Failure(e.message ?: "Cleanup failed")
        }
    }
}
