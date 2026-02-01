# Overlay System — Proposal

## Why

Overlay (data layer) — ключевой differentiator Playout Edge. Данные (очереди, KPI, уведомления) обновляются в realtime поверх видео, без рендеринга "100 версий видео".

Архитектурные решения:
- Transport: SSE (не WebSocket) — проходит через прокси
- Format: state + domain patch по widgetId
- Sources: Manual, REST pull, Webhook

## What Changes

### Overlay Profiles
- Definition JSON (layout + widgets)
- Widget types: text, ticker, table, KPI tiles, QR/image
- Позиция, размеры, базовые стили

### Overlay Bindings
- Связь channel → profile → source
- Source types: MANUAL, REST_PULL, WEBHOOK
- Source config (endpoint, auth, mapping, polling interval)

### Overlay State
- JSON state per channel
- Version для sync
- Size limits (256KB-1MB)

### SSE Transport
- `GET /api/player/overlay/stream?channelId=`
- Events: `state` (при connect), `patch`, `keepalive`
- Reconnection handling
- In-memory pub/sub per channel

### Data Sources

#### Manual
- `PUT /api/admin/overlay/state/{channelId}` — полный state
- `PATCH /api/admin/overlay/state/{channelId}` — patch

#### REST Pull
- Job polling endpoint
- Mapping presets (queue/kpi/ticker)
- Error tracking и status

#### Webhook Push
- Endpoint URL generation
- Signing secret validation
- Rate limiting
- Logs последних calls

### Domain Patch Format
- `upsert`: список виджетов (id + payload)
- `remove`: список widgetId
- Idempotent operations
- Schema validation

## Capabilities

### New Capabilities
- `overlay-profiles`: Шаблоны layout и widgets
- `overlay-bindings`: Связь channel-profile-source
- `overlay-sse`: Realtime stream для player
- `overlay-manual`: Ручное обновление данных
- `overlay-rest-pull`: Polling внешних endpoints
- `overlay-webhook`: Push от внешних систем

## Impact

- `libs/overlay/` — State+patch модель, reducer
- `libs/domain/src/.../OverlayProfile.kt`, `OverlayBinding.kt`
- `libs/persistence/src/.../OverlayRepository.kt`
- `apps/server/src/.../routes/admin/OverlayRoutes.kt`
- `apps/server/src/.../routes/player/OverlayStreamRoutes.kt`
- `apps/server/src/.../jobs/OverlayPullJob.kt`
