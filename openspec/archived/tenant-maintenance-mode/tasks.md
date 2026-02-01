# Tenant Maintenance Mode — Tasks

## Phase 1: Database

- [x] 1.1 Add SupportTier and ReleaseRing enums
- [x] 1.2 Extend Tenants table with maintenance fields
- [x] 1.3 Create migration V012__add_tenant_maintenance_fields.sql
- [x] 1.4 Update TenantEntity

## Phase 2: Service

- [x] 2.1 Create MaintenanceService

## Phase 3: Plugin

- [x] 3.1 Create MaintenancePlugin

## Phase 4: Admin API

- [x] 4.1 Create MaintenanceRoutes with PUT and GET

## Phase 5: Integration

- [x] 5.1 Wire services and plugin
- [x] 5.2 Verify build
