# Webhook Integration — Tasks

## Phase 1: Database

- [x] 1.1 Create WebhookLogs table
- [x] 1.2 Create migration V013__create_webhook_logs_table.sql
- [x] 1.3 Create WebhookLogEntity
- [x] 1.4 Create WebhookLogRepository

## Phase 2: Service

- [x] 2.1 Create WebhookService with signature verification
- [x] 2.2 Add secret generation/regeneration logic

## Phase 3: Webhook Endpoint

- [x] 3.1 Create WebhookRoutes with POST /api/webhook/{bindingId}
- [x] 3.2 Add signature verification middleware
- [x] 3.3 Add rate limiting per binding (using existing RateLimitPlugin)

## Phase 4: Admin API

- [x] 4.1 Add POST /api/admin/overlays/bindings/{id}/webhook/regenerate
- [x] 4.2 Add GET /api/admin/overlays/bindings/{id}/webhook/logs

## Phase 5: Integration

- [x] 5.1 Wire services and routes
- [x] 5.2 Verify build
