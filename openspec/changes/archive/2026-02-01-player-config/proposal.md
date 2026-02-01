# Player Config Management — Proposal

## Why

Config polling — как player узнаёт о назначенном канале и политиках. Критично для устойчивости: должен работать даже при проблемах с сетью.

## What Changes

### Config Polling
- Periodic `GET /api/player/config`
- Configurable interval (от сервера)
- Backoff при ошибках

### Config Model
- tenantId, deviceId
- channelId (assigned)
- scheduleVersionId
- overlay binding info
- assetBaseUrl
- signedUrlTtlSec
- nextPollAt

### Config Storage
- DataStore (encrypted) для persistence
- Last known good config
- Config version tracking

### Config Application
- Detect changes (channel, schedule version)
- Trigger playlist fetch on change
- Trigger overlay reconnect on change

### Fallback Behavior
- Use last known good config if poll fails
- Continue operation with cached config
- Log config fetch failures

### Server-Driven Settings
- Poll interval
- Cache policies
- Feature flags (future)

## Capabilities

### New Capabilities
- `player-config-polling`: Periodic config fetch
- `player-config-storage`: Persistent config storage
- `player-config-application`: Change detection и application

## Impact

- `apps/player-androidtv/src/.../config/ConfigManager.kt`
- `apps/player-androidtv/src/.../config/ConfigStorage.kt`
- `apps/player-androidtv/src/.../config/ConfigModels.kt`
