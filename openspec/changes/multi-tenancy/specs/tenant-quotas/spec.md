## ADDED Requirements

### Requirement: TenantQuotas data class
The system SHALL provide a TenantQuotas data class defining resource limits per tenant.

#### Scenario: Quota fields defined
- **WHEN** TenantQuotas is accessed
- **THEN** it SHALL contain: maxStorageBytes, maxDevices, maxConcurrentConnections

### Requirement: Default quotas for new tenants
The system SHALL apply default quota values when a tenant is created.

#### Scenario: New tenant gets default quotas
- **WHEN** a new tenant is created
- **THEN** default quotas SHALL be applied (e.g., 10GB storage, 100 devices, 50 connections)

### Requirement: Storage quota enforcement
The system SHALL check storage quota before allowing asset uploads.

#### Scenario: Upload within quota
- **WHEN** tenant uploads asset and total storage < maxStorageBytes
- **THEN** upload SHALL proceed

#### Scenario: Upload exceeds quota
- **WHEN** tenant uploads asset and total storage + new asset > maxStorageBytes
- **THEN** system SHALL reject with TENANT_QUOTA_EXCEEDED error

### Requirement: Device quota enforcement
The system SHALL check device quota before allowing new device enrollment.

#### Scenario: Enroll within quota
- **WHEN** tenant enrolls device and total devices < maxDevices
- **THEN** enrollment SHALL proceed

#### Scenario: Enroll exceeds quota
- **WHEN** tenant enrolls device and total devices >= maxDevices
- **THEN** system SHALL reject with TENANT_QUOTA_EXCEEDED error

### Requirement: Quota service
The system SHALL provide QuotaService for checking and managing tenant quotas.

#### Scenario: Check storage usage
- **WHEN** QuotaService.getStorageUsage(tenantId) is called
- **THEN** it SHALL return total bytes used by tenant's assets

#### Scenario: Check device count
- **WHEN** QuotaService.getDeviceCount(tenantId) is called
- **THEN** it SHALL return count of enrolled devices for tenant
