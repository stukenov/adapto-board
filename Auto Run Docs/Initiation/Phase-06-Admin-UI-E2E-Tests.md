# Phase 06: Admin UI E2E Tests

This phase expands Playwright E2E test coverage for all admin panel features. E2E tests verify the complete user experience - from clicking buttons to seeing results - ensuring the SSR views and JavaScript interactions work correctly together.

## Tasks

- [ ] Organize E2E test structure and utilities:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/ui/E2ETestBase.kt`:
    - Centralize Playwright browser setup and teardown
    - Add login helper method for authenticated tests
    - Add screenshot capture on test failure
    - Add wait utilities for page loads and AJAX
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/ui/E2ETestData.kt`:
    - Define test data constants (test channel names, device codes)
    - Add methods to clean up test data after runs
    - Document required test user credentials

- [ ] Write E2E tests for authentication flows:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/ui/AuthE2ETest.kt`
  - Test login with valid credentials → redirects to dashboard
  - Test login with invalid credentials → shows error message
  - Test logout → redirects to login page
  - Test session expiration → redirects to login
  - Test forgot password link (if implemented)
  - Test protected routes redirect to login when not authenticated

- [ ] Write E2E tests for channel management:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/ui/ChannelE2ETest.kt`
  - Test channel list displays existing channels
  - Test create new channel: fill form, submit, verify in list
  - Test view channel details: click channel, see details page
  - Test edit channel: modify name/settings, save, verify changes
  - Test pause channel: click pause, verify status change
  - Test resume channel: click resume, verify status change
  - Test archive channel: confirm dialog, verify removed from active list
  - Test channel search/filter functionality

- [ ] Write E2E tests for device management:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/ui/DeviceE2ETest.kt`
  - Test device list displays registered devices
  - Test generate enrollment code: click button, see code displayed
  - Test device details: click device, see status and info
  - Test assign device to channel: select channel, save
  - Test unassign device from channel
  - Test device status indicators (online/offline)
  - Test device search/filter by status

- [ ] Write E2E tests for asset management:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/ui/AssetE2ETest.kt`
  - Test asset list displays uploaded assets
  - Test upload new asset: select file, fill metadata, upload
  - Test view asset details: thumbnail, duration, metadata
  - Test edit asset metadata: tags, description
  - Test delete asset: confirm dialog, verify removal
  - Test asset search by name and tags
  - Test filter assets by type (video, image, audio)

- [ ] Write E2E tests for schedule management:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/ui/ScheduleE2ETest.kt`
  - Test schedule list for channel
  - Test create schedule: add time slots, select assets
  - Test edit schedule: modify entries
  - Test publish schedule: verify status change
  - Test schedule preview/timeline view
  - Test schedule conflict warnings

- [ ] Write E2E tests for overlay management:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/ui/OverlayE2ETest.kt`
  - Test overlay profiles list
  - Test create overlay profile: template, settings
  - Test edit overlay profile
  - Test bind overlay to channel
  - Test unbind overlay from channel
  - Test overlay preview (if available)

- [ ] Write E2E tests for reports and settings:
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/ui/ReportsE2ETest.kt`:
    - Test as-run report generation
    - Test date range selection
    - Test export functionality (if implemented)
  - Create `/apps/server/src/test/kotlin/com/playoutedge/server/ui/SettingsE2ETest.kt`:
    - Test user settings page loads
    - Test profile update
    - Test password change flow

- [ ] Run all E2E tests and document coverage:
  - Ensure server is running: `make dev`
  - Run E2E tests: `RUN_UI_TESTS=true ./gradlew :apps:server:uiTest`
  - Review screenshots in `build/screenshots/`
  - Document any flaky tests for stabilization
  - Create list of untested UI flows for future work
