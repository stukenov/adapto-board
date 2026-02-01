# Multi-Tenancy — Proposal

## Why

Playout Edge — multi-tenant SaaS с жёсткими требованиями изоляции данных. Главная задача архитектуры: сделать так, чтобы "забыть tenant filter" было технически сложно.

## What Changes

### TenantContext Pattern
- Извлечение `TenantContext(tenantId, userId, roles)` из каждого request
- Обязательный параметр `TenantId` во всех repository методах
- Запрет "сырого" доступа к DSL/SQL из HTTP handlers

### Tenant Lifecycle
- Создание tenant со статусом ACTIVE
- Suspension и reactivation
- Tenant-level quotas (storage, devices, SSE connections)

### Isolation Enforcement
- Application-level tenant_id filters (обязательно)
- Postgres RLS подготовка (для R1/R2)
- Тесты на cross-tenant access

## Capabilities

### New Capabilities
- `tenant-context`: TenantContext в каждом request
- `tenant-scoped-repos`: Все repositories принимают TenantId
- `tenant-quotas`: Лимиты на tenant level

## Impact

- `libs/domain/src/.../TenantContext.kt`
- `libs/persistence/src/.../` — все repositories
- `apps/server/src/.../plugins/TenantPlugin.kt`
