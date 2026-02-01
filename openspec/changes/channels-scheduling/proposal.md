# Channels & Scheduling — Proposal

## Why

Каналы и расписания — ядро продукта Playout Edge. Operator создаёт канал, добавляет assets в расписание, публикует — и изменения применяются на устройствах.

Ключевые инварианты:
- Draft не влияет на экраны
- Published version immutable
- Assets должны быть READY перед publish

## What Changes

### Channels
- CRUD для каналов
- Привязка к overlay profile (опционально)
- Статусы: ACTIVE/ARCHIVED

### Schedule Versions
- Draft создание с version+1
- Items management (order, asset, time windows)
- Publish с валидацией assets
- Rollback к предыдущей версии

### Schedule Items
- Привязка к asset
- Order index
- Time windows (valid_from, valid_to, days_of_week, time_start/end)
- Weight для shuffle (future)

### Admin API
- `POST /api/admin/channels`
- `GET /api/admin/channels`
- `PATCH /api/admin/channels/{id}`
- `POST /api/admin/channels/{channelId}/schedules/draft`
- `PUT /api/admin/schedules/{scheduleVersionId}/items`
- `POST /api/admin/schedules/{scheduleVersionId}/publish`
- `POST /api/admin/channels/{channelId}/schedules/{version}/rollback`

### Player API
- `GET /api/player/playlist` — manifest с items

## Capabilities

### New Capabilities
- `channel-management`: CRUD каналов
- `schedule-drafts`: Draft версии расписаний
- `schedule-publish`: Publish с валидацией
- `schedule-rollback`: Откат к предыдущей версии
- `playlist-manifest`: Manifest для player

## Impact

- `libs/domain/src/.../Channel.kt`, `ScheduleVersion.kt`, `ScheduleItem.kt`
- `libs/persistence/src/.../ChannelRepository.kt`, `ScheduleRepository.kt`
- `apps/server/src/.../routes/admin/ChannelsRoutes.kt`
- `apps/server/src/.../routes/player/PlaylistRoutes.kt`
