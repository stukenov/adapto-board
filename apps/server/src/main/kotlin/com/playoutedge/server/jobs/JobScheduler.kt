package com.playoutedge.server.jobs

import com.playoutedge.persistence.repositories.JobRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class JobScheduler(
    private val jobRepo: JobRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val handlers = mutableMapOf<String, JobHandler>()
    private var schedulerJob: Job? = null
    private val workerId = UUID.randomUUID().toString()

    fun register(handler: JobHandler) {
        handlers[handler.type] = handler
    }

    fun start() {
        schedulerJob = scope.launch {
            // Unlock stale jobs on startup
            jobRepo.unlockStale(10.minutes)

            while (isActive) {
                try {
                    processPendingJobs()
                } catch (e: Exception) {
                    // Log error but continue
                }
                delay(5.seconds)
            }
        }
    }

    suspend fun stop() {
        schedulerJob?.cancelAndJoin()
    }

    private suspend fun processPendingJobs() {
        val pendingJobs = jobRepo.findPending(10)

        for (job in pendingJobs) {
            val handler = handlers[job.type] ?: continue

            if (!jobRepo.lock(job.id.value, workerId)) {
                continue
            }

            try {
                val result = handler.handle(job.payloadJson)

                when (result) {
                    is JobResult.Success -> {
                        jobRepo.complete(job.id.value)
                    }
                    is JobResult.Failure -> {
                        jobRepo.fail(job.id.value, result.error)
                    }
                    is JobResult.Reschedule -> {
                        val nextRun = Clock.System.now() + result.delayMinutes.minutes
                        jobRepo.reschedule(job.id.value, nextRun)
                    }
                }
            } catch (e: Exception) {
                jobRepo.fail(job.id.value, e.message ?: "Unknown error")
            }
        }
    }
}
