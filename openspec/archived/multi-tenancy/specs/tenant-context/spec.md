## ADDED Requirements

### Requirement: TenantId value class
The system SHALL provide a TenantId value class that wraps UUID to ensure type-safe tenant identification.

#### Scenario: TenantId creation
- **WHEN** a TenantId is created with a UUID
- **THEN** the value class SHALL store the UUID and prevent accidental mixing with other UUID types

### Requirement: TenantContext data class
The system SHALL provide a TenantContext data class containing tenantId, userId (optional), and roles.

#### Scenario: Admin request context
- **WHEN** an authenticated admin request is processed
- **THEN** TenantContext SHALL contain tenantId from JWT, userId from JWT, and roles from JWT

#### Scenario: Device request context
- **WHEN** an authenticated device request is processed
- **THEN** TenantContext SHALL contain tenantId from JWT, no userId, and empty roles

### Requirement: TenantPlugin extracts context from JWT
The system SHALL implement a Ktor plugin that extracts TenantContext from JWT claims and stores it in call attributes.

#### Scenario: Valid admin JWT
- **WHEN** a request has valid admin JWT
- **THEN** TenantPlugin SHALL extract tenantId, userId, and role into TenantContext

#### Scenario: Valid device JWT
- **WHEN** a request has valid device JWT
- **THEN** TenantPlugin SHALL extract tenantId into TenantContext with null userId

#### Scenario: Missing or invalid JWT
- **WHEN** a request has no JWT or invalid JWT
- **THEN** TenantPlugin SHALL not set TenantContext (null)

### Requirement: TenantContext accessible via extension property
The system SHALL provide call.tenantContext extension property to access the current request's TenantContext.

#### Scenario: Access tenant context in handler
- **WHEN** a route handler needs tenant information
- **THEN** call.tenantContext SHALL return the TenantContext or null if not authenticated
