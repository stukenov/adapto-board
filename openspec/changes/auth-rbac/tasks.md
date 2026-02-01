## 1. Auth Module Setup (libs/auth)

- [x] 1.1 Add jwt, bcrypt dependencies to libs/auth/build.gradle.kts
- [x] 1.2 Create AuthConfig data class with JWT secrets and TTLs from environment
- [x] 1.3 Create TokenClaims data classes (AdminClaims, DeviceClaims)
- [x] 1.4 Create Role enum with TenantAdmin, Operator, SupportAgent, SupportAdmin

## 2. Core Auth Services (libs/auth)

- [x] 2.1 Create PasswordService with hash() and verify() using bcrypt
- [x] 2.2 Create JwtService with generateAdminToken() and generateDeviceToken()
- [x] 2.3 Add validateToken() returning TokenClaims or null
- [x] 2.4 Add generateRefreshToken() for both admin and device

## 3. RBAC Definitions (libs/auth)

- [x] 3.1 Create Permission enum with all resource actions
- [x] 3.2 Create RolePermissions object mapping roles to permissions
- [x] 3.3 Add hasPermission(role, permission) helper function

## 4. Server Auth Plugins (apps/server)

- [x] 4.1 Create JwtAuthPlugin installing Ktor Authentication with admin-jwt and device-jwt providers
- [x] 4.2 Create RbacPlugin for role-based route protection
- [x] 4.3 Add requireRole() and requirePermission() route extensions
- [x] 4.4 Add call.adminClaims and call.deviceClaims extension properties

## 5. Admin Auth Endpoints (apps/server)

- [x] 5.1 Create POST /api/auth/login endpoint (email, password → tokens)
- [x] 5.2 Create POST /api/auth/refresh endpoint (refresh token → new access token)
- [x] 5.3 Create POST /api/auth/logout endpoint (clear refresh cookie)
- [x] 5.4 Create GET /api/auth/me endpoint (return current user info)

## 6. Device Auth Endpoints (apps/server)

- [x] 6.1 Create POST /api/admin/enroll-codes endpoint (generate enroll code)
- [x] 6.2 Create POST /api/device/enroll endpoint (code + deviceInfo → tokens)
- [x] 6.3 Create POST /api/device/refresh endpoint (device refresh → new access token)
- [x] 6.4 Create POST /api/admin/devices/{id}/revoke endpoint (revoke device)

## 7. Integration

- [x] 7.1 Install JwtAuthPlugin in Application.module()
- [x] 7.2 Install RbacPlugin in Application.module()
- [x] 7.3 Create configureAuthRoutes() function with all auth endpoints
- [x] 7.4 Verify build compiles with all auth infrastructure
