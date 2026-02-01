## Context

Playout Edge — B2B SaaS для digital signage. Требуется два типа аутентификации:
1. **Admin users** — web-интерфейс для управления контентом
2. **Devices** — Android TV плееры, работающие автономно

Текущее состояние: есть database schema с таблицами users, devices, enroll_codes. Нужна реализация auth логики.

Зависимости: libs/auth модуль, java-jwt библиотека, bcrypt для паролей.

## Goals / Non-Goals

**Goals:**
- JWT-based аутентификация для admin users
- Device enrollment через одноразовые коды
- RBAC с ролями TenantAdmin и Operator
- Route-level authorization middleware
- Signed URLs для protected assets (подготовка)

**Non-Goals:**
- OIDC/SSO интеграция (R1 roadmap)
- MFA (future)
- Session clustering (single instance v1)
- Granular permissions beyond roles

## Decisions

### 1. JWT Structure

**Решение:** Два типа токенов с разными claims

**Admin JWT:**
```json
{
  "sub": "user-uuid",
  "tid": "tenant-uuid",
  "role": "TenantAdmin",
  "type": "admin",
  "exp": 1234567890
}
```

**Device JWT:**
```json
{
  "sub": "device-uuid",
  "tid": "tenant-uuid",
  "cid": "channel-uuid",
  "type": "device",
  "exp": 1234567890
}
```

**Альтернативы:**
- Opaque tokens + introspection — дополнительный round-trip на каждый запрос
- Session cookies only — не подходит для device auth

### 2. Token Lifetimes

**Решение:**
- Admin access token: 15 минут
- Admin refresh token: 7 дней (httpOnly cookie)
- Device access token: 1 час
- Device refresh token: 90 дней

**Rationale:** Короткий access token минимизирует window для compromise. Device токены длиннее т.к. устройства работают автономно.

### 3. Password Hashing

**Решение:** bcrypt с cost factor 12

**Альтернативы:**
- Argon2 — лучше, но bcrypt достаточен и широко поддержан
- PBKDF2 — слабее против GPU attacks

### 4. Auth Module Structure (libs/auth)

```
libs/auth/
├── JwtService.kt        — генерация/валидация JWT
├── PasswordService.kt   — bcrypt hashing
├── TokenClaims.kt       — data classes для claims
├── AuthConfig.kt        — конфигурация (secrets, TTLs)
└── RolePermissions.kt   — маппинг ролей на permissions
```

### 5. Ktor Auth Plugin Integration

**Решение:** Использовать Ktor Authentication plugin с custom JWT provider

```kotlin
install(Authentication) {
    jwt("admin-jwt") { ... }
    jwt("device-jwt") { ... }
}

authenticate("admin-jwt") {
    route("/api/admin") { ... }
}
```

### 6. RBAC Enforcement

**Решение:** Custom Ktor plugin `RbacPlugin` проверяющий role из JWT против required permissions

```kotlin
route("/users") {
    requireRole(Role.TenantAdmin)
    post { ... }
}
```

**Альтернативы:**
- Annotation-based — Ktor не поддерживает нативно
- Service-level checks — дублирование, легко забыть

### 7. Device Enrollment Flow

```
1. Admin: POST /api/admin/enroll-codes → { code: "ABC123", expiresAt: ... }
2. Device: POST /api/device/enroll { code, deviceInfo } → { accessToken, refreshToken }
3. Device: GET /api/device/schedule (с JWT)
```

**Enroll code:** 6 alphanumeric characters, uppercase, TTL 30 минут

### 8. Refresh Token Storage

**Решение:**
- Admin refresh tokens: httpOnly secure cookie
- Device refresh tokens: возвращаются в response, device хранит в secure storage

**Альтернативы:**
- Database stored sessions — overhead для v1
- Redis sessions — добавляет infra dependency

## Risks / Trade-offs

**[Risk] JWT secret compromise**
→ Mitigation: Environment variable, rotation procedure documented

**[Risk] Refresh token theft**
→ Mitigation: httpOnly cookies for admin, device tokens bound to device_id

**[Trade-off] No token revocation list**
→ Short access token TTL limits exposure. Device revoke marks device as revoked in DB, checked on refresh.

**[Trade-off] Role stored in JWT**
→ Role changes require re-login. Acceptable for v1, infrequent operation.

**[Risk] Enroll code brute force**
→ Mitigation: 6 chars = 2B combinations, rate limiting on endpoint, short TTL
