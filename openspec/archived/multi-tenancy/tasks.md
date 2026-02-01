## 1. TenantId and TenantContext (libs/domain)

- [x] 1.1 Create TenantId value class wrapping UUID
- [x] 1.2 Create TenantContext data class with tenantId, userId, roles

## 2. TenantPlugin (apps/server)

- [x] 2.1 Create TenantPlugin that extracts TenantContext from JWT claims
- [x] 2.2 Add call.tenantContext extension property
- [x] 2.3 Install TenantPlugin in Application.module()

## 3. Repository Interfaces (libs/persistence)

- [x] 3.1 Create AssetRepository interface with TenantId-scoped methods
- [x] 3.2 Create ChannelRepository interface with TenantId-scoped methods
- [x] 3.3 Create DeviceRepository interface with TenantId-scoped methods
- [x] 3.4 Create ScheduleRepository interface with TenantId-scoped methods

## 4. Repository Implementations (libs/persistence)

- [x] 4.1 Implement AssetRepositoryImpl with tenant filtering
- [x] 4.2 Implement ChannelRepositoryImpl with tenant filtering
- [x] 4.3 Implement DeviceRepositoryImpl with tenant filtering
- [x] 4.4 Implement ScheduleRepositoryImpl with tenant filtering

## 5. Tenant Quotas (libs/domain + libs/persistence)

- [x] 5.1 Create TenantQuotas data class with storage, devices, connections limits
- [x] 5.2 Add quotas field to Tenant table/entity
- [x] 5.3 Create QuotaService interface for quota checking

## 6. Integration

- [x] 6.1 Verify build compiles with all multi-tenancy infrastructure
