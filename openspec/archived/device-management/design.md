# Device Management — Design

## Overview

Расширение существующей device инфраструктуры: добавление Admin API для fleet management и Player API для heartbeat/config.

## Existing Infrastructure

Уже реализовано:
- DeviceEntity, EnrollCodeEntity
- DeviceRepository с базовыми методами
- POST /api/device/enroll — device enrollment
- POST /api/admin/enroll-codes — создание кодов
- POST /api/admin/devices/{id}/revoke — revoke устройства

## New Components

### 1. DeviceService

Location: `apps/server/src/main/kotlin/com/playoutedge/server/services/DeviceService.kt`

```kotlin
class DeviceService(
    private val deviceRepo: DeviceRepository
) {
    suspend fun findAll(tenantId: TenantId): List<DeviceEntity>
    suspend fun findById(tenantId: TenantId, deviceId: UUID): DeviceEntity?
    suspend fun findByChannel(tenantId: TenantId, channelId: UUID): List<DeviceEntity>
    suspend fun update(tenantId: TenantId, deviceId: UUID, update: UpdateDeviceRequest): DeviceEntity?
    suspend fun assignChannel(tenantId: TenantId, deviceId: UUID, channelId: UUID): DeviceEntity?
    suspend fun updateHeartbeat(tenantId: TenantId, deviceId: UUID, currentAssetId: UUID?, playerState: String?): DeviceEntity?
    suspend fun getOnlineDevices(tenantId: TenantId, thresholdMinutes: Int = 5): List<DeviceEntity>
}
```

### 2. Admin API Routes

Location: `apps/server/src/main/kotlin/com/playoutedge/server/routes/DevicesRoutes.kt`

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/admin/devices | List all devices |
| GET | /api/admin/devices/{id} | Get device details |
| PATCH | /api/admin/devices/{id} | Update device |
| POST | /api/admin/devices/{id}/assign-channel | Assign channel |

### 3. Player API Routes

Location: `apps/server/src/main/kotlin/com/playoutedge/server/routes/player/DevicePlayerRoutes.kt`

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/player/config | Get device config |
| POST | /api/player/heartbeat | Report heartbeat |

## Data Models

### DeviceResponse (Admin)

```kotlin
@Serializable
data class DeviceResponse(
    val id: String,
    val displayName: String,
    val enrollStatus: String,
    val assignedChannelId: String?,
    val lastSeenAt: String?,
    val appVersion: String?,
    val androidModel: String?,
    val androidVersion: String?,
    val createdAt: String,
    val isOnline: Boolean
)
```

### DeviceConfigResponse (Player)

```kotlin
@Serializable
data class DeviceConfigResponse(
    val channelId: String?,
    val heartbeatIntervalSeconds: Int,
    val configPollIntervalSeconds: Int,
    val playlistPollIntervalSeconds: Int
)
```

### HeartbeatRequest

```kotlin
@Serializable
data class HeartbeatRequest(
    val currentAssetId: String?,
    val playerState: String?,  // "PLAYING", "PAUSED", "IDLE"
    val appVersion: String?
)
```

## Repository Extensions

Добавить в DeviceRepository:
- `updateHeartbeat(tenantId, deviceId, lastSeenAt, currentAssetId?, playerState?)`
- `findOnline(tenantId, thresholdMinutes)` — devices seen within threshold

## Online Status Logic

```
isOnline = lastSeenAt != null && lastSeenAt > now() - HEARTBEAT_THRESHOLD
HEARTBEAT_THRESHOLD = 5 minutes (configurable)
```

## Integration

Wire in Application.module():
1. Add DeviceService
2. Register devicesRoutes
3. Register devicePlayerRoutes

## Error Codes

| Code | Description |
|------|-------------|
| DEVICE_NOT_FOUND | Device ID invalid |
| DEVICE_NOT_ENROLLED | Device not enrolled |
| DEVICE_REVOKED | Device access revoked |
