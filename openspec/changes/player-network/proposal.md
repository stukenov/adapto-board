# Player Network Layer — Proposal

## Why

Network layer — основа коммуникации Android TV player с backend. Должен быть устойчив к плохим сетям и enterprise прокси.

## What Changes

### HTTP Client
- Ktor client (или OkHttp)
- kotlinx.serialization для JSON
- Configurable timeouts
- Retry с exponential backoff

### Authentication
- Device JWT storage (encrypted)
- Token refresh flow
- Token injection в requests
- Handle 401 → refresh → retry

### API Client Interface
- Config API calls
- Playlist API calls
- Heartbeat API calls
- As-run batch API calls
- Enroll API calls

### Network State
- Online/offline detection
- Network type (WiFi/Ethernet)
- Connectivity monitoring
- State changes callback

### Error Handling
- Network errors categorization
- Timeout handling
- Server errors (5xx) handling
- Auth errors handling

### Logging
- Request/response logging (debug)
- Error logging
- Correlation with request_id

## Capabilities

### New Capabilities
- `player-http-client`: Configured HTTP client
- `player-api-client`: API calls abstraction
- `player-auth-handler`: Token management
- `player-network-state`: Connectivity monitoring

## Impact

- `apps/player-androidtv/src/.../network/HttpClient.kt`
- `apps/player-androidtv/src/.../network/ApiClient.kt`
- `apps/player-androidtv/src/.../network/AuthHandler.kt`
- `apps/player-androidtv/src/.../network/NetworkMonitor.kt`
