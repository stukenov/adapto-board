# Monorepo Structure — Spec

## Requirements

### REQ-PS-001: Gradle Multi-Module Project

Проект должен быть организован как Gradle multi-module с Kotlin DSL.

#### Scenario: Root project configuration

- **WHEN** разработчик клонирует репозиторий
- **THEN** `./gradlew build` собирает все модули
- **AND** зависимости между модулями соблюдаются

### REQ-PS-002: Module Structure

Должна быть создана следующая структура модулей:

#### Scenario: Apps modules exist

- **WHEN** проект структурирован
- **THEN** существует `apps/server` (Ktor монолит)
- **AND** существует `apps/player-androidtv` (Android TV приложение)

#### Scenario: Libs modules exist

- **WHEN** проект структурирован
- **THEN** существует `libs/contracts` (Shared DTO)
- **AND** существует `libs/domain` (Доменная модель)
- **AND** существует `libs/persistence` (Exposed + миграции)
- **AND** существует `libs/auth` (RBAC, JWT)
- **AND** существует `libs/storage` (LOCAL/S3)
- **AND** существует `libs/overlay` (State+patch)
- **AND** существует `libs/observability` (Метрики, логи)

### REQ-PS-003: Dependency Rules

Модули должны соблюдать правила зависимостей.

#### Scenario: Domain has no dependencies

- **WHEN** модуль `libs/domain` собирается
- **THEN** он не зависит от других модулей проекта
- **AND** содержит только доменные классы

#### Scenario: Contracts has no runtime dependencies

- **WHEN** модуль `libs/contracts` собирается
- **THEN** он не зависит от server/UI/Android специфичных библиотек

#### Scenario: Server depends on libs

- **WHEN** модуль `apps/server` собирается
- **THEN** он зависит от: domain, contracts, persistence, auth, storage, overlay, observability

### REQ-PS-004: Docker Compose

Локальная разработка должна быть возможна через Docker Compose.

#### Scenario: Local development setup

- **WHEN** разработчик запускает `docker-compose up`
- **THEN** поднимается PostgreSQL
- **AND** создаётся база данных
- **AND** приложение доступно на localhost
