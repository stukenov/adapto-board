# 04 — Контракты API (Ktor)

Ниже — минимальные endpoints для MVP. Контракты фиксируются как OpenAPI + Kotlin data classes (kotlinx.serialization) и шарятся между backend/admin/player через Kotlin Multiplatform (опционально).

## 1) Аутентификация

### 1.1 Admin API

- v1 (pilot): email+password + JWT.
- v1.5/R1: OIDC (Azure AD/Okta/Keycloak) → backend выдаёт session/JWT.

### 1.2 Player API (Android TV)

- Device enroll: одноразовый код/QR → backend выдаёт device refresh token.
- Дальше: device JWT с TTL + refresh.

Важно: device токен ограничен tenant/device scope, не даёт доступ к admin ресурсам.

## 2) Control Plane API (Admin/Integrator)

### 2.1 Tenants/Users

- `POST /api/admin/users` (TenantAdmin)
- `GET /api/admin/users`
- `PATCH /api/admin/users/{id}`

### 2.2 Assets

- `POST /api/admin/assets/upload` (multipart) → `{assetId, status}`
- `GET /api/admin/assets?query=&type=&status=&page=`
- `GET /api/admin/assets/{id}`
- `DELETE /api/admin/assets/{id}` (soft delete)

### 2.3 Channels & Schedules

- `POST /api/admin/channels`
- `GET /api/admin/channels`
- `PATCH /api/admin/channels/{id}`

Draft/publish:
- `POST /api/admin/channels/{channelId}/schedules/draft` → create new draft version
- `PUT /api/admin/schedules/{scheduleVersionId}/items` → replace items (simple v1)
- `POST /api/admin/schedules/{scheduleVersionId}/publish`
- `POST /api/admin/channels/{channelId}/schedules/{version}/rollback` (optional v1)

### 2.4 Fleet

- `GET /api/admin/devices?status=&groupId=`
- `POST /api/admin/devices/enroll-codes` → выдаёт `code`/`qrPayload`
- `PATCH /api/admin/devices/{id}` (rename, assign channel)
- `POST /api/admin/devices/{id}/assign-channel` → `{channelId}`

### 2.5 Overlay

- `POST /api/admin/overlay/profiles`
- `GET /api/admin/overlay/profiles`
- `PUT /api/admin/overlay/profiles/{id}`

- `POST /api/admin/overlay/bindings` (channel → profile + source)
- `GET /api/admin/overlay/bindings?channelId=`
- `PATCH /api/admin/overlay/bindings/{id}`

Manual data (pilot):
- `PUT /api/admin/overlay/state/{channelId}` → полный state
- `PATCH /api/admin/overlay/state/{channelId}` → patch

### 2.6 Audit / As-run

- `GET /api/admin/audit?entityType=&entityId=&from=&to=`
- `GET /api/admin/asrun?deviceId=&channelId=&from=&to=`

## 3) Player API (Android TV)

### 3.1 Enrollment

- `POST /api/player/enroll` → `{deviceId, refreshToken, configPollIntervalSec}`
  - вход: `{enrollCode, deviceInfo}`

### 3.2 Config & Playlist

- `GET /api/player/config` → актуальная привязка + политика кэширования
  - `{tenantId, deviceId, channelId, scheduleVersionId, overlay: {profileId, bindingId}, assetBaseUrl, signedUrlTtlSec, nextPollAt}`

- `GET /api/player/playlist` → manifest
  - `{scheduleVersionId, items:[{assetId, url, checksum, durationMs, orderIndex}] , fallback: {...}}`

### 3.3 Heartbeat & As-run

- `POST /api/player/heartbeat` (каждые N секунд)
  - `{deviceState, currentAssetId, errors[]}`

- `POST /api/player/asrun` (батч)
  - `{events:[{type, at, assetId, details}]}`

### 3.4 Overlay stream (SSE)

- `GET /api/player/overlay/stream?channelId=` (SSE)
  - события:
    - `event: state` → `{version, state}`
    - `event: patch` → `{version, patch}`
    - `event: keepalive` → `{ts}`

## 4) Подпись URL на assets

Чтобы Android TV качал медиа без постоянной авторизации на storage:

- backend выдаёт `url` со сроком жизни (signed URL), привязанный к `tenantId` и `assetId`.
- device может обновить manifest, если URL истёк.

В пилоте (local storage) допускается “проксирование” через backend с device auth, но это увеличивает нагрузку на app.

