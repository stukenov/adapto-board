# Admin Web Auth — Proposal

## Why

Admin Web UI требует аутентификацию. V1: email/password + JWT session. Server-rendered (SSR) внутри Ktor.

## What Changes

### Login Page
- Email/password форма
- Error messages (invalid credentials, account locked)
- "Forgot password" link (R1)
- Redirect to originally requested page

### Session Management
- JWT в httpOnly cookie
- Session refresh
- Logout endpoint
- Session timeout handling

### Protected Routes
- Auth middleware для всех admin routes
- Redirect to login если не авторизован
- Role-based route protection

### UI Components
- Login form
- User dropdown (profile, logout)
- Session expired modal

## Capabilities

### New Capabilities
- `admin-login-page`: Страница входа
- `admin-session-management`: Cookie-based sessions
- `admin-protected-routes`: Route protection

## Impact

- `apps/server/src/.../routes/admin/AuthRoutes.kt`
- `apps/server/src/.../views/auth/LoginView.kt`
- `apps/server/src/.../views/components/UserDropdown.kt`
- `apps/server/src/.../plugins/AdminAuthPlugin.kt`
