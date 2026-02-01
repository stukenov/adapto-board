# Background Jobs — Proposal

## Why

Фоновые задачи без внешних брокеров (Kafka/Rabbit) — принцип "меньше систем". Используем Postgres как job queue.

## What Changes

### Job Infrastructure
- Таблица `jobs` (type, payload, status, next_run_at, attempts, locked_by, locked_at)
- Scheduler внутри монолита
- Postgres advisory lock для distributed locking
- Retry с exponential backoff

### Job Types

#### Overlay REST Pull
- Polling внешних endpoints
- Configurable interval per binding
- Error tracking

#### Retention Cleanup
- Audit log cleanup (по retention policy)
- As-run cleanup
- Soft-deleted assets purge

#### Connector Health Check
- Проверка доступности REST pull endpoints
- Status update в DB

### Job Monitoring
- Метрики: execution time, failures, queue size
- Logs с job_id correlation
- Admin UI: job status view (R1)

### Concurrency Control
- Max concurrent jobs per type
- Advisory lock per job id/type
- Graceful shutdown

## Capabilities

### New Capabilities
- `job-scheduler`: Postgres-based job scheduling
- `job-overlay-pull`: Overlay REST pull polling
- `job-cleanup`: Retention cleanup jobs
- `job-monitoring`: Job execution metrics

## Impact

- `libs/domain/src/.../Job.kt`
- `libs/persistence/src/.../JobRepository.kt`
- `apps/server/src/.../jobs/JobScheduler.kt`
- `apps/server/src/.../jobs/OverlayPullJob.kt`
- `apps/server/src/.../jobs/CleanupJob.kt`
