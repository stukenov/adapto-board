# Channels & Scheduling — Design

## Overview

Реализация каналов и расписаний с draft/publish workflow. Расписание становится активным только после publish.

## Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                        Admin API                               │
│  POST /channels │ GET /channels │ PATCH /channels/{id}         │
│  POST /channels/{id}/schedules/draft                           │
│  PUT /schedules/{id}/items │ POST /schedules/{id}/publish      │
│  POST /channels/{id}/schedules/{ver}/rollback                  │
└─────────────────────────┬──────────────────────────────────────┘
                          │
┌─────────────────────────▼──────────────────────────────────────┐
│                   ScheduleService                              │
│  - createDraft()                                               │
│  - updateItems()                                               │
│  - publish() ─ validates all assets READY                      │
│  - rollback() ─ checks assets availability                     │
└─────────────────────────┬──────────────────────────────────────┘
                          │
┌─────────────────────────▼──────────────────────────────────────┐
│  ChannelRepository  │  ScheduleRepository  │  AssetRepository  │
└─────────────────────────┬──────────────────────────────────────┘
                          │
┌─────────────────────────▼──────────────────────────────────────┐
│                        Player API                              │
│  GET /api/player/playlist ─ returns manifest with signed URLs  │
└────────────────────────────────────────────────────────────────┘
```

## Key Components

### 1. ChannelService

Location: `libs/domain/src/main/kotlin/com/playoutedge/domain/services/ChannelService.kt`

```kotlin
class ChannelService(
    private val channelRepo: ChannelRepository
) {
    suspend fun create(tenantId: TenantId, request: CreateChannelRequest): Channel
    suspend fun findAll(tenantId: TenantId): List<Channel>
    suspend fun findById(tenantId: TenantId, channelId: UUID): Channel?
    suspend fun update(tenantId: TenantId, channelId: UUID, request: UpdateChannelRequest): Channel?
    suspend fun archive(tenantId: TenantId, channelId: UUID): Boolean
}
```

### 2. ScheduleService

Location: `libs/domain/src/main/kotlin/com/playoutedge/domain/services/ScheduleService.kt`

```kotlin
class ScheduleService(
    private val scheduleRepo: ScheduleRepository,
    private val assetRepo: AssetRepository
) {
    suspend fun createDraft(tenantId: TenantId, channelId: UUID, createdBy: UUID?): ScheduleVersion
    suspend fun updateItems(tenantId: TenantId, versionId: UUID, items: List<ScheduleItemInput>): List<ScheduleItem>
    suspend fun publish(tenantId: TenantId, versionId: UUID): PublishResult
    suspend fun rollback(tenantId: TenantId, channelId: UUID, toVersion: Int, reason: String): RollbackResult
    suspend fun getActiveVersion(tenantId: TenantId, channelId: UUID): ScheduleVersion?
    suspend fun getDraftVersion(tenantId: TenantId, channelId: UUID): ScheduleVersion?
}

sealed class PublishResult {
    data class Success(val version: ScheduleVersion) : PublishResult()
    data class AssetsNotReady(val assetIds: List<UUID>) : PublishResult()
    object ScheduleEmpty : PublishResult()
}

sealed class RollbackResult {
    data class Success(val version: ScheduleVersion) : RollbackResult()
    data class AssetsUnavailable(val assetIds: List<UUID>) : RollbackResult()
    data class VersionNotFound(val version: Int) : RollbackResult()
}
```

### 3. PlaylistService

Location: `libs/domain/src/main/kotlin/com/playoutedge/domain/services/PlaylistService.kt`

```kotlin
class PlaylistService(
    private val scheduleRepo: ScheduleRepository,
    private val assetRepo: AssetRepository,
    private val storageService: StorageService
) {
    suspend fun getManifest(tenantId: TenantId, channelId: UUID): PlaylistManifest?
}

data class PlaylistManifest(
    val scheduleVersionId: UUID,
    val version: Int,
    val items: List<PlaylistItem>,
    val fallbackAsset: PlaylistItem?
)

data class PlaylistItem(
    val assetId: UUID,
    val url: String,
    val checksum: String,
    val durationMs: Long,
    val orderIndex: Int,
    val validFrom: LocalDate?,
    val validTo: LocalDate?,
    val daysOfWeek: Int?,
    val timeStart: LocalTime?,
    val timeEnd: LocalTime?
)
```

## Admin API Routes

### ChannelsRoutes

Location: `apps/server/src/main/kotlin/com/playoutedge/server/routes/ChannelsRoutes.kt`

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/admin/channels | Create channel |
| GET | /api/admin/channels | List channels |
| GET | /api/admin/channels/{id} | Get channel |
| PATCH | /api/admin/channels/{id} | Update channel |
| DELETE | /api/admin/channels/{id} | Archive channel |

### SchedulesRoutes

Location: `apps/server/src/main/kotlin/com/playoutedge/server/routes/SchedulesRoutes.kt`

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/admin/channels/{channelId}/schedules/draft | Create draft |
| GET | /api/admin/channels/{channelId}/schedules | List versions |
| GET | /api/admin/schedules/{versionId} | Get version |
| PUT | /api/admin/schedules/{versionId}/items | Replace items |
| POST | /api/admin/schedules/{versionId}/publish | Publish |
| POST | /api/admin/channels/{channelId}/schedules/{version}/rollback | Rollback |

## Player API Routes

### PlaylistRoutes

Location: `apps/server/src/main/kotlin/com/playoutedge/server/routes/player/PlaylistRoutes.kt`

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/player/playlist | Get manifest |

Request requires device auth with assigned channel.

## Domain Enums

### ScheduleState

```kotlin
enum class ScheduleState {
    DRAFT,      // Being edited
    PUBLISHED,  // Active, immutable
    ARCHIVED    // Replaced by newer version
}
```

### ChannelStatus

```kotlin
enum class ChannelStatus {
    ACTIVE,
    ARCHIVED
}
```

## Validation Rules

### Publish Validation

1. Draft must have at least one item
2. All referenced assets must have status READY
3. Order indices must be unique within version

### Rollback Validation

1. Target version must exist and be PUBLISHED/ARCHIVED
2. All assets in target version must not be DELETED

## Error Codes

| Code | Description |
|------|-------------|
| CHANNEL_NOT_FOUND | Channel ID invalid |
| SCHEDULE_VERSION_NOT_FOUND | Version ID invalid |
| SCHEDULE_VERSION_IMMUTABLE | Cannot modify published version |
| SCHEDULE_EMPTY | Cannot publish empty schedule |
| ASSET_NOT_READY | Some assets not ready for publish |
| ROLLBACK_ASSETS_UNAVAILABLE | Some assets deleted |
| DUPLICATE_ORDER_INDEX | Order index collision |

## Dependencies

- Existing: ChannelRepository, ScheduleRepository, AssetRepository
- New: ChannelService, ScheduleService, PlaylistService
- StorageService for signed URLs
