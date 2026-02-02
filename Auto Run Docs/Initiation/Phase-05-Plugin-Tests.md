# Phase 05: Plugin Tests

This phase adds unit tests for all 8 Ktor plugins that handle cross-cutting concerns like authentication, rate limiting, multi-tenancy, and error handling. These plugins are critical infrastructure - bugs here affect every request.

## Tasks

- [ ] Create plugin test infrastructure:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/plugins/PluginTestBase.kt`:
    - Set up minimal Ktor test application
    - Add helper methods for installing individual plugins
    - Configure mock dependencies for each plugin
  - Add test utilities for:
    - Generating test JWT tokens with various claims
    - Creating mock request/response pipelines
    - Inspecting plugin-added attributes

- [ ] Write tests for TenantPlugin:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/plugins/TenantPluginTest.kt`
  - Test tenant extraction from admin JWT claims
  - Test tenant extraction from device JWT claims
  - Test missing tenant claim handling
  - Test invalid tenant ID handling
  - Test tenant context propagation to downstream handlers
  - Test requests without authentication (public routes)

- [ ] Write tests for AdminSessionPlugin:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/plugins/AdminSessionPluginTest.kt`
  - Test valid session cookie parsing
  - Test expired session handling
  - Test missing session cookie redirect
  - Test invalid session cookie rejection
  - Test session refresh on activity
  - Test logout and session invalidation

- [ ] Write tests for RateLimitPlugin:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/plugins/RateLimitPluginTest.kt`
  - Test requests within rate limit pass through
  - Test requests exceeding rate limit return 429
  - Test rate limit window reset behavior
  - Test per-user/per-device isolation
  - Test different rate limits for different endpoints
  - Test rate limit headers in response (X-RateLimit-*)

- [ ] Write tests for JwtAuth plugin:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/plugins/JwtAuthPluginTest.kt`
  - Test valid admin token acceptance
  - Test valid device token acceptance
  - Test expired token rejection (401)
  - Test malformed token rejection (401)
  - Test token with wrong issuer rejection
  - Test token with wrong audience rejection
  - Test missing token on protected route (401)
  - Test role claim extraction

- [ ] Write tests for RbacPlugin:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/plugins/RbacPluginTest.kt`
  - Test admin role has full access
  - Test operator role has limited access
  - Test viewer role has read-only access
  - Test device role can only access device endpoints
  - Test permission inheritance (admin includes operator permissions)
  - Test 403 response for insufficient permissions
  - Test permission checks on specific resources

- [ ] Write tests for ErrorHandling plugin:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/plugins/ErrorHandlingPluginTest.kt`
  - Test validation errors return 400 with details
  - Test not found errors return 404
  - Test authentication errors return 401
  - Test authorization errors return 403
  - Test rate limit errors return 429
  - Test internal errors return 500 (without leaking details)
  - Test error response JSON structure consistency
  - Test exception to status code mapping

- [ ] Write tests for RequestIdPlugin and MaintenancePlugin:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/plugins/RequestIdPluginTest.kt`:
    - Test request ID generation for each request
    - Test request ID header propagation
    - Test request ID in response headers
    - Test request ID in log context
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/plugins/MaintenancePluginTest.kt`:
    - Test requests blocked during maintenance (503)
    - Test health check still accessible
    - Test maintenance mode toggle
    - Test response includes Retry-After header

- [ ] Run all plugin tests and verify coverage:
  - Execute: `./gradlew :apps:server:test --tests "*PluginTest*"`
  - Generate coverage report
  - Verify all tests pass
  - Target: 90%+ coverage for plugin code (critical infrastructure)
