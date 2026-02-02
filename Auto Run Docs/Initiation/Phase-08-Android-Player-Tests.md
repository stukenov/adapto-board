# Phase 08: Android Player Tests

This phase adds unit and integration tests for the Android TV player application. The player is the user-facing component - testing ensures reliable playback, proper enrollment, and correct overlay rendering on TV devices.

## Tasks

- [ ] Set up Android test infrastructure:
  - Configure test dependencies in `/apps/player-androidtv/build.gradle.kts`:
    - Add JUnit 4/5 for unit tests
    - Add MockK for Kotlin mocking
    - Add kotlinx-coroutines-test for coroutine testing
    - Add Turbine for Flow testing
  - Create `/apps/player-androidtv/src/test/kotlin/com/playoutedge/player/TestUtils.kt`:
    - Test dispatchers configuration
    - Mock factories for common dependencies
    - Helper methods for Flow testing

- [ ] Write tests for PlaylistManager and PlaylistCalculator:
  - Create `/apps/player-androidtv/src/test/kotlin/com/playoutedge/player/playlist/PlaylistManagerTest.kt`:
    - Test playlist loading from server
    - Test playlist refresh scheduling
    - Test handling of empty playlists
    - Test fallback to default content
    - Test playlist update notifications
  - Create `/apps/player-androidtv/src/test/kotlin/com/playoutedge/player/playlist/PlaylistCalculatorTest.kt`:
    - Test current item calculation based on time
    - Test next item prediction
    - Test looping behavior
    - Test schedule boundary handling

- [ ] Write tests for PlayerManager:
  - Create `/apps/player-androidtv/src/test/kotlin/com/playoutedge/player/playback/PlayerManagerTest.kt`
  - Test video playback start
  - Test playback pause and resume
  - Test playback seek
  - Test asset transition (video to video)
  - Test error handling and retry logic
  - Test playback state changes
  - Test ExoPlayer callbacks handling
  - Test fallback hierarchy (channel default, global default)

- [ ] Write tests for EnrollmentViewModel:
  - Create `/apps/player-androidtv/src/test/kotlin/com/playoutedge/player/enrollment/EnrollmentViewModelTest.kt`
  - Test initial state (unenrolled)
  - Test enrollment code display
  - Test successful enrollment flow
  - Test enrollment error handling
  - Test enrollment retry on failure
  - Test persistence of enrollment state
  - Test automatic enrollment check on startup

- [ ] Write tests for network clients:
  - Create `/apps/player-androidtv/src/test/kotlin/com/playoutedge/player/network/ApiClientTest.kt`:
    - Test API request construction
    - Test authentication header injection
    - Test response parsing
    - Test error response handling
    - Test network timeout handling
  - Create `/apps/player-androidtv/src/test/kotlin/com/playoutedge/player/network/TokenStorageTest.kt`:
    - Test token save and retrieve
    - Test token refresh
    - Test token expiration handling
    - Test secure storage encryption
  - Create `/apps/player-androidtv/src/test/kotlin/com/playoutedge/player/network/NetworkMonitorTest.kt`:
    - Test online/offline detection
    - Test network type detection (WiFi, Ethernet)
    - Test connectivity change callbacks

- [ ] Write tests for OverlaySseClient:
  - Create `/apps/player-androidtv/src/test/kotlin/com/playoutedge/player/overlay/OverlaySseClientTest.kt`
  - Test SSE connection establishment
  - Test message parsing
  - Test overlay update handling
  - Test reconnection on disconnect
  - Test backoff strategy on repeated failures
  - Test connection cleanup on stop

- [ ] Write tests for telemetry components:
  - Create `/apps/player-androidtv/src/test/kotlin/com/playoutedge/player/telemetry/HeartbeatManagerTest.kt`:
    - Test heartbeat scheduling
    - Test heartbeat payload construction
    - Test heartbeat failure handling
  - Create `/apps/player-androidtv/src/test/kotlin/com/playoutedge/player/telemetry/AsrunCollectorTest.kt`:
    - Test playback event collection
    - Test event batching
    - Test event transmission
    - Test offline queueing

- [ ] Write tests for ConfigManager:
  - Create `/apps/player-androidtv/src/test/kotlin/com/playoutedge/player/config/ConfigManagerTest.kt`
  - Test config loading from server
  - Test config caching
  - Test config update handling
  - Test default values when server unavailable
  - Test config validation

- [ ] Run all Android tests and verify:
  - Execute: `./gradlew :apps:player-androidtv:test`
  - Review test results
  - Generate coverage report
  - Target: 70%+ coverage for player code
