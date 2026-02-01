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

- [ ] Fix any failing tests in library modules:
  - Review failing tests in `/libs/auth/src/test/kotlin/`
  - Review failing tests in `/libs/storage/src/test/kotlin/`
  - Review failing tests in `/libs/persistence/src/test/kotlin/`
  - Fix issues and re-run: `./gradlew :libs:auth:test :libs:storage:test :libs:persistence:test`
  - All library tests must pass before proceeding

- [ ] Verify E2E test infrastructure with Playwright:
  - Ensure Playwright is installed: `cd apps/server && npx playwright install chromium`
  - Start the server locally: `make dev` or `./gradlew :apps:server:run`
  - Run UI tests: `RUN_UI_TESTS=true ./gradlew :apps:server:uiTest`
  - Document any E2E test failures for follow-up phases
  - Note: E2E tests may have environmental issues - document but don't block on these

- [ ] Create test coverage baseline report:
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

- [ ] Verify full project build succeeds:
  - Run complete build: `./gradlew build -x test` (excluding tests which we already verified)
  - Run with tests: `./gradlew build`
  - Fix any compilation errors or warnings
  - Ensure both server and Android modules compile successfully
  - Document the final state: all tests passing, build successful
