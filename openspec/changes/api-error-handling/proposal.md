# API Error Handling — Proposal

## Why

Единый формат ошибок критичен для:
- Понятных сообщений в UI
- Отладки интеграций
- Корректной обработки на player
- Correlation между requests и logs

## What Changes

### Error Envelope (единый формат)

Все API возвращают ошибки в формате:
```json
{
  "code": "ASSET_NOT_READY",
  "message": "Asset is still processing",
  "details": { "assetId": "...", "status": "PROCESSING" },
  "requestId": "req-abc123"
}
```

### Error Codes (стабильные)

#### Auth Errors
- `INVALID_CREDENTIALS`
- `TOKEN_EXPIRED`
- `TOKEN_INVALID`
- `FORBIDDEN_ROLE`
- `DEVICE_NOT_ENROLLED`
- `ENROLL_CODE_EXPIRED`
- `ENROLL_CODE_USED`

#### Tenant Errors
- `TENANT_NOT_FOUND`
- `TENANT_SUSPENDED`
- `TENANT_QUOTA_EXCEEDED`

#### Asset Errors
- `ASSET_NOT_FOUND`
- `ASSET_NOT_READY`
- `ASSET_TOO_LARGE`
- `ASSET_INVALID_FORMAT`

#### Schedule Errors
- `SCHEDULE_VERSION_CONFLICT`
- `SCHEDULE_VERSION_IMMUTABLE`
- `SCHEDULE_EMPTY`
- `ROLLBACK_ASSETS_UNAVAILABLE`

#### Device Errors
- `DEVICE_NOT_FOUND`
- `DEVICE_REVOKED`

#### Overlay Errors
- `OVERLAY_STATE_TOO_LARGE`
- `CONNECTOR_FAILURE`

#### General Errors
- `VALIDATION_ERROR`
- `NOT_FOUND`
- `INTERNAL_ERROR`

### Request Correlation
- `requestId` в каждый response
- `requestId` в логах
- Header `X-Request-Id` для override

### HTTP Status Mapping
- 400 — validation errors
- 401 — auth errors
- 403 — authorization errors
- 404 — not found
- 409 — conflicts
- 422 — business rule violations
- 500 — internal errors
- 503 — service unavailable

## Capabilities

### New Capabilities
- `api-error-envelope`: Единый формат ошибок
- `api-error-codes`: Стабильные коды ошибок
- `api-request-correlation`: RequestId везде

## Impact

- `libs/contracts/src/.../ApiError.kt`
- `apps/server/src/.../plugins/ErrorHandlingPlugin.kt`
- `apps/server/src/.../plugins/RequestIdPlugin.kt`
