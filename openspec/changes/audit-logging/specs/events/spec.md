# Audit Events — Spec

## Requirements

### REQ-AUDIT-001: Event Recording

#### Scenario: User action audit

- **WHEN** user выполняет важное действие
- **THEN** система записывает audit event с:
  - actor_type = USER
  - actor_user_id = user id
  - action (CREATE/UPDATE/DELETE/PUBLISH/etc.)
  - entity_type и entity_id
  - diff_json (что изменилось)
  - request_id для correlation

#### Scenario: System action audit

- **WHEN** система выполняет автоматическое действие
- **THEN** audit event с actor_type = SYSTEM

#### Scenario: Device action audit

- **WHEN** device выполняет действие (enroll, heartbeat error)
- **THEN** audit event с actor_type = DEVICE

### REQ-AUDIT-002: Actions Coverage

Следующие actions должны логироваться:

#### User Management
- CREATE_USER, UPDATE_USER, DELETE_USER
- ASSIGN_ROLE, LOGIN, LOGOUT

#### Assets
- ASSET_UPLOADED, ASSET_DELETED

#### Channels & Schedules
- CHANNEL_CREATED, CHANNEL_UPDATED, CHANNEL_ARCHIVED
- SCHEDULE_DRAFT_CREATED, SCHEDULE_PUBLISHED, SCHEDULE_ROLLBACK

#### Devices
- DEVICE_ENROLLED, DEVICE_ASSIGNED, DEVICE_REVOKED
- ENROLL_CODE_GENERATED

#### Overlay
- OVERLAY_PROFILE_CREATED, OVERLAY_PROFILE_UPDATED
- OVERLAY_BINDING_CREATED, OVERLAY_BINDING_UPDATED
- OVERLAY_STATE_UPDATED

#### Settings
- TENANT_SETTINGS_UPDATED, POLICY_UPDATED

### REQ-AUDIT-003: Query API

#### Scenario: Filter by entity

- **WHEN** admin запрашивает audit с entity_type=CHANNEL и entity_id=X
- **THEN** возвращаются все events для этого channel

#### Scenario: Filter by time range

- **WHEN** admin запрашивает audit с from/to
- **THEN** возвращаются events в указанном диапазоне

#### Scenario: Filter by actor

- **WHEN** admin запрашивает audit с actor_user_id=X
- **THEN** возвращаются все events этого user

### REQ-AUDIT-004: Export

#### Scenario: Export to CSV

- **WHEN** admin экспортирует audit
- **THEN** система генерирует CSV с колонками:
  - timestamp, actor_type, actor_email, action, entity_type, entity_id, diff_summary

### REQ-AUDIT-005: Retention

#### Scenario: Cleanup old events

- **WHEN** event старше retention period
- **THEN** cleanup job удаляет event
- **AND** retention configurable per tenant (180-365 дней)
