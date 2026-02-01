# Admin Web Devices — Design

## Overview

Fleet management UI with device list, detail, and enrollment code generation.

## Routes

```
GET  /admin/devices              → Devices list
GET  /admin/devices/:id          → Device detail
GET  /admin/devices/enroll       → Enroll codes page
POST /admin/devices/enroll       → Generate enroll code
```

## Views

### DevicesListView

```kotlin
fun HTML.devicesListView(
    session: AdminClaims,
    devices: List<DeviceViewItem>,
    stats: DeviceStats,
    filters: DeviceFilters
)
```

### DeviceDetailView

```kotlin
fun HTML.deviceDetailView(
    session: AdminClaims,
    device: DeviceDetailView,
    recentActions: List<DeviceActionItem>
)
```

### EnrollCodesView

```kotlin
fun HTML.enrollCodesView(
    session: AdminClaims,
    channels: List<ChannelOption>,
    activeCodes: List<EnrollCodeItem>
)
```

## Data Classes

```kotlin
data class DeviceViewItem(
    val id: UUID,
    val name: String,
    val status: DeviceEnrollStatus,
    val isOnline: Boolean,
    val channelName: String?,
    val appVersion: String?,
    val lastSeen: Instant?
)

data class DeviceStats(
    val total: Int,
    val online: Int,
    val offline: Int,
    val pending: Int
)

data class DeviceDetailModel(
    val id: UUID,
    val name: String,
    val status: DeviceEnrollStatus,
    val isOnline: Boolean,
    val channelId: UUID?,
    val channelName: String?,
    val appVersion: String?,
    val androidModel: String?,
    val androidVersion: String?,
    val lastSeen: Instant?,
    val createdAt: Instant
)
```
