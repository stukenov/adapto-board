# Database Tables — Spec

## Requirements

### REQ-DB-001: Tenancy Tables

#### Scenario: Tenants table

- **WHEN** tenant создаётся
- **THEN** запись в `tenants` содержит: id (uuid), name, status (ACTIVE/SUSPENDED), created_at
- **AND** id является primary key

#### Scenario: Users table

- **WHEN** user создаётся
- **THEN** запись в `users` содержит: id, tenant_id, email, display_name, status, password_hash, created_at
- **AND** (tenant_id, email) уникальны

#### Scenario: User roles table

- **WHEN** role назначается user
- **THEN** запись в `user_roles` содержит: user_id, role (enum)

### REQ-DB-002: Device Tables

#### Scenario: Devices table

- **WHEN** device регистрируется
- **THEN** запись в `devices` содержит: id, tenant_id, display_name, enroll_status, device_secret_hash, assigned_channel_id, last_seen_at, app_version, android_model, android_version, created_at

#### Scenario: Device groups

- **WHEN** group создаётся
- **THEN** `device_groups` содержит: id, tenant_id, name
- **AND** `device_group_members` связывает devices с groups

### REQ-DB-003: Asset Tables

#### Scenario: Assets table

- **WHEN** asset загружается
- **THEN** запись в `assets` содержит: id, tenant_id, type (VIDEO/IMAGE), name, status, duration_ms, mime_type, checksum_sha256, storage_key, width, height, created_by, created_at

#### Scenario: Asset versions (optional)

- **WHEN** asset имеет версии
- **THEN** `asset_versions` содержит: id, asset_id, profile (ORIGINAL/NORMALIZED), storage_key, codec, bitrate, container

### REQ-DB-004: Channel & Schedule Tables

#### Scenario: Channels table

- **WHEN** channel создаётся
- **THEN** запись в `channels` содержит: id, tenant_id, name, status, default_overlay_profile_id, created_at

#### Scenario: Schedule versions table

- **WHEN** schedule version создаётся
- **THEN** `schedule_versions` содержит: id, tenant_id, channel_id, version (monotonic), state (DRAFT/PUBLISHED/ROLLED_BACK), published_at, created_by, created_at
- **AND** (tenant_id, channel_id, version) уникальны

#### Scenario: Schedule items table

- **WHEN** items добавляются в schedule
- **THEN** `schedule_items` содержит: id, tenant_id, schedule_version_id, asset_id, order_index, valid_from, valid_to, days_of_week, time_start, time_end, weight
- **AND** (tenant_id, schedule_version_id, order_index) уникальны

### REQ-DB-005: Overlay Tables

#### Scenario: Overlay profiles

- **WHEN** profile создаётся
- **THEN** `overlay_profiles` содержит: id, tenant_id, name, definition_json, created_at

#### Scenario: Overlay bindings

- **WHEN** binding создаётся
- **THEN** `overlay_bindings` содержит: id, tenant_id, channel_id, overlay_profile_id, source_type, source_config_json, status, created_at

#### Scenario: Overlay states

- **WHEN** state обновляется
- **THEN** `overlay_states` содержит: id, tenant_id, channel_id, state_json, updated_at
- **AND** (tenant_id, channel_id) уникальны

### REQ-DB-006: Audit & As-Run Tables

#### Scenario: Audit log

- **WHEN** действие логируется
- **THEN** `audit_log` содержит: id, tenant_id, actor_user_id, actor_type, action, entity_type, entity_id, diff_json, created_at

#### Scenario: As-run events

- **WHEN** playback event записывается
- **THEN** `asrun_events` содержит: id, tenant_id, device_id, channel_id, schedule_version_id, asset_id, event_type, at, details_json

### REQ-DB-007: Ops Tables

#### Scenario: Tenant contacts

- **WHEN** contact добавляется
- **THEN** `tenant_contacts` содержит: id, tenant_id, type, name, email, phone, created_at

#### Scenario: Device actions

- **WHEN** action создаётся
- **THEN** `device_actions` содержит: id, tenant_id, device_id, action, params_json, status, created_by, created_at, ack_at

#### Scenario: Alerts

- **WHEN** alert возникает
- **THEN** `alerts` содержит: id, tenant_id, type, status, payload_json, first_seen_at, last_seen_at

### REQ-DB-008: Jobs Table

#### Scenario: Background jobs

- **WHEN** job создаётся
- **THEN** `jobs` содержит: id, type, payload_json, status, next_run_at, attempts, locked_by, locked_at, created_at

### REQ-DB-009: Tenant Isolation

#### Scenario: All tables have tenant_id

- **WHEN** данные запрашиваются
- **THEN** все таблицы (кроме справочных) содержат tenant_id
- **AND** запросы всегда фильтруются по tenant_id
