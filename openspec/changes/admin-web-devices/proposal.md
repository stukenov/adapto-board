# Admin Web Devices — Proposal

## Why

Fleet management UI для IT Owner. Должен давать полную картину состояния всех устройств и инструменты для triage.

## What Changes

### Devices Dashboard
- Summary cards: total, online, offline, pending
- Online rate trend chart (optional)
- Quick filters

### Devices List
- Table: name, status (online/offline), channel, app version, last seen, last error
- Filters: status, channel, location, app version
- Search
- Bulk actions (R1)

### Device Detail
- Status card (online/offline, SSE status)
- Current channel и schedule version
- Current asset playing
- App version, android version, model
- Last config/playlist time
- Last errors (list)
- Support bundle button
- Actions: assign channel, rename, revoke

### Enroll Codes
- Generate form: count, TTL, bind to channel, label
- Generated codes list (code, QR, copy button)
- Print layout
- Active codes list

### Assign Channel
- Channel picker modal
- "Next poll at ~..." indicator
- Confirmation

### Revoke Device
- Reason field (required)
- Confirmation with warning
- Audit event

### Support Bundle
- "Copy support bundle" button
- JSON format with all diagnostics

### Device Groups (R1)
- Groups list
- Create/edit group
- Add/remove devices

### Remote Actions (R1)
- force_config_refresh
- force_playlist_refresh
- Status tracking

## Capabilities

### New Capabilities
- `admin-devices-dashboard`: Fleet overview
- `admin-devices-list`: Список устройств
- `admin-device-detail`: Детальная страница устройства
- `admin-enroll-codes`: Генерация enroll codes
- `admin-device-assign`: Назначение канала
- `admin-device-revoke`: Отзыв устройства
- `admin-support-bundle`: Диагностический пакет

## Impact

- `apps/server/src/.../routes/admin/DeviceRoutes.kt`
- `apps/server/src/.../views/devices/*.kt`
