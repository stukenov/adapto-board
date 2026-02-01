# Authentication — Spec

## Requirements

### REQ-AUTH-001: Admin JWT Authentication

#### Scenario: Successful login

- **WHEN** user отправляет valid email и password
- **THEN** система возвращает JWT access token
- **AND** устанавливает httpOnly refresh token cookie
- **AND** записывает audit event LOGIN

#### Scenario: Invalid credentials

- **WHEN** user отправляет invalid credentials
- **THEN** система возвращает 401 Unauthorized
- **AND** не выдаёт tokens

#### Scenario: Token refresh

- **WHEN** access token истёк
- **AND** refresh token валиден
- **THEN** система выдаёт новый access token

### REQ-AUTH-002: Device Enrollment

#### Scenario: Generate enroll code

- **WHEN** admin генерирует enroll code
- **THEN** система создаёт одноразовый code
- **AND** code имеет TTL (10-30 минут)
- **AND** опционально привязан к channel

#### Scenario: Device enroll

- **WHEN** device отправляет valid enroll code и device info
- **THEN** система создаёт device record
- **AND** выдаёт device refresh token
- **AND** выдаёт device JWT
- **AND** помечает code как использованный

#### Scenario: Enroll code expired

- **WHEN** device отправляет expired enroll code
- **THEN** система возвращает 401 с кодом ENROLL_CODE_EXPIRED

#### Scenario: Enroll code already used

- **WHEN** device отправляет уже использованный code
- **THEN** система возвращает 401 с кодом ENROLL_CODE_USED

### REQ-AUTH-003: Device Token Management

#### Scenario: Device token refresh

- **WHEN** device JWT истёк
- **AND** device refresh token валиден
- **THEN** система выдаёт новый device JWT

#### Scenario: Device revoked

- **WHEN** admin revokes device
- **THEN** все tokens устройства становятся invalid
- **AND** device получает 401 на следующем запросе

### REQ-AUTH-004: Token Scope

#### Scenario: Device token limited scope

- **WHEN** device пытается вызвать admin endpoint
- **THEN** система возвращает 403 Forbidden

#### Scenario: Admin token tenant scope

- **WHEN** admin пытается получить данные другого tenant
- **THEN** система возвращает 404 или 403

## RBAC Requirements

### REQ-RBAC-001: Role Definitions

#### Scenario: TenantAdmin permissions

- **WHEN** user имеет роль TenantAdmin
- **THEN** он может: управлять users, channels, assets, devices, overlay, settings
- **AND** видеть audit и reports

#### Scenario: Operator permissions

- **WHEN** user имеет роль Operator
- **THEN** он может: управлять channels, assets, overlay (data)
- **AND** не может: управлять users, tenant settings

### REQ-RBAC-002: Route Protection

#### Scenario: Protected route without auth

- **WHEN** request без token обращается к protected route
- **THEN** система возвращает 401

#### Scenario: Insufficient role

- **WHEN** Operator пытается создать user
- **THEN** система возвращает 403 FORBIDDEN_ROLE
