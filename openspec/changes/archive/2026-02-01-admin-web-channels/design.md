# Admin Web Channels — Design

## Overview

Channel management UI with list, detail, schedule editor, and publish flow.

## Routes

```
GET  /admin/channels              → Channels list
GET  /admin/channels/new          → Create channel form
POST /admin/channels              → Create channel
GET  /admin/channels/:id          → Channel detail
POST /admin/channels/:id          → Update channel
GET  /admin/channels/:id/schedule → Schedule editor
POST /admin/channels/:id/publish  → Publish schedule
```

## Views

### ChannelsListView

```kotlin
fun HTML.channelsListView(
    session: AdminClaims,
    channels: List<ChannelListItem>,
    filters: ChannelFilters
)
```

### ChannelDetailView

```kotlin
fun HTML.channelDetailView(
    session: AdminClaims,
    channel: ChannelDetail,
    scheduleVersion: ScheduleVersionDetail?,
    devices: List<DeviceListItem>
)
```

### ScheduleEditorView

```kotlin
fun HTML.scheduleEditorView(
    session: AdminClaims,
    channel: ChannelDetail,
    draftItems: List<ScheduleItem>,
    availableAssets: List<AssetListItem>
)
```

## Data Classes

```kotlin
data class ChannelListItem(
    val id: UUID,
    val name: String,
    val status: ChannelStatus,
    val deviceCount: Int,
    val lastPublish: Instant?
)

data class ChannelDetail(
    val id: UUID,
    val name: String,
    val status: ChannelStatus,
    val createdAt: Instant
)

data class ScheduleItem(
    val id: UUID,
    val assetId: UUID,
    val assetName: String,
    val sortOrder: Int,
    val startTime: String?,
    val endTime: String?
)

data class DeviceListItem(
    val id: UUID,
    val name: String,
    val status: DeviceEnrollStatus,
    val lastSeen: Instant?
)
```

## Components

```kotlin
fun FlowContent.channelTable(channels: List<ChannelListItem>)
fun FlowContent.scheduleTable(items: List<ScheduleItem>)
fun FlowContent.deviceTable(devices: List<DeviceListItem>)
fun FlowContent.assetPicker(assets: List<AssetListItem>)
```
