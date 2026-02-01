package com.playoutedge.persistence.repositories

import com.playoutedge.persistence.entities.JobEntity
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import java.util.UUID
import kotlin.time.Duration

interface JobRepository {
    suspend fun create(type: String, payload: JsonObject, runAt: Instant): JobEntity
    suspend fun findPending(limit: Int): List<JobEntity>
    suspend fun findById(jobId: UUID): JobEntity?
    suspend fun findAll(limit: Int): List<JobEntity>
    suspend fun lock(jobId: UUID, workerId: String): Boolean
    suspend fun complete(jobId: UUID): JobEntity?
    suspend fun fail(jobId: UUID, error: String): JobEntity?
    suspend fun reschedule(jobId: UUID, nextRunAt: Instant): JobEntity?
    suspend fun unlockStale(timeout: Duration): Int
}
