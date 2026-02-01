# Device Management (Fleet) — Proposal

## Why

Fleet management — критичная часть B2B продукта. IT Owner должен видеть:
- Все устройства и их статусы
- Online/offline по heartbeat
- Версии приложений
- Последние ошибки
- Историю назначений

## What Changes

### Device Enrollment
- Генерация enroll codes (одноразовые, TTL)
- Enroll code может быть привязан к channel (lean shortcut)
- Device enroll endpoint
- Device info сохранение (model, android version, app version)

### Device Assignment
- Назначение channel устройству
- Устройство применяет при следующем config poll
- Audit event DEVICE_ASSIGNED

### Device Groups (R1)
- Создание групп
- Bulk operations на группы

### Heartbeat & Status
- Heartbeat endpoint (каждые N секунд)
- last_seen_at обновление
- Offline threshold (configurable)
- Current asset tracking
- Error reporting

### Remote Actions (R1)
- force_config_refresh
- force_playlist_refresh
- rotate_device_token
- Actions queue в DB

### Admin API
- `GET /api/admin/devices`
- `POST /api/admin/devices/enroll-codes`
- `PATCH /api/admin/devices/{id}`
- `POST /api/admin/devices/{id}/assign-channel`
- `GET /api/admin/devices/{id}/support-bundle`
- `POST /api/admin/devices/{id}/actions` (R1)

### Player API
- `POST /api/player/enroll`
- `GET /api/player/config`
- `POST /api/player/heartbeat`

## Capabilities

### New Capabilities
- `device-enrollment`: Enroll flow с codes
- `device-assignment`: Channel assignment
- `device-heartbeat`: Status tracking
- `device-dashboard`: Fleet overview
- `support-bundle`: Диагностика одной кнопкой

## Impact

- `libs/domain/src/.../Device.kt`, `EnrollCode.kt`
- `libs/persistence/src/.../DeviceRepository.kt`
- `apps/server/src/.../routes/admin/DevicesRoutes.kt`
- `apps/server/src/.../routes/player/DeviceRoutes.kt`
