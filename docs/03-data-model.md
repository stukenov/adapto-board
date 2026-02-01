# 03 — Модель данных (PostgreSQL)

Цель: минимальный набор таблиц, достаточный для MVP, с понятными инвариантами и индексами. Ниже — логическая модель (не окончательный DDL).

## 1) Общие принципы

- Все записи (кроме справочников) содержат `tenant_id`.
- Все сущности, влияющие на показ, версионируются (минимум: расписания).
- Любое изменение критичных сущностей пишет `audit_log`.

## 2) Сущности

### 2.1 Tenancy / Users

**`tenants`**
- `id` (uuid, pk)
- `name`
- `status` (ACTIVE/SUSPENDED)
- `created_at`

**`users`**
- `id` (uuid, pk)
- `tenant_id` (uuid, fk tenants)
- `email` (unique within tenant)
- `display_name`
- `status`
- `password_hash` (если без SSO)
- `created_at`

**`user_roles`**
- `user_id` (fk users)
- `role` (enum)

Индексы:
- `users(tenant_id, email)` unique

### 2.2 Devices / Fleet

**`devices`**
- `id` (uuid, pk)
- `tenant_id`
- `display_name`
- `enroll_status` (PENDING/ENROLLED/REVOKED)
- `device_secret_hash` (или public key, если делаем подпись)
- `assigned_channel_id` (uuid, nullable)
- `last_seen_at`
- `app_version`
- `android_model` / `android_version`
- `created_at`

**`device_groups`**
- `id` (uuid, pk)
- `tenant_id`
- `name`

**`device_group_members`**
- `device_group_id`
- `device_id`

Индексы:
- `devices(tenant_id, last_seen_at)`
- `devices(tenant_id, assigned_channel_id)`

### 2.3 Content / Assets

**`assets`**
- `id` (uuid, pk)
- `tenant_id`
- `type` (VIDEO/IMAGE)
- `name`
- `status` (UPLOADING/PROCESSING/READY/REJECTED/DELETED)
- `duration_ms` (nullable for images)
- `mime_type`
- `checksum_sha256`
- `storage_key` (путь/ключ в storage)
- `width` / `height` (optional)
- `created_by`
- `created_at`

Опционально для версий:
**`asset_versions`**
- `id`
- `asset_id`
- `profile` (ORIGINAL/NORMALIZED)
- `storage_key`
- `codec`/`bitrate`/`container`

Индексы:
- `assets(tenant_id, status)`
- `assets(tenant_id, created_at)`

### 2.4 Channels / Scheduling

**`channels`**
- `id` (uuid, pk)
- `tenant_id`
- `name`
- `status` (ACTIVE/ARCHIVED)
- `default_overlay_profile_id` (nullable)
- `created_at`

**`schedule_versions`**
- `id` (uuid, pk)
- `tenant_id`
- `channel_id` (fk channels)
- `version` (int, monotonic per channel)
- `state` (DRAFT/PUBLISHED/ROLLED_BACK)
- `published_at` (nullable)
- `created_by`
- `created_at`

**`schedule_items`**
- `id` (uuid, pk)
- `tenant_id`
- `schedule_version_id` (fk schedule_versions)
- `asset_id` (fk assets)
- `order_index` (int)
- `valid_from` (nullable)
- `valid_to` (nullable)
- `days_of_week` (bitmask) (optional)
- `time_start` / `time_end` (optional, local tenant timezone)
- `weight` (optional for simple shuffle later)

Инварианты:
- В одном `schedule_version` `order_index` уникален.
- `schedule_version` immutable после publish (кроме служебных полей).

Индексы:
- `schedule_versions(tenant_id, channel_id, version)` unique
- `schedule_versions(tenant_id, channel_id, state)`
- `schedule_items(tenant_id, schedule_version_id, order_index)` unique

### 2.5 Overlay / Data layer

**`overlay_profiles`**
- `id` (uuid, pk)
- `tenant_id`
- `name`
- `definition_json` (layout + widgets)
- `created_at`

**`overlay_bindings`**
- `id` (uuid, pk)
- `tenant_id`
- `channel_id` (fk channels)
- `overlay_profile_id` (fk overlay_profiles)
- `source_type` (MANUAL/REST_PULL/WEBHOOK)
- `source_config_json` (endpoint, auth, mapping, polling interval)
- `status`
- `created_at`

**`overlay_states`** (снапшот текущего состояния на канал/устройство)
- `id` (uuid, pk)
- `tenant_id`
- `channel_id` (fk channels)
- `state_json`
- `updated_at`

Индексы:
- `overlay_states(tenant_id, channel_id)` unique

### 2.6 Audit / As-run

**`audit_log`**
- `id` (uuid, pk)
- `tenant_id`
- `actor_user_id` (nullable для system/device)
- `actor_type` (USER/SYSTEM/DEVICE)
- `action` (CREATE/UPDATE/DELETE/PUBLISH/ASSIGN/ENROLL/…)
- `entity_type` (ASSET/CHANNEL/SCHEDULE/DEVICE/OVERLAY/USER)
- `entity_id`
- `diff_json` (минимальный diff)
- `created_at`

Индексы:
- `audit_log(tenant_id, created_at)`
- `audit_log(tenant_id, entity_type, entity_id, created_at)`

**`asrun_events`** (coarse)
- `id` (uuid, pk)
- `tenant_id`
- `device_id`
- `channel_id`
- `schedule_version_id` (nullable)
- `asset_id` (nullable)
- `event_type` (START/END/HEARTBEAT_SNAPSHOT/ERROR)
- `at` (timestamp)
- `details_json` (optional)

Индексы:
- `asrun_events(tenant_id, device_id, at)`
- `asrun_events(tenant_id, channel_id, at)`

## 3) Очистка и ретеншн (обязательно определить)

- `asrun_events`: ретеншн 30–90 дней (настраиваемо).
- `audit_log`: ретеншн 180–365 дней (или бессрочно для enterprise).
- `assets`: lifecycle “soft delete” → purge после N дней.

