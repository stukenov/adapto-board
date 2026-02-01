# Phase 01: Test Infrastructure Foundation

This phase establishes a solid testing foundation by ensuring all existing tests pass, fixing any broken tests, and verifying the test infrastructure works correctly. By the end of this phase, you'll have a green test suite that serves as the baseline for all future development.

## Tasks

- [x] Verify test infrastructure and run existing tests:
  - Start the PostgreSQL database container with `make db-up` or `docker-compose up -d db`
  - Run all server unit tests with `./gradlew :apps:server:test`
  - Run library tests with `./gradlew :libs:auth:test :libs:storage:test :libs:persistence:test`
  - Document which tests pass and which fail in a summary
  - If any tests fail, note the specific failures for the next task

  **Test Results Summary (2026-02-02):**
  | Module | Tests | Failures | Duration | Status |
  |--------|-------|----------|----------|--------|
  | apps:server | 31 | 0 | 5.757s | ✅ PASS |
  | libs:auth | 21 | 0 | 3.342s | ✅ PASS |
  | libs:storage | 12 | 0 | 1.321s | ✅ PASS |
  | libs:persistence | 14 | 0 | 7.666s | ✅ PASS |
  | **TOTAL** | **78** | **0** | **18.086s** | **✅ ALL PASSING** |

  **Note:** The docker-compose service is named `postgres`, not `db`. Use `docker-compose up -d postgres` instead.

- [x] Fix any failing tests in the server module:
  - Review each failing test in `/apps/server/src/test/kotlin/`
  - Fix test code issues (assertions, setup, teardown)
  - Fix production code bugs if tests reveal actual issues
  - Re-run tests to confirm fixes: `./gradlew :apps:server:test`
  - All tests must pass before proceeding

  **Status (2026-02-02):** No failing tests to fix. All 31 server tests verified passing via `./gradlew :apps:server:test` - BUILD SUCCESSFUL.

- [x] Fix any failing tests in library modules:
  - Review failing tests in `/libs/auth/src/test/kotlin/`
  - Review failing tests in `/libs/storage/src/test/kotlin/`
  - Review failing tests in `/libs/persistence/src/test/kotlin/`
  - Fix issues and re-run: `./gradlew :libs:auth:test :libs:storage:test :libs:persistence:test`
  - All library tests must pass before proceeding

  **Status (2026-02-02):** No failing tests to fix. All library tests verified passing:
  | Module | Tests | Failures | Status |
  |--------|-------|----------|--------|
  | libs:auth | 21 | 0 | ✅ PASS |
  | libs:storage | 12 | 0 | ✅ PASS |
  | libs:persistence | 14 | 0 | ✅ PASS |
  | **TOTAL** | **47** | **0** | **✅ ALL PASSING** |

  Note: Some deprecation warnings present in persistence module (`limit()` function deprecated) - not blocking.

- [x] Verify E2E test infrastructure with Playwright:
  - Ensure Playwright is installed: `cd apps/server && npx playwright install chromium`
  - Start the server locally: `make dev` or `./gradlew :apps:server:run`
  - Run UI tests: `RUN_UI_TESTS=true ./gradlew :apps:server:uiTest`
  - Document any E2E test failures for follow-up phases
  - Note: E2E tests may have environmental issues - document but don't block on these

  **E2E Test Results Summary (2026-02-02):**

  Playwright infrastructure verified successfully. Browsers were auto-downloaded on first run:
  - Chromium 131.0.6778.33 (v1148)
  - Firefox 132.0 (v1466)
  - Webkit 18.2 (v2104)
  - FFMPEG (v1010)

  | Test Suite | Tests | Failures | Skipped | Duration | Status |
  |------------|-------|----------|---------|----------|--------|
  | AdminDeepE2ETest | 18 | 0 | 0 | 5m 48s | ✅ PASS |
  | AdminE2ETest | 22 | 0 | 0 | 26.5s | ✅ PASS |
  | AdminUITest | 12 | 0 | 0 | 7.3s | ✅ PASS |
  | OperatorWorkdayE2ETest | 32 | 0 | 0 | 31.2s | ✅ PASS |
  | **TOTAL** | **84** | **0** | **0** | **6m 54s** | **✅ ALL PASSING** |

  **Notes:**
  - Playwright uses Java API (com.microsoft.playwright:playwright:1.49.0), not npm package
  - Browser installation is automatic via `driver-bundle` dependency
  - UI tests require `RUN_UI_TESTS=true` environment variable
  - Tests run against server at `http://localhost:8080` (or `TEST_BASE_URL` env var)
  - Some timeout warnings in logs for element visibility, but tests pass

- [x] Create test coverage baseline report:
  - Add JaCoCo plugin configuration to `apps/server/build.gradle.kts` if not present:
    ```kotlin
    plugins {
        jacoco
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
    ```
  - Run tests with coverage: `./gradlew :apps:server:test :apps:server:jacocoTestReport`
  - Coverage report will be at `apps/server/build/reports/jacoco/test/html/index.html`
  - Note the current coverage percentage as baseline

  **Coverage Baseline Report (2026-02-02):**

  JaCoCo plugin configured and coverage report generated successfully.

  | Metric | Covered | Total | Coverage |
  |--------|---------|-------|----------|
  | Instructions | 4,740 | 52,975 | **8%** |
  | Branches | 120 | 2,381 | **5%** |
  | Lines | 864 | 8,717 | **9%** |
  | Methods | 126 | 1,389 | **9%** |
  | Classes | 47 | 383 | **12%** |

  **Top Covered Packages:**
  | Package | Instruction Coverage |
  |---------|---------------------|
  | com.playoutedge.server.views.channels | 72% |
  | com.playoutedge.server.views | 57% |
  | com.playoutedge.server.views.auth | 44% |
  | com.playoutedge.server.plugins | 28% |

  **Uncovered Packages (0%):**
  - com.playoutedge.server.services
  - com.playoutedge.server.views.overlay
  - com.playoutedge.server.views.settings
  - com.playoutedge.server.views.onboarding
  - com.playoutedge.server.views.reports
  - com.playoutedge.server.views.devices
  - com.playoutedge.server.views.assets
  - com.playoutedge.server.routes.player
  - com.playoutedge.server.views.home
  - com.playoutedge.server.jobs
  - com.playoutedge.server (Application entry point)

  **Report Location:** `apps/server/build/reports/jacoco/test/html/index.html`

- [x] Verify full project build succeeds:
  - Run complete build: `./gradlew build -x test` (excluding tests which we already verified)
  - Run with tests: `./gradlew build`
  - Fix any compilation errors or warnings
  - Ensure both server and Android modules compile successfully
  - Document the final state: all tests passing, build successful

  **Build Verification (2026-02-02):**

  Initial `./gradlew build -x test` failed with 2 Android lint errors:

  | Error | File | Issue |
  |-------|------|-------|
  | MissingSuperCall | MainActivity.kt:248 | `onBackPressed()` not calling super |
  | MissingTvBanner | AndroidManifest.xml:18 | Missing TV banner for Leanback launcher |

  **Fixes Applied:**
  1. Added `@Suppress("MissingSuperCall")` to `onBackPressed()` in `MainActivity.kt` - intentionally blocking back button in kiosk mode
  2. Created `app_banner.xml` (320x180dp vector drawable) for TV home screen
  3. Added `android:banner="@drawable/app_banner"` to AndroidManifest.xml

  **Final Build Results:**
  | Build Type | Command | Result |
  |------------|---------|--------|
  | Without tests | `./gradlew build -x test` | ✅ BUILD SUCCESSFUL |
  | With all tests | `./gradlew build` | ✅ BUILD SUCCESSFUL |

  **Test Summary (All Modules):**
  | Module | Tests | Status |
  |--------|-------|--------|
  | apps:server | 31 | ✅ PASS |
  | libs:auth | 21 | ✅ PASS |
  | libs:storage | 12 | ✅ PASS |
  | libs:persistence | 14 | ✅ PASS |
  | apps:player-androidtv | 0 (no unit tests) | ✅ N/A |
  | **TOTAL** | **78** | **✅ ALL PASSING** |

  **Remaining Lint Warnings (Non-blocking):**
  - 12 warnings in Android module (GradleDependency updates, OldTargetApi, etc.)
  - Deprecated Gradle features warning (compatibility with Gradle 10)

  **Phase 01 Complete:** All tests pass, full build succeeds for both server and Android modules.
