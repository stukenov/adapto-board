# Database Schema — Proposal

## Why

Playout Edge использует PostgreSQL как единственный обязательный внешний компонент. Необходима продуманная схема БД с:
- Multi-tenant изоляцией (tenant_id везде)
- Версионированием расписаний
- Audit trail для всех изменений
- Правильными индексами для production нагрузки

## What Changes

- Создание Flyway миграций для всех таблиц
- Таблицы tenancy: `tenants`, `users`, `user_roles`
- Таблицы fleet: `devices`, `device_groups`, `device_group_members`
- Таблицы content: `assets`, `asset_versions`
- Таблицы scheduling: `channels`, `schedule_versions`, `schedule_items`
- Таблицы overlay: `overlay_profiles`, `overlay_bindings`, `overlay_states`
- Таблицы audit: `audit_log`, `asrun_events`
- Таблицы ops: `tenant_contacts`, `device_actions`, `alerts`, `jobs`
- Exposed DAO и repository layer

## Capabilities

### New Capabilities
- `database-migrations`: Flyway миграции с версионированием
- `tenant-isolation-schema`: Все таблицы с tenant_id
- `audit-trail-schema`: Структура для полного audit log
- `versioned-schedules-schema`: Immutable schedule versions

## Impact

- `libs/persistence/src/main/kotlin/` — Exposed entities и repositories
- `apps/server/src/main/resources/db/migration/` — Flyway миграции
