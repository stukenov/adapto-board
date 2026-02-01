# Audit Logging — Proposal

## Why

Audit log — обязательное требование для B2B/enterprise. IT Owner и compliance должны видеть:
- Кто и что менял
- Когда произошло изменение
- Что именно изменилось (diff)

## What Changes

### Audit Events
- Actor types: USER, SYSTEM, DEVICE
- Actions: CREATE, UPDATE, DELETE, PUBLISH, ASSIGN, ENROLL, REVOKE, LOGIN, etc.
- Entity types: ASSET, CHANNEL, SCHEDULE, DEVICE, OVERLAY, USER, TENANT
- Diff JSON (минимальный diff изменений)
- Request correlation (request_id)

### Audit Middleware
- Автоматическая запись для важных команд
- TenantContext и actor info
- Structured logging integration

### Admin API
- `GET /api/admin/audit?entityType=&entityId=&from=&to=&actor=`
- Pagination и фильтры
- Export CSV

### Retention
- Configurable retention (180-365 дней)
- Cleanup job

## Capabilities

### New Capabilities
- `audit-events`: Запись всех изменений
- `audit-query`: Фильтры и поиск по audit log
- `audit-export`: Export в CSV

## Impact

- `libs/domain/src/.../AuditEvent.kt`
- `libs/persistence/src/.../AuditRepository.kt`
- `apps/server/src/.../plugins/AuditPlugin.kt`
- `apps/server/src/.../routes/admin/AuditRoutes.kt`
- `apps/server/src/.../jobs/AuditCleanupJob.kt`
