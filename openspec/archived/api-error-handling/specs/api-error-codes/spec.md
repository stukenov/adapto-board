## ADDED Requirements

### Requirement: Stable error codes enum
The system SHALL define an ErrorCode enum in libs/contracts containing all supported error codes grouped by domain.

#### Scenario: Auth error codes defined
- **WHEN** accessing auth-related error codes
- **THEN** the enum SHALL contain: INVALID_CREDENTIALS, TOKEN_EXPIRED, TOKEN_INVALID, FORBIDDEN_ROLE, DEVICE_NOT_ENROLLED, ENROLL_CODE_EXPIRED, ENROLL_CODE_USED

#### Scenario: Tenant error codes defined
- **WHEN** accessing tenant-related error codes
- **THEN** the enum SHALL contain: TENANT_NOT_FOUND, TENANT_SUSPENDED, TENANT_QUOTA_EXCEEDED

#### Scenario: Asset error codes defined
- **WHEN** accessing asset-related error codes
- **THEN** the enum SHALL contain: ASSET_NOT_FOUND, ASSET_NOT_READY, ASSET_TOO_LARGE, ASSET_INVALID_FORMAT

#### Scenario: Schedule error codes defined
- **WHEN** accessing schedule-related error codes
- **THEN** the enum SHALL contain: SCHEDULE_VERSION_CONFLICT, SCHEDULE_VERSION_IMMUTABLE, SCHEDULE_EMPTY, ROLLBACK_ASSETS_UNAVAILABLE

#### Scenario: Device error codes defined
- **WHEN** accessing device-related error codes
- **THEN** the enum SHALL contain: DEVICE_NOT_FOUND, DEVICE_REVOKED

#### Scenario: Overlay error codes defined
- **WHEN** accessing overlay-related error codes
- **THEN** the enum SHALL contain: OVERLAY_STATE_TOO_LARGE, CONNECTOR_FAILURE

#### Scenario: General error codes defined
- **WHEN** accessing general error codes
- **THEN** the enum SHALL contain: VALIDATION_ERROR, NOT_FOUND, INTERNAL_ERROR

### Requirement: Error code to HTTP status mapping
Each error code SHALL map to an appropriate HTTP status code according to the category: auth errors to 401/403, not found errors to 404, conflict errors to 409, validation errors to 400/422, internal errors to 500.

#### Scenario: Token expired maps to 401
- **WHEN** TOKEN_EXPIRED error code is used
- **THEN** the HTTP response status SHALL be 401 Unauthorized

#### Scenario: Forbidden role maps to 403
- **WHEN** FORBIDDEN_ROLE error code is used
- **THEN** the HTTP response status SHALL be 403 Forbidden

#### Scenario: Not found codes map to 404
- **WHEN** ASSET_NOT_FOUND, TENANT_NOT_FOUND, DEVICE_NOT_FOUND, or NOT_FOUND error code is used
- **THEN** the HTTP response status SHALL be 404 Not Found

#### Scenario: Version conflict maps to 409
- **WHEN** SCHEDULE_VERSION_CONFLICT error code is used
- **THEN** the HTTP response status SHALL be 409 Conflict
