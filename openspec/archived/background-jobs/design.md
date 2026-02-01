# Background Jobs — Design

## Overview

Postgres-based job queue without external message brokers. Jobs table already exists in OpsTables.kt.

## Database

### Existing: Jobs table
```kotlin
object Jobs : UUIDTable("jobs") {
    val type = varchar("type", 100)
    val payloadJson = jsonbColumn("payload_json")
    val status = enumerationByName<JobStatus>("status", 20)
    val nextRunAt = timestamp("next_run_at")
    val attempts = integer("attempts").default(0)
    val maxAttempts = integer("max_attempts").default(3)
    val lockedBy = varchar("locked_by", 100).nullable()
    val lockedAt = timestamp("locked_at").nullable()
    val completedAt = timestamp("completed_at").nullable()
    val errorMessage = text("error_message").nullable()
    val createdAt = timestamp("created_at")
}
```

## Components

### 1. JobEntity
Create entity class for Jobs table.

### 2. JobRepository
```kotlin
interface JobRepository {
    suspend fun create(type: String, payload: JsonObject, runAt: Instant): JobEntity
    suspend fun findPending(limit: Int): List<JobEntity>
    suspend fun lock(jobId: UUID, workerId: String): Boolean
    suspend fun complete(jobId: UUID): JobEntity?
    suspend fun fail(jobId: UUID, error: String): JobEntity?
    suspend fun reschedule(jobId: UUID, nextRunAt: Instant): JobEntity?
    suspend fun unlockStale(timeout: Duration): Int
}
```

### 3. JobScheduler
```kotlin
class JobScheduler(jobRepo, coroutineScope) {
    private val handlers = mutableMapOf<String, JobHandler>()

    fun register(type: String, handler: JobHandler)
    fun start()
    fun stop()
}
```

### 4. Job Handlers
- CleanupJobHandler: Audit log, webhook logs, soft-deleted assets cleanup
- HealthCheckJobHandler: Overlay binding health checks

## Job Types

| Type | Description | Default Interval |
|------|-------------|------------------|
| CLEANUP_AUDIT | Clean old audit logs | 24h |
| CLEANUP_WEBHOOK_LOGS | Clean old webhook logs | 24h |
| HEALTH_CHECK_BINDINGS | Check overlay binding health | 5m |

## Admin API

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/admin/jobs | List jobs |
| GET | /api/admin/jobs/{id} | Get job details |
