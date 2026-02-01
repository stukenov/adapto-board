package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.JobStatus
import com.playoutedge.persistence.entities.JobEntity
import com.playoutedge.persistence.repositories.JobRepository
import com.playoutedge.persistence.tables.Jobs
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class JobRepositoryImpl : JobRepository {

    override suspend fun create(type: String, payload: JsonObject, runAt: Instant): JobEntity =
        newSuspendedTransaction {
            JobEntity.new {
                this.type = type
                this.payloadJson = payload
                this.status = JobStatus.PENDING
                this.nextRunAt = runAt
                this.attempts = 0
                this.maxAttempts = 3
                this.createdAt = Clock.System.now()
            }
        }

    override suspend fun findPending(limit: Int): List<JobEntity> =
        newSuspendedTransaction {
            val now = Clock.System.now()
            JobEntity.find {
                (Jobs.status eq JobStatus.PENDING) and
                    (Jobs.nextRunAt lessEq now) and
                    (Jobs.lockedBy.isNull())
            }.orderBy(Jobs.nextRunAt to SortOrder.ASC)
                .limit(limit)
                .toList()
        }

    override suspend fun findById(jobId: UUID): JobEntity? =
        newSuspendedTransaction {
            JobEntity.findById(jobId)
        }

    override suspend fun findAll(limit: Int): List<JobEntity> =
        newSuspendedTransaction {
            JobEntity.all()
                .orderBy(Jobs.createdAt to SortOrder.DESC)
                .limit(limit)
                .toList()
        }

    override suspend fun lock(jobId: UUID, workerId: String): Boolean =
        newSuspendedTransaction {
            val now = Clock.System.now()
            val staleThreshold = now - 5.minutes

            // First get the job to read attempts
            val job = JobEntity.findById(jobId) ?: return@newSuspendedTransaction false

            val updated = Jobs.update({
                (Jobs.id eq jobId) and
                    (Jobs.status eq JobStatus.PENDING) and
                    (Jobs.lockedBy.isNull() or (Jobs.lockedAt less staleThreshold))
            }) {
                it[status] = JobStatus.RUNNING
                it[lockedBy] = workerId
                it[lockedAt] = now
                it[attempts] = job.attempts + 1
            }
            updated > 0
        }

    override suspend fun complete(jobId: UUID): JobEntity? =
        newSuspendedTransaction {
            val job = JobEntity.findById(jobId) ?: return@newSuspendedTransaction null
            job.status = JobStatus.COMPLETED
            job.completedAt = Clock.System.now()
            job.lockedBy = null
            job.lockedAt = null
            job
        }

    override suspend fun fail(jobId: UUID, error: String): JobEntity? =
        newSuspendedTransaction {
            val job = JobEntity.findById(jobId) ?: return@newSuspendedTransaction null

            if (job.attempts >= job.maxAttempts) {
                job.status = JobStatus.FAILED
            } else {
                job.status = JobStatus.PENDING
                // Exponential backoff: 1min, 4min, 16min
                val backoffMinutes = Math.pow(4.0, job.attempts.toDouble()).toInt()
                job.nextRunAt = Clock.System.now() + backoffMinutes.minutes
            }
            job.errorMessage = error
            job.lockedBy = null
            job.lockedAt = null
            job
        }

    override suspend fun reschedule(jobId: UUID, nextRunAt: Instant): JobEntity? =
        newSuspendedTransaction {
            val job = JobEntity.findById(jobId) ?: return@newSuspendedTransaction null
            job.nextRunAt = nextRunAt
            job.status = JobStatus.PENDING
            job
        }

    override suspend fun unlockStale(timeout: Duration): Int =
        newSuspendedTransaction {
            val cutoff = Clock.System.now() - timeout
            Jobs.update({
                (Jobs.status eq JobStatus.RUNNING) and
                    (Jobs.lockedAt.isNotNull()) and
                    (Jobs.lockedAt less cutoff)
            }) {
                it[status] = JobStatus.PENDING
                it[lockedBy] = null
                it[lockedAt] = null
            }
        }
}
