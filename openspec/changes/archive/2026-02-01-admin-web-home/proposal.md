# Admin Web Home (Ops Dashboard) — Proposal

## Why

Главный принцип UX: Home = Ops (здоровье системы), а не "список сущностей". Первое что видит пользователь — состояние системы.

## What Changes

### Ops Dashboard
- Fleet health: X online / Y total, offline list
- Publish health: последний publish, pending applies
- Overlay health: connector status, last success/error
- Alerts: open alerts с actions

### Widgets
- Online rate card (%)
- Recent publishes (last 5)
- Connector status cards
- Alert summary

### Empty States
- "No channels" → Create first channel
- "No devices" → Generate enroll code
- "No assets" → Upload sample pack

### Quick Actions
- Generate enroll code
- Create channel
- View all devices

### Pilot Dashboard (extension)
- Publish p50/p95
- Online rate trend
- Overlay latency
- Export report

## Capabilities

### New Capabilities
- `admin-ops-dashboard`: Home page с health overview
- `admin-fleet-health-widget`: Online/offline summary
- `admin-publish-health-widget`: Publish status
- `admin-overlay-health-widget`: Connector status
- `admin-alerts-widget`: Open alerts

## Impact

- `apps/server/src/.../routes/admin/HomeRoutes.kt`
- `apps/server/src/.../views/home/HomeView.kt`
- `apps/server/src/.../views/home/widgets/*.kt`
