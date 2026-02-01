# Admin Web Channels — Proposal

## Why

Channels — ядро работы Operator. UI должен поддерживать полный workflow: создание канала → draft → добавление assets → publish.

## What Changes

### Channels List
- Таблица каналов (name, status, devices count, last publish)
- Filters: status, search
- Create channel button

### Channel Detail
- Tabs: Schedule, Overlay, Devices, History
- Channel settings (name, status)

### Schedule Tab
- Current published version info
- Draft section (create/edit)
- Items table (order, asset, time window)
- Add item modal (asset picker)
- Reorder items (drag or buttons)
- Validate button (check assets READY)
- Publish button with confirmation

### Publish Flow
- Confirmation modal
- "Changes will apply in ≤ 10 min P95"
- Device coverage: X online / Y offline
- Publish tracker after submit

### Publish Tracker
- Progress: X/Y devices applied
- Lagging devices list with reasons:
  - Offline
  - Old app version
  - Download/playback errors
- Link to device detail for triage

### Schedule History
- Version list
- Diff view (items added/removed)
- Rollback button

### Rollback Flow
- Select version
- Reason field (required)
- Confirmation with warnings

## Capabilities

### New Capabilities
- `admin-channels-list`: Список каналов
- `admin-channel-detail`: Детальная страница канала
- `admin-schedule-editor`: Редактор расписания
- `admin-publish-flow`: Publish с confirmation и tracker
- `admin-schedule-history`: История версий
- `admin-rollback-flow`: Откат версии

## Impact

- `apps/server/src/.../routes/admin/ChannelRoutes.kt`
- `apps/server/src/.../views/channels/*.kt`
