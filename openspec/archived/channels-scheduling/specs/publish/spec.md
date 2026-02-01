# Schedule Publish — Spec

## Requirements

### REQ-SCHED-001: Draft Management

#### Scenario: Create draft

- **WHEN** operator создаёт draft для channel
- **THEN** система создаёт schedule_version с version = prev + 1
- **AND** state = DRAFT
- **AND** draft не влияет на устройства

#### Scenario: Update draft items

- **WHEN** operator обновляет items в draft
- **THEN** система заменяет все items
- **AND** валидирует order_index uniqueness
- **AND** проверяет что assets существуют

### REQ-SCHED-002: Publish Validation

#### Scenario: Publish with READY assets

- **WHEN** operator публикует draft
- **AND** все assets имеют статус READY
- **THEN** система устанавливает state = PUBLISHED
- **AND** published_at = now()
- **AND** записывает audit SCHEDULE_PUBLISHED

#### Scenario: Publish blocked by non-READY asset

- **WHEN** operator публикует draft
- **AND** хотя бы один asset не READY
- **THEN** система возвращает ошибку ASSET_NOT_READY
- **AND** включает список problematic assets

#### Scenario: Publish empty schedule

- **WHEN** operator публикует пустой draft
- **THEN** система возвращает ошибку SCHEDULE_EMPTY
- **AND** предлагает добавить fallback

### REQ-SCHED-003: Immutability

#### Scenario: Published version immutable

- **WHEN** schedule_version имеет state = PUBLISHED
- **THEN** нельзя изменить schedule_items
- **AND** API возвращает ошибку SCHEDULE_VERSION_IMMUTABLE

### REQ-SCHED-004: Rollback

#### Scenario: Rollback to previous version

- **WHEN** operator выполняет rollback к version N
- **AND** все assets version N доступны
- **THEN** система устанавливает current version = N
- **AND** записывает audit SCHEDULE_ROLLBACK с reason

#### Scenario: Rollback blocked by deleted assets

- **WHEN** operator выполняет rollback
- **AND** некоторые assets удалены
- **THEN** система возвращает ошибку ROLLBACK_ASSETS_UNAVAILABLE

### REQ-SCHED-005: Player Manifest

#### Scenario: Get playlist manifest

- **WHEN** player запрашивает /api/player/playlist
- **THEN** система возвращает: scheduleVersionId, items (assetId, url, checksum, durationMs, orderIndex), fallback
- **AND** URLs signed с TTL

#### Scenario: Time-filtered items

- **WHEN** playlist содержит items с time windows
- **THEN** manifest включает all items
- **AND** player фильтрует по текущему времени
