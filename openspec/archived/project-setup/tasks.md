## 1. Root Gradle Configuration

- [x] 1.1 Create settings.gradle.kts with all module includes
- [x] 1.2 Create root build.gradle.kts with common plugin configuration
- [x] 1.3 Create gradle.properties with version catalog and Kotlin settings

## 2. Libs Modules Setup

- [x] 2.1 Create libs/contracts module with build.gradle.kts (kotlinx.serialization)
- [x] 2.2 Create libs/domain module with build.gradle.kts (no dependencies)
- [x] 2.3 Create libs/persistence module with build.gradle.kts (Exposed, Flyway)
- [x] 2.4 Create libs/auth module with build.gradle.kts (JWT dependencies)
- [x] 2.5 Create libs/storage module with build.gradle.kts (AWS SDK optional)
- [x] 2.6 Create libs/overlay module with build.gradle.kts
- [x] 2.7 Create libs/observability module with build.gradle.kts (Micrometer)

## 3. Apps Modules Setup

- [x] 3.1 Create apps/server module with build.gradle.kts (Ktor, all libs dependencies)
- [x] 3.2 Create apps/player-androidtv module with build.gradle.kts (Android, Media3, Compose)

## 4. Docker Compose

- [x] 4.1 Create docker-compose.yml with PostgreSQL service
- [x] 4.2 Create Dockerfile for apps/server

## 5. Verification

- [x] 5.1 Verify ./gradlew build compiles all modules
- [x] 5.2 Verify docker-compose up starts PostgreSQL
