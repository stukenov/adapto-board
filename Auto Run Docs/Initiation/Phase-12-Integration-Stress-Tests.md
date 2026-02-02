# Phase 12: Integration and Stress Tests

This phase adds integration tests that verify multiple components working together, plus stress tests that ensure the system handles load gracefully. These tests catch issues that unit tests miss - race conditions, connection leaks, and performance bottlenecks.

## Tasks

- [ ] Create integration test infrastructure:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/integration/IntegrationTestBase.kt`:
    - Full application setup with real database
    - Test data seeding and cleanup
    - HTTP client for API calls
    - Utilities for waiting on async operations
  - Configure separate Gradle task for integration tests:
    ```kotlin
    tasks.register<Test>("integrationTest") {
        useJUnitPlatform {
            includeTags("integration")
        }
        shouldRunAfter(tasks.test)
    }
    ```

- [ ] Write integration tests for complete user flows:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/integration/ChannelWorkflowTest.kt`:
    - Create channel → Add schedule → Upload asset → Assign to schedule → Publish
    - Verify playlist is generated correctly
    - Test channel pause/resume affects playlist
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/integration/DeviceEnrollmentTest.kt`:
    - Generate enrollment code → Device enrolls → Device authenticates
    - Assign device to channel → Verify device receives playlist
    - Test device status updates propagate correctly
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/integration/OverlayWorkflowTest.kt`:
    - Create overlay profile → Bind to channel → Trigger update
    - Verify SSE subscribers receive updates
    - Test overlay unbinding cleanup

- [ ] Write stress tests for concurrent operations:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/stress/ConcurrencyTest.kt`:
    - Test 100 concurrent API requests
    - Test 50 concurrent database operations
    - Test 20 concurrent file uploads
    - Verify no deadlocks or connection pool exhaustion
    - Measure response time degradation under load
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/stress/OverlaySubscriberTest.kt`:
    - Test 100 concurrent SSE connections
    - Test broadcast to many subscribers
    - Test subscriber cleanup on disconnect
    - Verify memory doesn't grow unbounded

- [ ] Write database stress tests:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/stress/DatabaseStressTest.kt`:
    - Test large batch inserts (10,000 as-run entries)
    - Test complex queries with large datasets
    - Test connection pool under sustained load
    - Test transaction isolation under concurrency
    - Measure query performance with indexes

- [ ] Write API stress tests:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/stress/ApiStressTest.kt`:
    - Test rate limiter behavior under burst traffic
    - Test authentication under load
    - Test file upload with many concurrent requests
    - Verify error handling doesn't leak resources

- [ ] Write memory and resource leak tests:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/stress/ResourceLeakTest.kt`:
    - Test repeated request cycles don't increase memory
    - Test connection cleanup after errors
    - Test file handle cleanup after uploads
    - Test SSE connection cleanup on disconnect

- [ ] Run all integration and stress tests:
  - Execute integration tests: `./gradlew integrationTest`
  - Execute stress tests: `./gradlew test --tests "*Stress*"`
  - Document any failures or performance issues
  - Add thresholds for acceptable performance
  - Verify all tests pass consistently (run 3 times)
