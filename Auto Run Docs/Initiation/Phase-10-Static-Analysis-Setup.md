# Phase 10: Static Analysis Setup

This phase adds static analysis tools (detekt, ktlint) to catch code quality issues automatically. Static analysis prevents bugs, enforces consistency, and maintains code quality without requiring manual review of every change.

## Tasks

- [ ] Configure detekt for Kotlin static analysis:
  - Add detekt plugin to root `build.gradle.kts`:
    ```kotlin
    plugins {
        id("io.gitlab.arturbosch.detekt") version "1.23.4"
    }

    detekt {
        buildUponDefaultConfig = true
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    }
    ```
  - Create `/config/detekt/detekt.yml` with project-specific rules:
    - Enable complexity checks (long methods, large classes)
    - Enable potential bug detection
    - Enable code smell detection
    - Configure thresholds appropriate for the project
    - Suppress rules that conflict with project style

- [ ] Configure ktlint for code formatting:
  - Add ktlint plugin to root `build.gradle.kts`:
    ```kotlin
    plugins {
        id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    }

    ktlint {
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
    }
    ```
  - Create `.editorconfig` with Kotlin style preferences:
    - Indentation (4 spaces)
    - Max line length (120)
    - Import ordering
    - Trailing comma preferences

- [ ] Run initial static analysis and fix critical issues:
  - Run detekt: `./gradlew detekt`
  - Document all issues found
  - Fix critical and major issues:
    - Potential bugs
    - Security issues
    - Major complexity issues
  - Suppress minor issues that are intentional (with comments)

- [ ] Run ktlint and fix formatting issues:
  - Run check: `./gradlew ktlintCheck`
  - Auto-format where possible: `./gradlew ktlintFormat`
  - Fix issues that can't be auto-formatted
  - Verify clean run after fixes

- [ ] Add pre-commit hook for static analysis:
  - Create `.githooks/pre-commit`:
    ```bash
    #!/bin/bash
    ./gradlew ktlintCheck detekt --daemon
    ```
  - Add setup script to README or Makefile:
    ```bash
    git config core.hooksPath .githooks
    ```
  - Verify hook blocks commit on violations

- [ ] Verify static analysis passes completely:
  - Run full analysis: `./gradlew ktlintCheck detekt`
  - Both must pass with zero violations
  - Document any suppressed rules and reasons
  - Update Makefile with analysis commands:
    - `make lint` - run all static analysis
    - `make lint-fix` - auto-fix where possible
