## 1. Flyway Setup

- [x] 1.1 Configure Flyway in libs/persistence module
- [x] 1.2 Create database connection setup with HikariCP

## 2. Enum Types Migration

- [x] 2.1 Create V001__create_enum_types.sql with all PostgreSQL enums

## 3. Core Tables Migrations

- [x] 3.1 Create V002__create_tenancy_tables.sql (tenants, users, user_roles)
- [x] 3.2 Create V003__create_device_tables.sql (devices, device_groups, device_group_members)
- [x] 3.3 Create V004__create_asset_tables.sql (assets, asset_versions)
- [x] 3.4 Create V005__create_schedule_tables.sql (channels, schedule_versions, schedule_items)
- [x] 3.5 Create V006__create_overlay_tables.sql (overlay_profiles, overlay_bindings, overlay_states)
- [x] 3.6 Create V007__create_audit_tables.sql (audit_log, asrun_events)
- [x] 3.7 Create V008__create_ops_tables.sql (tenant_contacts, device_actions, alerts, jobs)

## 4. Indexes Migration

- [x] 4.1 Create V009__create_indexes.sql with all performance indexes

## 5. Exposed Entities

- [x] 5.1 Create domain enums in libs/domain (TenantStatus, UserRole, etc.)
- [x] 5.2 Create Exposed table objects in libs/persistence (Tenants, Users, etc.)
- [x] 5.3 Create Exposed entity classes in libs/persistence

## 6. Database Configuration

- [x] 6.1 Create DatabaseFactory for connection pool and Flyway execution
- [x] 6.2 Add database configuration to server Application.kt

## 7. Verification

- [x] 7.1 Verify migrations run successfully on fresh database
