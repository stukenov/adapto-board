# Tenant Maintenance Mode — Design

## Overview

Maintenance mode позволяет заморозить изменения tenant'а для rollout, инцидентов или планового обслуживания.

## Database

Расширить таблицу Tenants следующими полями:
- `support_tier` (VARCHAR)
- `release_ring` (VARCHAR)
- `maintenance_mode` (BOOLEAN)
- `maintenance_reason` (TEXT)
- `maintenance_until` (TIMESTAMP)

## Components

### 1. TenantEnums

```kotlin
enum class SupportTier {
    BASIC, PREMIUM, ENTERPRISE
}

enum class ReleaseRing {
    STABLE, CANARY, BETA
}
```

### 2. MaintenancePlugin

Ktor plugin для блокировки mutating операций в maintenance mode:
- Проверяет tenant.maintenanceMode на каждом запросе
- Блокирует POST/PUT/PATCH/DELETE к определённым routes
- Пропускает read-only операции

### 3. MaintenanceService

```kotlin
class MaintenanceService(tenantRepo) {
    suspend fun enableMaintenance(tenantId, reason, until?)
    suspend fun disableMaintenance(tenantId)
    suspend fun getStatus(tenantId): MaintenanceStatus
}
```

## Admin API

| Method | Path | Description |
|--------|------|-------------|
| PUT | /api/admin/tenant/maintenance | Toggle maintenance mode |
| GET | /api/admin/tenant/maintenance | Get maintenance status |

## Request/Response

### PUT /api/admin/tenant/maintenance
```json
{
  "enabled": true,
  "reason": "Scheduled maintenance",
  "until": "2025-01-15T10:00:00Z"
}
```

### GET /api/admin/tenant/maintenance
```json
{
  "maintenanceMode": false,
  "reason": null,
  "until": null
}
```

## Blocked Operations in Maintenance Mode

- Schedule publish
- Overlay updates
- Device assignments
- Channel modifications

## Allowed Operations in Maintenance Mode

- All read operations
- Authentication
- Device heartbeat
- Playlist fetch
