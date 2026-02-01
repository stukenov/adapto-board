# Admin Web Assets — Design

## Overview

Asset library management with list, upload, and detail views.

## Routes

```
GET  /admin/assets              → Assets list
GET  /admin/assets/:id          → Asset detail
POST /admin/assets/:id/delete   → Delete asset (soft)
```

## Views

### AssetsListView

```kotlin
fun HTML.assetsListView(
    session: AdminClaims,
    assets: List<AssetViewItem>,
    quota: QuotaInfo,
    filters: AssetFilters
)
```

### AssetDetailView

```kotlin
fun HTML.assetDetailView(
    session: AdminClaims,
    asset: AssetDetailModel,
    usedInSchedules: List<ScheduleUsage>
)
```

## Data Classes

```kotlin
data class AssetViewItem(
    val id: UUID,
    val name: String,
    val type: AssetType,
    val status: AssetStatus,
    val fileSize: Long,
    val duration: Int?,
    val createdAt: Instant
)

data class QuotaInfo(
    val usedBytes: Long,
    val limitBytes: Long,
    val usedPercent: Int
)

data class AssetDetailModel(
    val id: UUID,
    val name: String,
    val type: AssetType,
    val status: AssetStatus,
    val mimeType: String?,
    val fileSize: Long,
    val duration: Int?,
    val width: Int?,
    val height: Int?,
    val rejectionReason: String?,
    val createdAt: Instant
)
```
