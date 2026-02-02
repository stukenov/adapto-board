# Phase 02: Repository Layer Tests

This phase adds comprehensive test coverage for all 13 repository implementations in the persistence layer. Repositories are the foundation of data access - thorough testing here prevents data corruption and ensures reliable CRUD operations across all entities.

## Tasks

- [ ] Create test utilities and base class for repository testing:
  - Create `/libs/persistence/src/test/kotlin/com/playoutedge/persistence/RepositoryTestBase.kt`:
    - Extend existing `DatabaseTestBase` pattern
    - Add helper methods for creating test tenants, users, channels
    - Add transaction rollback support for test isolation
    - Add factory methods for common test entities
  - Create `/libs/persistence/src/test/kotlin/com/playoutedge/persistence/TestFixtures.kt`:
    - Define reusable test data factories for all entity types
    - Include valid and edge-case data variations

- [ ] Write tests for ChannelRepository:
  - Create `/libs/persistence/src/test/kotlin/com/playoutedge/persistence/repositories/ChannelRepositoryTest.kt`
  - Test create channel with all required fields
  - Test find by ID (existing and non-existing)
  - Test find all channels for tenant with pagination
  - Test update channel properties (name, status, settings)
  - Test archive/soft-delete behavior
  - Test tenant isolation (channel from tenant A not visible to tenant B)
  - Test filtering by status (active, paused, archived)

- [ ] Write tests for DeviceRepository:
  - Create `/libs/persistence/src/test/kotlin/com/playoutedge/persistence/repositories/DeviceRepositoryTest.kt`
  - Test device registration with enrollment code
  - Test find by ID and find by enrollment code
  - Test update device status (online, offline, maintenance)
  - Test channel assignment and unassignment
  - Test device listing with filters (status, channel, tenant)
  - Test last-seen timestamp updates
  - Test device metadata storage and retrieval

- [ ] Write tests for AssetRepository:
  - Create `/libs/persistence/src/test/kotlin/com/playoutedge/persistence/repositories/AssetRepositoryTest.kt`
  - Test asset creation with file metadata
  - Test asset versioning (create new version, get latest)
  - Test find by ID with version history
  - Test search by name and tags
  - Test filtering by type (video, image, audio)
  - Test soft-delete and permanent delete
  - Test storage path generation and retrieval

- [ ] Write tests for ScheduleRepository:
  - Create `/libs/persistence/src/test/kotlin/com/playoutedge/persistence/repositories/ScheduleRepositoryTest.kt`
  - Test schedule creation with time ranges
  - Test schedule version management (draft, published)
  - Test find schedules for channel by date range
  - Test schedule conflict detection
  - Test recurring schedule patterns
  - Test schedule entry ordering

- [ ] Write tests for OverlayRepository:
  - Create `/libs/persistence/src/test/kotlin/com/playoutedge/persistence/repositories/OverlayRepositoryTest.kt`
  - Test overlay profile CRUD operations
  - Test overlay binding creation and removal
  - Test find bindings by channel
  - Test find profiles by tenant
  - Test overlay template storage

- [ ] Write tests for remaining repositories:
  - Create `/libs/persistence/src/test/kotlin/com/playoutedge/persistence/repositories/AlertRepositoryTest.kt`:
    - Test alert creation with severity levels
    - Test alert acknowledgment and resolution
    - Test alert listing with filters
  - Create `/libs/persistence/src/test/kotlin/com/playoutedge/persistence/repositories/AsrunRepositoryTest.kt`:
    - Test as-run entry creation
    - Test retrieval by channel and time range
    - Test aggregation queries
  - Create `/libs/persistence/src/test/kotlin/com/playoutedge/persistence/repositories/AuditRepositoryTest.kt`:
    - Test audit log entry creation
    - Test filtering by user, action, entity
    - Test pagination and date range queries
  - Create `/libs/persistence/src/test/kotlin/com/playoutedge/persistence/repositories/WebhookLogRepositoryTest.kt`:
    - Test webhook delivery logging
    - Test retry tracking
    - Test cleanup of old entries

- [ ] Run all repository tests and verify coverage:
  - Execute: `./gradlew :libs:persistence:test`
  - Generate coverage report: `./gradlew :libs:persistence:jacocoTestReport`
  - Verify all tests pass
  - Target: 80%+ coverage for repository implementations
