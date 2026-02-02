# Phase 04: API Routes Tests

This phase adds integration tests for all REST API endpoints. These tests verify request/response contracts, authentication, authorization, validation, and error handling - ensuring the API behaves correctly for external consumers.

## Tasks

- [ ] Create API test infrastructure:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/ApiTestBase.kt`:
    - Configure Ktor testApplication with all plugins
    - Set up test database with DatabaseTestContainer
    - Add helper methods for authenticated requests (JWT generation)
    - Add JSON parsing utilities for response validation
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/TestAuthHelper.kt`:
    - Generate valid admin JWT tokens for testing
    - Generate valid device JWT tokens for testing
    - Generate expired and malformed tokens for negative tests

- [ ] Write tests for ChannelsRoutes:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/ChannelsRoutesTest.kt`
  - Test GET /api/channels: list with pagination, filtering, sorting
  - Test GET /api/channels/{id}: existing, non-existing, wrong tenant
  - Test POST /api/channels: valid creation, validation errors, duplicate name
  - Test PUT /api/channels/{id}: partial update, full update, validation
  - Test DELETE /api/channels/{id}: soft delete, already deleted
  - Test POST /api/channels/{id}/pause and /resume: state transitions
  - Test authentication: missing token, invalid token, expired token
  - Test authorization: admin-only endpoints, tenant isolation

- [ ] Write tests for DevicesRoutes:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/DevicesRoutesTest.kt`
  - Test GET /api/devices: list with filters (status, channel)
  - Test GET /api/devices/{id}: device details
  - Test POST /api/devices/enroll: enrollment flow with code
  - Test PUT /api/devices/{id}: update device settings
  - Test POST /api/devices/{id}/assign: channel assignment
  - Test DELETE /api/devices/{id}/assign: channel unassignment
  - Test GET /api/enroll-codes: generate and list enrollment codes
  - Test device-specific authentication (device JWT vs admin JWT)

- [ ] Write tests for AssetsRoutes:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/AssetsRoutesTest.kt`
  - Test GET /api/assets: list with filters (type, tags, search)
  - Test GET /api/assets/{id}: asset details with versions
  - Test POST /api/assets: file upload (multipart), metadata
  - Test PUT /api/assets/{id}: metadata update
  - Test DELETE /api/assets/{id}: soft delete behavior
  - Test GET /api/assets/{id}/url: signed URL generation
  - Test file type validation (allowed/disallowed formats)
  - Test file size limits

- [ ] Write tests for SchedulesRoutes:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/SchedulesRoutesTest.kt`
  - Test GET /api/schedules: list schedules for channel
  - Test GET /api/schedules/{id}: schedule details with entries
  - Test POST /api/schedules: create schedule with entries
  - Test PUT /api/schedules/{id}: update schedule
  - Test POST /api/schedules/{id}/publish: publish draft
  - Test DELETE /api/schedules/{id}: delete schedule
  - Test schedule time validation and conflict detection

- [ ] Write tests for OverlayRoutes:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/OverlayRoutesTest.kt`
  - Test GET /api/overlays/profiles: list profiles
  - Test POST /api/overlays/profiles: create profile
  - Test PUT /api/overlays/profiles/{id}: update profile
  - Test GET /api/overlays/bindings: list bindings for channel
  - Test POST /api/overlays/bindings: bind profile to channel
  - Test DELETE /api/overlays/bindings/{id}: unbind profile

- [ ] Write tests for AlertsRoutes, AsrunRoutes, AuditRoutes:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/AlertsRoutesTest.kt`:
    - Test GET /api/alerts: list active alerts
    - Test POST /api/alerts/{id}/acknowledge
    - Test POST /api/alerts/{id}/resolve
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/AsrunRoutesTest.kt`:
    - Test GET /api/asrun: get as-run logs with date range
    - Test POST /api/asrun: log playback entry (device auth)
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/AuditRoutesTest.kt`:
    - Test GET /api/audit: list audit entries with filters
    - Test pagination and date range filtering

- [ ] Write tests for AuthRoutes and DeviceAuthRoutes:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/AuthRoutesTest.kt`:
    - Test POST /api/auth/login: valid credentials, invalid credentials
    - Test POST /api/auth/refresh: token refresh flow
    - Test POST /api/auth/logout: session invalidation
    - Test rate limiting on auth endpoints
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/DeviceAuthRoutesTest.kt`:
    - Test POST /api/devices/auth: device authentication
    - Test enrollment code validation
    - Test device token refresh

- [ ] Write tests for WebhookRoutes and StorageRoutes:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/WebhookRoutesTest.kt`:
    - Test GET /api/webhooks: list webhooks
    - Test POST /api/webhooks: register webhook
    - Test DELETE /api/webhooks/{id}: remove webhook
    - Test webhook payload validation
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/routes/StorageRoutesTest.kt`:
    - Test presigned URL generation
    - Test upload completion handling

- [ ] Run all API tests and verify coverage:
  - Execute: `./gradlew :apps:server:test --tests "*RoutesTest*"`
  - Generate coverage report
  - Verify all tests pass
  - Verify response codes match API contract
  - Target: 80%+ coverage for route handlers
