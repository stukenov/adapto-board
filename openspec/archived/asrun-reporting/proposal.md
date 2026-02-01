# As-Run Reporting — Proposal

## Why

As-run — фактический лог воспроизведения "что реально показывалось". Критично для:
- Compliance и отчётности
- Диагностики проблем
- Billing (если по показам)

## What Changes

### As-Run Events
- Event types: START, END, HEARTBEAT_SNAPSHOT, ERROR
- Device, channel, schedule version, asset info
- Timestamp (at)
- Details JSON (duration played, errors)

### Player Reporting
- Batch endpoint `POST /api/player/asrun`
- Coarse events (не каждую секунду)
- Offline buffering на player

### Admin Reporting
- `GET /api/admin/asrun?deviceId=&channelId=&from=&to=`
- Timeline view
- Summary aggregation
- Export CSV/PDF
- "Unknown gaps" для offline periods

### Retention
- Configurable (30-90 дней)
- Cleanup job

## Capabilities

### New Capabilities
- `asrun-events`: Запись событий воспроизведения
- `asrun-batch-api`: Batch upload от player
- `asrun-reports`: Отчёты по периоду
- `asrun-export`: Export в CSV/PDF

## Impact

- `libs/domain/src/.../AsrunEvent.kt`
- `libs/persistence/src/.../AsrunRepository.kt`
- `apps/server/src/.../routes/player/AsrunRoutes.kt`
- `apps/server/src/.../routes/admin/AsrunRoutes.kt`
- `apps/server/src/.../jobs/AsrunCleanupJob.kt`
