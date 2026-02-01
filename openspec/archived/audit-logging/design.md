# Audit Logging — Design

## Overview

Централизованный audit log для всех изменений в системе. Запись actor, action, entity, diff.

## Existing Infrastructure

- `AuditLog` table уже существует
- `ActorType` enum уже существует

## Key Components

### 1. AuditRepository

```kotlin
interface AuditRepository {
    suspend fun log(event: AuditEvent): AuditLogEntity
    suspend fun findByTenant(tenantId: TenantId, filters: AuditFilters): List<AuditLogEntity>
    suspend fun findByEntity(tenantId: TenantId, entityType: String, entityId: UUID): List<AuditLogEntity>
    suspend fun deleteOlderThan(days: Int): Long
}
```

### 2. AuditService

```kotlin
class AuditService(private val auditRepo: AuditRepository) {
    suspend fun log(
        tenantId: TenantId?,
        actorUserId: UUID?,
        actorType: ActorType,
        action: String,
        entityType: String,
        entityId: UUID,
        diff: JsonObject? = null
    )

    suspend fun query(tenantId: TenantId, filters: AuditFilters): AuditQueryResult
}
```

### 3. AuditFilters

```kotlin
data class AuditFilters(
    val entityType: String? = null,
    val entityId: UUID? = null,
    val actorUserId: UUID? = null,
    val action: String? = null,
    val fromDate: Instant? = null,
    val toDate: Instant? = null,
    val limit: Int = 100,
    val offset: Int = 0
)
```

## Admin API Routes

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/admin/audit | Query audit log |
| GET | /api/admin/audit/export | Export CSV |

## Standard Actions

- USER_CREATED, USER_UPDATED, USER_DELETED
- ASSET_UPLOADED, ASSET_ARCHIVED, ASSET_DELETED
- CHANNEL_CREATED, CHANNEL_UPDATED, CHANNEL_ARCHIVED
- SCHEDULE_PUBLISHED, SCHEDULE_ROLLBACK
- DEVICE_ENROLLED, DEVICE_ASSIGNED, DEVICE_REVOKED
- OVERLAY_STATE_UPDATED

## Integration Points

Audit logging вызывается из сервисов после успешных операций:
- ChannelService, ScheduleService, AssetUploadService
- DeviceService, OverlayService
- User management

## Retention

- Default: 180 дней
- Configurable per tenant
- Cleanup job (background)
