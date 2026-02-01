# Admin Web Auth — Design

## Overview

Server-rendered admin web UI with email/password authentication using JWT stored in httpOnly cookies.

## Architecture

### SSR with Ktor HTML DSL
- All views rendered server-side using kotlinx.html
- Static assets (CSS, JS) served from resources
- Minimal JavaScript for interactivity

### Authentication Flow

1. User visits `/admin` (protected)
2. Redirected to `/admin/login` if not authenticated
3. Submits email/password form
4. Server validates credentials, sets JWT cookie
5. Redirects to originally requested page or `/admin`
6. Logout clears cookie

## Components

### Routes
- `GET /admin/login` - Login page
- `POST /admin/login` - Process login
- `POST /admin/logout` - Logout
- `GET /admin/*` - Protected admin routes

### Views
- `LoginView` - Login form with error handling
- `AdminLayout` - Base layout with nav and user dropdown
- `UserDropdown` - Profile menu with logout

### Plugins
- `AdminSessionPlugin` - Cookie-based JWT session validation
- Redirects to login if session invalid/expired

## Data Flow

```
Browser -> GET /admin/dashboard
        <- 302 Redirect to /admin/login?next=/admin/dashboard
        -> GET /admin/login?next=/admin/dashboard
        <- HTML Login Page
        -> POST /admin/login (email, password)
        <- Set-Cookie: admin_session=<jwt>; HttpOnly; Secure
        <- 302 Redirect to /admin/dashboard
        -> GET /admin/dashboard (Cookie: admin_session=<jwt>)
        <- HTML Dashboard Page
```

## Security

- JWT in httpOnly cookie (not accessible from JS)
- Secure flag in production
- SameSite=Lax
- CSRF protection via SameSite
- Session timeout: 24 hours
