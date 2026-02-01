# Webhook Integration — Proposal

## Why

Webhook — один из source types для overlay. Внешние системы пушат данные в Playout Edge. Критично:
- Безопасность (подпись)
- Надёжность (retry, logs)
- Документация для интеграторов

## What Changes

### Webhook Endpoint
- Per-binding unique URL: `/api/webhook/{bindingId}`
- POST only
- Content-Type: application/json

### Signature Verification
- Algorithm: HMAC-SHA256
- Header: `X-Signature-256`
- Payload: raw request body
- Format: `sha256={hex_signature}`

Verification:
```
expected = HMAC-SHA256(secret, request_body)
actual = parse(X-Signature-256)
secure_compare(expected, actual)
```

### Request Limits
- Max payload size: 256KB
- Request timeout: 30 seconds
- Rate limit: 60 req/min per binding

### Response Codes
- 200: Accepted
- 400: Invalid payload
- 401: Invalid signature
- 413: Payload too large
- 429: Rate limited
- 500: Internal error

### Webhook Logs
- Store last 100 calls per binding
- Fields: timestamp, status, latency, payload_size, error
- Retention: 7 days

### Admin UI
- Generate/regenerate secret
- Show endpoint URL
- View call logs
- Test webhook button

### Payload Mapping
- Same as REST pull
- Presets: queue, kpi, ticker, raw
- Field mapping UI

### Error Handling
- Invalid signature → 401, log attempt
- Invalid JSON → 400 with details
- Mapping error → 400 with field info

### Security
- Secrets stored encrypted
- Secrets never logged
- Audit: secret regeneration

## Capabilities

### New Capabilities
- `webhook-endpoint`: Per-binding webhook URL
- `webhook-signature`: HMAC-SHA256 verification
- `webhook-logs`: Call history
- `webhook-mapping`: Payload processing

## Impact

- `apps/server/src/.../routes/webhook/WebhookRoutes.kt`
- `libs/overlay/src/.../WebhookHandler.kt`
- `libs/persistence/src/.../WebhookLogRepository.kt`
