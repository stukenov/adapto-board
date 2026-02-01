# Admin Web Home — Design

## Overview

Server-side rendered dashboard showing system health. Built with Ktor HTML DSL.

## Routes

```
GET /admin           → HomeView (dashboard)
GET /admin/          → redirect to /admin
```

## Views

### HomeView

Main dashboard with widgets:

```kotlin
fun HTML.homeView(
    session: AdminClaims,
    fleetHealth: FleetHealth,
    publishHealth: PublishHealth,
    alerts: List<AlertSummary>
)
```

### Widgets

Each widget is a function that renders into a card:

```kotlin
fun FlowContent.fleetHealthWidget(health: FleetHealth)
fun FlowContent.publishHealthWidget(health: PublishHealth)
fun FlowContent.alertsWidget(alerts: List<AlertSummary>)
fun FlowContent.quickActionsWidget()
```

## Data Classes

```kotlin
data class FleetHealth(
    val onlineCount: Int,
    val totalCount: Int,
    val offlineDevices: List<OfflineDevice>
)

data class OfflineDevice(
    val id: UUID,
    val name: String,
    val lastSeen: Instant?
)

data class PublishHealth(
    val lastPublish: Instant?,
    val pendingCount: Int
)

data class AlertSummary(
    val id: UUID,
    val type: String,
    val message: String,
    val createdAt: Instant
)
```

## Dependencies

- DeviceRepository for fleet health
- AlertRepository for alerts
- AdminSessionPlugin for auth

## CSS

Uses existing styles.css classes:
- `.dashboard-grid` for widget layout
- `.card` for widget containers
- `.badge` for status indicators
