# Player Overlay Rendering — Proposal

## Why

Overlay (data layer) отображается поверх видео. Должен обновляться в realtime через SSE и быть устойчивым к разрывам соединения.

## What Changes

### SSE Client
- Connect to `/api/player/overlay/stream?channelId=`
- Parse events: state, patch, keepalive
- Reconnection with backoff
- Version tracking

### State + Patch
- Initial `state` при connect
- Apply `patch` events
- Handle version mismatch → request new state
- Idempotent operations

### Overlay Reducer
- Apply upsert (create/update widgets)
- Apply remove (hide widgets)
- Validate schema
- Handle unknown widget types gracefully

### Compose Rendering
- Jetpack Compose overlay layer
- Widget renderers:
  - Text widget
  - Ticker (scrolling text)
  - Table (N rows)
  - KPI tiles
  - QR code
  - Image
- Position and sizing
- Basic styles

### Overlay UI
- Overlay container (on top of video)
- Widget layout management
- Animation for updates
- Fade in/out on visibility changes

### Resilience
- SSE down → freeze overlay, continue video
- Hide overlay on prolonged disconnect
- Show overlay when reconnected

### Performance
- Efficient recomposition
- Minimize overdraw
- Memory management for images

## Capabilities

### New Capabilities
- `player-sse-client`: SSE connection management
- `player-overlay-reducer`: State+patch processing
- `player-overlay-widgets`: Widget renderers
- `player-overlay-compose`: Compose UI layer

## Impact

- `apps/player-androidtv/src/.../overlay/SseClient.kt`
- `apps/player-androidtv/src/.../overlay/OverlayReducer.kt`
- `apps/player-androidtv/src/.../overlay/OverlayState.kt`
- `apps/player-androidtv/src/.../overlay/ui/OverlayContainer.kt`
- `apps/player-androidtv/src/.../overlay/ui/widgets/*.kt`
