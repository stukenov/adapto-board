# Phase 09: Job Handler Tests

This phase adds tests for background job handlers and schedulers. Jobs handle critical maintenance tasks like cleanup, data aggregation, and webhook delivery - testing ensures these run reliably without human intervention.

## Tasks

- [ ] Create job test infrastructure:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/jobs/JobTestBase.kt`:
    - Set up mock repositories for job handlers
    - Configure test clock for time-based testing
    - Add utilities for simulating job execution
    - Add helpers for verifying job side effects

- [ ] Write tests for JobScheduler:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/jobs/JobSchedulerTest.kt`
  - Test job registration and scheduling
  - Test job execution at scheduled times
  - Test job retry on failure
  - Test job cancellation
  - Test concurrent job execution handling
  - Test job persistence across restarts
  - Test job status tracking

- [ ] Write tests for CleanupJobHandler:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/jobs/CleanupJobHandlerTest.kt`
  - Test audit log cleanup based on retention period
  - Test webhook log cleanup based on retention period
  - Test no deletion of recent entries
  - Test batch deletion for large datasets
  - Test error handling during cleanup
  - Test cleanup metrics recording

- [ ] Write tests for AsrunCleanupJobHandler:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/jobs/AsrunCleanupJobHandlerTest.kt`
  - Test as-run entry cleanup based on age
  - Test tenant-specific retention policies
  - Test preservation of flagged entries
  - Test cleanup reporting
  - Test handling of empty datasets

- [ ] Write integration tests for job execution:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/jobs/JobIntegrationTest.kt`
  - Test end-to-end job execution with real database
  - Test job scheduler with multiple handlers
  - Test job failure recovery
  - Test job execution ordering
  - Test job dependencies (if any)

- [ ] Run all job tests and verify:
  - Execute: `./gradlew :apps:server:test --tests "*Job*"`
  - Verify all tests pass
  - Generate coverage report
  - Target: 85%+ coverage for job code
