# Webhook Integration — Design

## Overview

Webhook endpoint для overlay - внешние системы пушат JSON данные, которые обновляют overlay state.

## Database

### Existing: OverlayBindings
Уже содержит `webhookSecret` поле.

### New: WebhookLogs table

```sql
CREATE TABLE webhook_logs (
    id UUID PRIMARY KEY,
    binding_id UUID NOT NULL REFERENCES overlay_bindings(id),
    status_code INT NOT NULL,
    latency_ms INT NOT NULL,
    payload_size INT NOT NULL,
    error TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

## Components

### 1. WebhookService

```kotlin
class WebhookService(overlayRepo, webhookLogRepo) {
    suspend fun processWebhook(bindingId: UUID, signature: String, payload: ByteArray): WebhookResult
    suspend fun verifySignature(secret: String, signature: String, payload: ByteArray): Boolean
    suspend fun regenerateSecret(bindingId: UUID): String
    suspend fun getLogs(bindingId: UUID, limit: Int): List<WebhookLog>
}
```

### 2. Signature Verification

Algorithm: HMAC-SHA256
- Header: `X-Signature-256`
- Format: `sha256={hex_signature}`

```kotlin
fun verifySignature(secret: String, signature: String, payload: ByteArray): Boolean {
    val expected = Mac.getInstance("HmacSHA256")
        .apply { init(SecretKeySpec(secret.toByteArray(), "HmacSHA256")) }
        .doFinal(payload)
        .toHex()
    return "sha256=$expected" == signature
}
```

## Admin API

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/admin/overlays/bindings/{id}/webhook/regenerate | Regenerate webhook secret |
| GET | /api/admin/overlays/bindings/{id}/webhook/logs | Get webhook call logs |

## Webhook Endpoint

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/webhook/{bindingId} | Receive webhook payload |

## Response Codes

- 200: Accepted
- 400: Invalid payload/JSON
- 401: Invalid signature
- 413: Payload too large (>256KB)
- 429: Rate limited
- 500: Internal error

## Request Limits

- Max payload: 256KB
- Rate limit: 60 req/min per binding
