# Phase 03: Service Layer Tests

This phase adds comprehensive unit tests for all 12 server services. Services contain core business logic - testing them ensures the application behaves correctly regardless of how it's accessed (API, admin UI, or scheduled jobs).

## Tasks

- [ ] Create service test infrastructure:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/ServiceTestBase.kt`:
    - Set up MockK for mocking repositories
    - Configure coroutine test dispatchers
    - Add helper methods for common assertions
  - Create mock factories for all repository interfaces:
    - `MockRepositoryFactory.kt` with methods to create configured mocks
    - Include success and failure scenarios for each repository method

- [ ] Write tests for ChannelService:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/ChannelServiceTest.kt`
  - Test createChannel: valid input, duplicate name handling, quota limits
  - Test findChannel: existing, non-existing, wrong tenant
  - Test updateChannel: partial updates, name change, settings change
  - Test pauseChannel and resumeChannel: state transitions, already in state
  - Test archiveChannel: cascading effects, already archived
  - Test listChannels: pagination, sorting, filtering by status
  - Test channel quotas and limits per tenant

- [ ] Write tests for DeviceService:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/DeviceServiceTest.kt`
  - Test registerDevice: enrollment flow, code validation, duplicate registration
  - Test findDevice: by ID, by enrollment code, non-existing
  - Test updateDeviceStatus: online/offline transitions, timestamp updates
  - Test assignChannel: valid assignment, channel not found, already assigned
  - Test unassignChannel: successful unassign, device not assigned
  - Test listDevices: filtering, pagination, sorting
  - Test generateEnrollmentCode: uniqueness, expiration

- [ ] Write tests for ScheduleService:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/ScheduleServiceTest.kt`
  - Test createSchedule: valid schedule, time validation, asset validation
  - Test updateSchedule: version management, draft vs published
  - Test publishSchedule: validation, atomic updates
  - Test getScheduleForChannel: date range queries, fallback behavior
  - Test deleteSchedule: cascade handling, published schedule restrictions
  - Test schedule conflict detection and resolution

- [ ] Write tests for PlaylistService:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/PlaylistServiceTest.kt`
  - Test generatePlaylist: from schedule, with assets
  - Test playlist fallback hierarchy: channel default, global default
  - Test calculatePlaylistTiming: duration calculations, gaps
  - Test getPlaylistForDevice: channel assignment, time-based selection
  - Test URL generation for assets in playlist
  - Test handling of missing or unavailable assets

- [ ] Write tests for OverlayService:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/OverlayServiceTest.kt`
  - Test createOverlayProfile: template validation, default settings
  - Test bindOverlayToChannel: valid binding, profile not found
  - Test unbindOverlay: successful unbind, not bound
  - Test subscriber management: add subscriber, remove subscriber, broadcast
  - Test concurrent overlay updates
  - Test overlay template rendering

- [ ] Write tests for AsrunService, AuditService, AlertService:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/AsrunServiceTest.kt`:
    - Test logPlaybackStart and logPlaybackEnd
    - Test getAsrunEntries with filters
    - Test aggregation and reporting
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/AuditServiceTest.kt`:
    - Test logAction for various action types
    - Test getAuditLogs with filtering
    - Test user action attribution
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/AlertServiceTest.kt`:
    - Test createAlert with different severities
    - Test acknowledgeAlert and resolveAlert
    - Test getActiveAlerts and alert filtering

- [ ] Write tests for WebhookService, DeviceActionService, MaintenanceService:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/WebhookServiceTest.kt`:
    - Test registerWebhook and triggerWebhook
    - Test retry logic for failed deliveries
    - Test payload construction
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/DeviceActionServiceTest.kt`:
    - Test queueAction for device
    - Test getActionsForDevice
    - Test action completion handling
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/MaintenanceServiceTest.kt`:
    - Test enterMaintenanceMode and exitMaintenanceMode
    - Test isInMaintenanceMode checks
    - Test graceful handling of in-flight requests

- [ ] Write tests for CompatibilityService:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/services/CompatibilityServiceTest.kt`
  - Test version parsing and comparison
  - Test compatibility matrix lookups
  - Test checkCompatibility for various client versions
  - Test handling of unknown versions

- [ ] Run all service tests and verify coverage:
  - Execute: `./gradlew :apps:server:test --tests "*ServiceTest*"`
  - Generate coverage: `./gradlew :apps:server:jacocoTestReport`
  - Verify all tests pass
  - Target: 85%+ coverage for service layer
