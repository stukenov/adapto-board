# Phase 11: CI/CD Pipeline

This phase sets up GitHub Actions CI/CD pipeline to automatically run all tests, static analysis, and builds on every PR and push. This is the final safeguard ensuring no broken code reaches the main branch.

## Tasks

- [ ] Create base GitHub Actions workflow structure:
  - Create `.github/workflows/ci.yml` for the main CI pipeline
  - Configure workflow triggers:
    - On push to `main` and `develop` branches
    - On pull request to `main` and `develop`
  - Set up job concurrency to cancel outdated runs

- [ ] Configure CI workflow for server module:
  - Add job `server-tests`:
    ```yaml
    server-tests:
      runs-on: ubuntu-latest
      services:
        postgres:
          image: postgres:16-alpine
          env:
            POSTGRES_USER: test
            POSTGRES_PASSWORD: test
            POSTGRES_DB: playout_test
          ports:
            - 5432:5432
          options: >-
            --health-cmd pg_isready
            --health-interval 10s
            --health-timeout 5s
            --health-retries 5
      steps:
        - uses: actions/checkout@v4
        - uses: actions/setup-java@v4
          with:
            distribution: 'temurin'
            java-version: '17'
            cache: 'gradle'
        - name: Run server tests
          run: ./gradlew :apps:server:test
          env:
            DATABASE_URL: jdbc:postgresql://localhost:5432/playout_test
        - name: Upload test results
          uses: actions/upload-artifact@v4
          if: always()
          with:
            name: server-test-results
            path: apps/server/build/reports/tests/
    ```

- [ ] Configure CI workflow for library modules:
  - Add job `library-tests`:
    ```yaml
    library-tests:
      runs-on: ubuntu-latest
      services:
        postgres:
          # Same postgres config as server-tests
      steps:
        - uses: actions/checkout@v4
        - uses: actions/setup-java@v4
          with:
            distribution: 'temurin'
            java-version: '17'
            cache: 'gradle'
        - name: Run library tests
          run: |
            ./gradlew :libs:auth:test
            ./gradlew :libs:storage:test
            ./gradlew :libs:persistence:test
            ./gradlew :libs:domain:test
            ./gradlew :libs:contracts:test
    ```

- [ ] Configure CI workflow for Android module:
  - Add job `android-tests`:
    ```yaml
    android-tests:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v4
        - uses: actions/setup-java@v4
          with:
            distribution: 'temurin'
            java-version: '17'
            cache: 'gradle'
        - name: Run Android unit tests
          run: ./gradlew :apps:player-androidtv:test
        - name: Upload test results
          uses: actions/upload-artifact@v4
          if: always()
          with:
            name: android-test-results
            path: apps/player-androidtv/build/reports/tests/
    ```

- [ ] Add static analysis job:
  - Add job `static-analysis`:
    ```yaml
    static-analysis:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v4
        - uses: actions/setup-java@v4
          with:
            distribution: 'temurin'
            java-version: '17'
            cache: 'gradle'
        - name: Run ktlint
          run: ./gradlew ktlintCheck
        - name: Run detekt
          run: ./gradlew detekt
    ```

- [ ] Add build verification job:
  - Add job `build`:
    ```yaml
    build:
      runs-on: ubuntu-latest
      needs: [server-tests, library-tests, android-tests, static-analysis]
      steps:
        - uses: actions/checkout@v4
        - uses: actions/setup-java@v4
          with:
            distribution: 'temurin'
            java-version: '17'
            cache: 'gradle'
        - name: Build server
          run: ./gradlew :apps:server:build -x test
        - name: Build Android APK
          run: ./gradlew :apps:player-androidtv:assembleDebug
        - name: Upload server artifact
          uses: actions/upload-artifact@v4
          with:
            name: server-build
            path: apps/server/build/libs/
        - name: Upload Android APK
          uses: actions/upload-artifact@v4
          with:
            name: android-apk
            path: apps/player-androidtv/build/outputs/apk/debug/
    ```

- [ ] Add code coverage reporting:
  - Add coverage generation to test jobs
  - Add job `coverage-report`:
    ```yaml
    coverage-report:
      runs-on: ubuntu-latest
      needs: [server-tests, library-tests]
      steps:
        - uses: actions/checkout@v4
        - uses: actions/setup-java@v4
          with:
            distribution: 'temurin'
            java-version: '17'
            cache: 'gradle'
        - name: Generate coverage report
          run: ./gradlew jacocoTestReport
        - name: Upload coverage to Codecov
          uses: codecov/codecov-action@v4
          with:
            files: '**/build/reports/jacoco/test/jacocoTestReport.xml'
            fail_ci_if_error: false
    ```

- [ ] Configure branch protection rules:
  - Document required branch protection settings for GitHub:
    - Require status checks to pass before merging
    - Required checks: `server-tests`, `library-tests`, `android-tests`, `static-analysis`, `build`
    - Require branches to be up to date before merging
    - Require pull request reviews (1 approval)
  - Create `/docs/branch-protection-setup.md` with front matter:
    ```yaml
    ---
    type: reference
    title: Branch Protection Setup
    tags: [ci-cd, github, configuration]
    ---
    ```
  - Include step-by-step instructions for manual setup

- [ ] Verify CI pipeline works end-to-end:
  - Push changes to a feature branch
  - Create a test PR
  - Verify all CI jobs run and pass
  - Verify artifacts are uploaded correctly
  - Test that PR cannot be merged with failing checks
  - Merge PR and verify main branch workflow runs
