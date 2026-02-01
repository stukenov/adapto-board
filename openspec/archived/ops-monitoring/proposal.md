# Ops & Monitoring — Proposal

## Why

"Support-grade UX" — возможность быстро диагностировать проблемы без долгой переписки. Включает:
- Health endpoints
- Метрики (Prometheus)
- Alerts
- Structured logging

## What Changes

### Health Endpoints
- `/health/live` — liveness
- `/health/ready` — readiness (DB + storage)
- JSON response с деталями

### Metrics (Micrometer + Prometheus)
- API latency (p50/p95/p99), error rates
- Online devices count, heartbeat lag
- SSE connections count, reconnect rate
- Upload/processing failures
- Storage download errors
- DB pool saturation, slow queries
- Job execution metrics

### Structured Logging
- JSON logs
- Fields: tenant_id, request_id, device_id, user_id
- Levels: INFO/WARN/ERROR
- No PII в логах

### Alerts
- Types: ONLINE_RATE_LOW, PUBLISH_FAILURE_SPIKE, CONNECTOR_FAILURE, DB_DOWN, APP_DOWN
- Статусы: OPEN, ACKED, RESOLVED
- Payload с деталями
- Alert list в Admin UI

### Request Correlation
- request_id в каждый response
- Correlation в логах
- Tracing подготовка (OpenTelemetry)

## Capabilities

### New Capabilities
- `health-endpoints`: Liveness и readiness checks
- `prometheus-metrics`: Метрики для monitoring
- `structured-logging`: JSON logs с correlation
- `alerts-system`: Базовая система алертов
- `request-correlation`: request_id везде

## Impact

- `libs/observability/` — Метрики и logging utilities
- `apps/server/src/.../plugins/ObservabilityPlugin.kt`
- `apps/server/src/.../routes/HealthRoutes.kt`
- `apps/server/src/.../routes/admin/AlertsRoutes.kt`
