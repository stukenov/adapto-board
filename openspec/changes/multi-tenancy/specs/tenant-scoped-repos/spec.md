## ADDED Requirements

### Requirement: Repository methods require TenantId
All repository interfaces SHALL require TenantId as the first parameter for all data access methods.

#### Scenario: Find by ID requires tenant
- **WHEN** calling repository.findById(tenantId, entityId)
- **THEN** the query SHALL include WHERE tenant_id = tenantId AND id = entityId

#### Scenario: Find all requires tenant
- **WHEN** calling repository.findAll(tenantId)
- **THEN** the query SHALL include WHERE tenant_id = tenantId

#### Scenario: Create requires tenant
- **WHEN** calling repository.create(tenantId, data)
- **THEN** the created entity SHALL have tenant_id set to tenantId

### Requirement: Cross-tenant access prevention
Repository implementations SHALL prevent access to data from other tenants.

#### Scenario: Query with wrong tenant returns empty
- **WHEN** findById is called with tenantId A for an entity belonging to tenant B
- **THEN** the repository SHALL return null (not the entity)

#### Scenario: Update with wrong tenant fails
- **WHEN** update is called with tenantId A for an entity belonging to tenant B
- **THEN** the repository SHALL throw NotFound exception or return 0 rows affected

### Requirement: Base repository with tenant filtering
The system SHALL provide a base repository class/interface that enforces tenant filtering for all queries.

#### Scenario: All queries filtered by tenant
- **WHEN** any query is executed through the base repository
- **THEN** the WHERE clause SHALL always include tenant_id filter

### Requirement: AssetRepository interface
The system SHALL provide AssetRepository with tenant-scoped methods for asset management.

#### Scenario: Asset CRUD operations
- **WHEN** creating, reading, updating, or deleting assets
- **THEN** all operations SHALL require TenantId and filter by tenant

### Requirement: ChannelRepository interface
The system SHALL provide ChannelRepository with tenant-scoped methods for channel management.

#### Scenario: Channel CRUD operations
- **WHEN** creating, reading, updating, or deleting channels
- **THEN** all operations SHALL require TenantId and filter by tenant

### Requirement: DeviceRepository interface
The system SHALL provide DeviceRepository with tenant-scoped methods for device management.

#### Scenario: Device CRUD operations
- **WHEN** creating, reading, updating, or deleting devices
- **THEN** all operations SHALL require TenantId and filter by tenant
