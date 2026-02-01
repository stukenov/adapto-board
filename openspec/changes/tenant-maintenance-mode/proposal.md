# Tenant Maintenance Mode — Proposal

## Why

Maintenance mode нужен для:
- Контролируемого rollout изменений
- Заморозки изменений во время инцидента
- Планового обслуживания

## What Changes

### Tenant Extended Fields
- `support_tier` (BASIC, PREMIUM, ENTERPRISE)
- `release_ring` (STABLE, CANARY, BETA)
- `maintenance_mode` (boolean)
- `maintenance_reason` (text)
- `maintenance_until` (timestamp, nullable)

### Maintenance Mode Behavior
- Когда `maintenance_mode = true`:
  - Publish operations blocked
  - Overlay updates blocked
  - Device assignments blocked
  - Read operations allowed
  - Player continues with cached content

### Maintenance Mode API
- `PUT /api/admin/tenant/maintenance` — включить/выключить
  - Требует SupportAdmin роль
  - Обязательное поле reason
  - Optional: maintenance_until
- Audit event: MAINTENANCE_MODE_CHANGED

### Maintenance Mode UI
- Banner на всех страницах: "Maintenance mode active: {reason}"
- Settings → Maintenance: toggle, reason, scheduled end
- Confirmation modal с предупреждением

### Release Ring Feature
- Per-tenant feature flags based on release_ring
- API: `GET /api/admin/tenant/features`
- CANARY tenants get new features first
- STABLE tenants get features after validation

### Support Roles
- `SupportAgent` — read-only доступ к diagnostics
- `SupportAdmin` — может включать maintenance mode, revoke devices

## Capabilities

### New Capabilities
- `tenant-maintenance-mode`: Заморозка изменений
- `tenant-release-ring`: Feature gating по ring
- `tenant-support-tier`: Уровень поддержки

## Impact

- `libs/persistence/src/.../Tenants.kt` — extended fields
- `apps/server/src/.../routes/admin/MaintenanceRoutes.kt`
- `apps/server/src/.../plugins/MaintenancePlugin.kt`
- `apps/server/src/.../views/components/MaintenanceBanner.kt`
