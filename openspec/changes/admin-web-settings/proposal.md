# Admin Web Settings — Proposal

## Why

Settings для TenantAdmin: users, policies, tenant configuration. Критично для enterprise control.

## What Changes

### Users Management
- Users list: email, name, role, status, last login
- Invite user form:
  - Email
  - Role selection with description
  - Send invite
- Edit user: change role, reset password, deactivate
- Resend invite
- RBAC: только TenantAdmin может управлять users

### Roles Description
- TenantAdmin: всё в рамках tenant
- Operator: контент/каналы/расписания, без users
- (R1) Viewer: read-only
- (R1) Integrator: API access

### Tenant Settings
- Timezone
- Offline threshold (2-10 минут)
- Maintenance mode toggle
- Release ring (CANARY/STABLE)

### Content Policies
- Allowed codecs/containers
- Max bitrate
- Max resolution
- Max asset size
- Storage quota display

### Retention Settings
- Audit log retention (days)
- As-run retention (days)
- Current usage display

### Tenant Contacts
- Contact list: type, name, email, phone
- Types: IT_OWNER, OPERATOR_OWNER, BILLING, SECURITY
- Add/edit/delete contacts

### Limits & Quotas
- Storage: used / quota
- Devices: enrolled / max
- SSE connections: current / max
- Upgrade CTA

### Export Settings
- Export all settings as JSON
- Security/compliance pack download (R1)

## Capabilities

### New Capabilities
- `admin-users-management`: CRUD users и roles
- `admin-tenant-settings`: Tenant configuration
- `admin-content-policies`: Content validation rules
- `admin-retention-settings`: Cleanup configuration
- `admin-tenant-contacts`: Contact management
- `admin-limits-quotas`: Quota display

## Impact

- `apps/server/src/.../routes/admin/SettingsRoutes.kt`
- `apps/server/src/.../views/settings/*.kt`
