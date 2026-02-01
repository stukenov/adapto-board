# Auth & RBAC — Proposal

## Why

Playout Edge — B2B продукт с enterprise требованиями к безопасности:
- Разделение ролей (TenantAdmin, Operator, SupportAgent)
- Device authentication для Android TV
- Signed URLs для доступа к медиа
- Audit всех действий с авторизацией

## What Changes

### Admin Authentication (v1: JWT, R1: OIDC)
- Email/password login с JWT
- Refresh token механизм
- Session management
- OIDC интеграция (подготовка для R1)

### Device Authentication
- Enroll code генерация (одноразовый, TTL)
- Device refresh token выдача
- Device JWT с коротким TTL
- Token rotation и revoke

### RBAC
- Роли v1: `TenantAdmin`, `Operator`
- Внутренние роли: `SupportAgent`, `SupportAdmin`
- Route-level и service-level authorization
- Permission checking middleware

### Signed URLs
- Asset URL подпись с TTL
- Привязка к tenant_id и asset_id

## Capabilities

### New Capabilities
- `admin-jwt-auth`: JWT аутентификация для админки
- `device-enroll-auth`: Enrollment flow для устройств
- `rbac-enforcement`: Проверка ролей на всех endpoints
- `signed-asset-urls`: Подписанные URL для медиа

## Impact

- `libs/auth/` — Модуль аутентификации
- `apps/server/src/.../routes/` — Auth endpoints
- `apps/server/src/.../plugins/` — Auth middleware
