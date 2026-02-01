# Player Telemetry — Proposal

## Why

Telemetry — heartbeat и as-run events. Backend должен знать состояние устройства, а as-run нужен для compliance отчётности.

## What Changes

### Heartbeat
- Periodic `POST /api/player/heartbeat`
- Interval from config (e.g., 30 sec)
- Payload:
  - deviceState (PLAYING, IDLE, ERROR)
  - currentAssetId
  - errors[] (последние ошибки)
  - app info

### As-Run Events
- Event types: START, END, HEARTBEAT_SNAPSHOT, ERROR
- Batch collection
- Periodic upload `POST /api/player/asrun`
- Offline buffering

### Event Collection
- Capture playback events
- Capture errors
- Timestamps (device time)
- Asset and schedule info

### Offline Handling
- Buffer events locally
- Upload when online
- Limit buffer size
- Oldest events drop on overflow

### Error Reporting
- Capture error codes and messages
- Include context (asset, operation)
- Send in heartbeat
- Severity levels

### Metrics (local)
- Playback duration
- Network errors count
- Cache hits/misses
- For status screen

## Capabilities

### New Capabilities
- `player-heartbeat`: Periodic status reporting
- `player-asrun-events`: Playback event collection
- `player-asrun-batch`: Batch upload
- `player-offline-buffer`: Event buffering

## Impact

- `apps/player-androidtv/src/.../telemetry/HeartbeatManager.kt`
- `apps/player-androidtv/src/.../telemetry/AsrunCollector.kt`
- `apps/player-androidtv/src/.../telemetry/EventBuffer.kt`
- `apps/player-androidtv/src/.../telemetry/ErrorReporter.kt`
