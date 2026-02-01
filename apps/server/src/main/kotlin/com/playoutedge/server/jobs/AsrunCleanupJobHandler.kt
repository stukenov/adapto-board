package com.playoutedge.server.jobs

import com.playoutedge.persistence.repositories.AsrunRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

class AsrunCleanupJobHandler(
    private val asrunRepo: AsrunRepository
) : JobHandler {
    override val type = "ASRUN_CLEANUP"

    override suspend fun handle(payload: JsonObject): JobResult {
        val retentionDays = payload["retentionDays"]?.jsonPrimitive?.int ?: 30

        return try {
            val deleted = asrunRepo.deleteOlderThan(retentionDays)
            JobResult.Success
        } catch (e: Exception) {
            JobResult.Failure(e.message ?: "As-run cleanup failed")
        }
    }
}
