# Device Remote Actions — Design

## Overview

Удалённые команды устройствам через server. Admin создаёт action → player получает через config poll → ack.

## Database

Добавить таблицу device_actions и enum DeviceActionType.

## Components

### 1. DeviceActionRepository

```kotlin
interface DeviceActionRepository {
    suspend fun create(action: CreateDeviceAction): DeviceActionEntity
    suspend fun findPending(tenantId: TenantId, deviceId: UUID): List<DeviceActionEntity>
    suspend fun findByDevice(tenantId: TenantId, deviceId: UUID): List<DeviceActionEntity>
    suspend fun acknowledge(actionId: UUID, status: ActionStatus): DeviceActionEntity?
    suspend fun expireOldActions(): Long
}
```

### 2. DeviceActionService

```kotlin
class DeviceActionService(repo: DeviceActionRepository) {
    suspend fun createAction(tenantId, deviceId, actionType, params?, createdBy)
    suspend fun getPendingActions(tenantId, deviceId): List<DeviceAction>
    suspend fun acknowledgeAction(actionId, status)
}
```

## Admin API

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/admin/devices/{id}/actions | Create action |
| GET | /api/admin/devices/{id}/actions | Action history |

## Player API

Actions returned in config response:
```json
{
  "pendingActions": [
    {"id": "...", "action": "FORCE_PLAYLIST_REFRESH", "params": null}
  ]
}
```

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/player/actions/{id}/ack | Acknowledge action |

## Action Types

- FORCE_CONFIG_REFRESH
- FORCE_PLAYLIST_REFRESH
- ROTATE_DEVICE_TOKEN
