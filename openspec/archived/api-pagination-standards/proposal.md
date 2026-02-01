# API Pagination & Standards — Proposal

## Why

Единые стандарты API критичны для:
- Консистентного UX в админке
- Интеграций через API
- Предсказуемого поведения

## What Changes

### Pagination
Все list endpoints используют cursor-based pagination:
```json
{
  "items": [...],
  "nextPageToken": "abc123",
  "totalCount": 150
}
```

Query params:
- `pageSize` (default: 20, max: 100)
- `pageToken` (опционально, для следующей страницы)

### Sorting
- `sortBy` (field name)
- `sortOrder` (asc/desc, default: desc)
- Default sort: `created_at desc`

### Filtering
- Field-specific params: `status=READY`, `type=VIDEO`
- Date ranges: `from=2024-01-01`, `to=2024-01-31`
- Search: `query=keyword`

### Rate Limiting
- Admin API: 100 req/min per user
- Player API: 60 req/min per device
- Header: `X-RateLimit-Remaining`, `X-RateLimit-Reset`
- Response 429 Too Many Requests

### API Versioning
- URL prefix: `/api/admin/v1/`, `/api/player/v1/`
- Breaking changes → new version
- Support N-1 versions minimum

### Request/Response Standards
- Content-Type: `application/json`
- Timestamps: ISO 8601 (`2024-01-15T10:30:00Z`)
- IDs: UUID v4
- Enums: UPPER_SNAKE_CASE

### Idempotency
- POST operations support `X-Idempotency-Key` header
- Key valid for 24 hours
- Same key → same response (no duplicate creation)

## Capabilities

### New Capabilities
- `api-pagination`: Cursor-based pagination
- `api-rate-limiting`: Request limits
- `api-versioning`: Version management
- `api-idempotency`: Safe retries

## Impact

- `libs/contracts/src/.../Pagination.kt`
- `apps/server/src/.../plugins/RateLimitPlugin.kt`
- `apps/server/src/.../plugins/IdempotencyPlugin.kt`
