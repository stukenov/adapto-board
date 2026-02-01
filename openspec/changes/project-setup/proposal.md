# Project Setup — Proposal

## Why

Playout Edge требует организованную структуру монорепозитория с жёсткими границами модулей. Без правильной структуры проекта невозможно обеспечить:
- Единую кодовую базу для backend, admin web UI и Android TV player
- Shared contracts (DTO) между компонентами
- Чистое разделение domain логики от инфраструктуры

## What Changes

- Создание Gradle multi-module проекта (Kotlin DSL)
- Настройка модульной структуры:
  - `apps/server` — Ktor монолит
  - `apps/player-androidtv` — Android TV приложение
  - `libs/contracts` — Shared DTO и API contracts
  - `libs/domain` — Доменная модель без зависимостей
  - `libs/persistence` — Exposed + миграции
  - `libs/auth` — RBAC, JWT, device auth
  - `libs/storage` — Storage абстракция (LOCAL/S3)
  - `libs/overlay` — State+patch модель
  - `libs/observability` — Метрики, логирование
- Конфигурация зависимостей (Kotlin 2.x, Ktor, Exposed, Media3)
- Docker-compose для локальной разработки

## Capabilities

### New Capabilities
- `monorepo-structure`: Gradle multi-module проект с правильными зависимостями
- `shared-contracts`: Единые DTO между server и player
- `local-dev-environment`: Docker-compose с Postgres для разработки

## Impact

- Новые файлы:
  - `settings.gradle.kts`
  - `build.gradle.kts` (root + per module)
  - `gradle.properties`
  - `docker-compose.yml`
  - Module directories structure
