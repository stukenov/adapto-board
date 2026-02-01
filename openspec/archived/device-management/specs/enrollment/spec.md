# Device Enrollment — Spec

## Requirements

### REQ-DEV-001: Enroll Code Generation

#### Scenario: Generate single code

- **WHEN** admin генерирует enroll code
- **THEN** система создаёт код 6-8 alphanumeric символов
- **AND** код имеет TTL (default 30 минут)
- **AND** код одноразовый
- **AND** возвращает code и QR payload

#### Scenario: Generate codes with channel binding

- **WHEN** admin генерирует code с channel_id
- **THEN** устройство автоматически получает channel assignment после enroll
- **AND** не требует отдельного шага assign

#### Scenario: Generate batch codes

- **WHEN** admin генерирует N codes
- **THEN** система создаёт N уникальных codes
- **AND** все с одинаковым TTL и channel binding

### REQ-DEV-002: Device Enrollment

#### Scenario: Successful enrollment

- **WHEN** device отправляет valid code и device_info
- **THEN** система создаёт device record
- **AND** enroll_status = ENROLLED
- **AND** сохраняет: android_model, android_version, app_version
- **AND** выдаёт refresh_token и device JWT
- **AND** помечает code как использованный
- **AND** записывает audit DEVICE_ENROLLED

#### Scenario: Enrollment with channel binding

- **WHEN** device enrolls с code привязанным к channel
- **THEN** assigned_channel_id устанавливается автоматически
- **AND** device может сразу начать playback

### REQ-DEV-003: Device Assignment

#### Scenario: Assign channel

- **WHEN** admin назначает channel устройству
- **THEN** assigned_channel_id обновляется
- **AND** записывает audit DEVICE_ASSIGNED
- **AND** устройство получает новый channel при следующем config poll

#### Scenario: Unassign channel

- **WHEN** admin убирает channel с устройства
- **THEN** assigned_channel_id = null
- **AND** устройство показывает "waiting for assignment"

### REQ-DEV-004: Heartbeat

#### Scenario: Heartbeat update

- **WHEN** device отправляет heartbeat
- **THEN** last_seen_at обновляется
- **AND** current_asset_id сохраняется (опционально)
- **AND** errors сохраняются

#### Scenario: Offline detection

- **WHEN** last_seen_at > offline_threshold
- **THEN** device считается offline
- **AND** появляется в списке offline devices

### REQ-DEV-005: Revoke

#### Scenario: Revoke device

- **WHEN** admin revokes device
- **THEN** enroll_status = REVOKED
- **AND** все tokens инвалидируются
- **AND** записывает audit DEVICE_REVOKED
- **AND** device получает 401 на следующем запросе

### REQ-DEV-006: Support Bundle

#### Scenario: Get support bundle

- **WHEN** admin или support запрашивает support bundle для device
- **THEN** система возвращает JSON с:
  - tenant info
  - device info (id, model, versions)
  - runtime state (last_seen, config times, current asset)
  - assigned channel и schedule version
  - последние N ошибок
  - последние heartbeat данные
