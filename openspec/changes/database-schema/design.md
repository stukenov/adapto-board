# Database Schema — Design

## Context

Playout Edge использует PostgreSQL 16 как единственную БД. Схема должна поддерживать:
- Multi-tenant SaaS модель с изоляцией данных
- Immutable schedule versions для auditability
- Полный audit trail всех изменений
- Эффективные запросы для player polling и admin UI

Текущее состояние: проект инициализирован, persistence модуль пустой.

## Goals / Non-Goals

**Goals:**
- Flyway миграции для production-ready schema
- Exposed DAO entities для всех таблиц
- Indexes для типичных query patterns
- tenant_id во всех таблицах (кроме jobs)
- Soft delete где нужно (status field)

**Non-Goals:**
- Partitioning (R1+)
- Read replicas (ops concern)
- Full-text search (R1+)
- Data archival strategy (R1+)

## Decisions

### Decision 1: Migration Strategy

**Choice:** Flyway с SQL миграциями (не Java-based)

**Rationale:**
- SQL миграции reviewable и portable
- Flyway встроен в Spring/Ktor экосистему
- Версионирование: V001, V002, ... (не timestamps)

**Alternatives considered:**
- Liquibase: более сложный, overkill для наших нужд
- Exposed auto-DDL: не production-safe, нет versioning

### Decision 2: Multi-tenant Isolation

**Choice:** Shared database, shared schema, tenant_id column

**Rationale:**
- Простота операций (одна БД)
- Row-level isolation через WHERE clauses
- Масштабируется до тысяч tenants
- Можно добавить RLS позже

**Alternatives considered:**
- Schema per tenant: операционно сложно, миграции per-schema
- Database per tenant: overkill для SaaS, дорогие операции

### Decision 3: Primary Keys

**Choice:** UUID v4 для всех primary keys

**Rationale:**
- Глобально уникальны
- Безопасны для expose в URLs
- Распределённо генерируемы
- PostgreSQL native uuid type

**Alternatives considered:**
- SERIAL/BIGSERIAL: sequential, guessable, centralized generation
- UUIDv7: не стабильный стандарт

### Decision 4: Timestamps

**Choice:** `timestamptz` для всех временных полей

**Rationale:**
- Хранит UTC internally
- Автоматическое timezone handling
- PostgreSQL best practice

**Naming convention:**
- `created_at` — момент создания
- `updated_at` — последнее изменение (где нужно)
- `published_at`, `ack_at` — специфичные события

### Decision 5: JSONB Fields

**Choice:** JSONB для schema-flexible данных

**Используется в:**
- `overlay_profiles.definition_json` — layout определение
- `overlay_bindings.source_config_json` — конфиг источника
- `overlay_states.state_json` — текущее состояние
- `audit_log.diff_json` — изменения
- `device_actions.params_json` — параметры команды

**Rationale:**
- Гибкость без schema migrations
- Queryable (GIN indexes)
- PostgreSQL native

### Decision 6: Enums

**Choice:** PostgreSQL native ENUM types

```sql
CREATE TYPE tenant_status AS ENUM ('ACTIVE', 'SUSPENDED', 'MAINTENANCE');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'LOCKED');
CREATE TYPE user_role AS ENUM ('TENANT_ADMIN', 'OPERATOR', 'SUPPORT_AGENT', 'SUPPORT_ADMIN');
CREATE TYPE device_enroll_status AS ENUM ('PENDING', 'ENROLLED', 'REJECTED');
CREATE TYPE asset_type AS ENUM ('VIDEO', 'IMAGE');
CREATE TYPE asset_status AS ENUM ('UPLOADING', 'PROCESSING', 'READY', 'REJECTED', 'ARCHIVED');
CREATE TYPE channel_status AS ENUM ('ACTIVE', 'PAUSED');
CREATE TYPE schedule_state AS ENUM ('DRAFT', 'PUBLISHED', 'ROLLED_BACK');
CREATE TYPE binding_status AS ENUM ('ACTIVE', 'PAUSED');
CREATE TYPE overlay_source_type AS ENUM ('REST_PULL', 'WEBHOOK');
CREATE TYPE asrun_event_type AS ENUM ('PLAY_START', 'PLAY_END', 'SKIP', 'ERROR');
CREATE TYPE action_type AS ENUM ('FORCE_CONFIG_REFRESH', 'FORCE_PLAYLIST_REFRESH', 'ROTATE_DEVICE_TOKEN');
CREATE TYPE action_status AS ENUM ('PENDING', 'ACKED', 'FAILED', 'EXPIRED');
CREATE TYPE alert_type AS ENUM ('DEVICE_OFFLINE', 'PLAYBACK_ERROR', 'STORAGE_QUOTA');
CREATE TYPE alert_status AS ENUM ('OPEN', 'ACKNOWLEDGED', 'RESOLVED');
CREATE TYPE job_status AS ENUM ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED');
CREATE TYPE contact_type AS ENUM ('TECHNICAL', 'BUSINESS', 'BILLING');
```

**Rationale:**
- Type safety на уровне БД
- Меньше storage vs strings
- Compile-time checks в Kotlin через mapping

### Decision 7: Indexes Strategy

**Core indexes:**
- All foreign keys (automatic with constraints)
- `tenant_id` на всех таблицах (первый в composite)
- `created_at DESC` для списков
- Unique constraints как defined в spec

**Example indexes:**
```sql
CREATE INDEX idx_devices_tenant_last_seen ON devices(tenant_id, last_seen_at DESC);
CREATE INDEX idx_audit_tenant_created ON audit_log(tenant_id, created_at DESC);
CREATE INDEX idx_asrun_tenant_device_at ON asrun_events(tenant_id, device_id, at DESC);
```

### Decision 8: Exposed Mapping

**Entity structure:**
```kotlin
object Tenants : UUIDTable("tenants") {
    val name = varchar("name", 255)
    val status = enumerationByName<TenantStatus>("status", 20)
    val createdAt = timestamp("created_at")
}

class Tenant(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Tenant>(Tenants)
    var name by Tenants.name
    var status by Tenants.status
    var createdAt by Tenants.createdAt
}
```

## Risks / Trade-offs

**[Risk] Large audit_log table** → Partition by created_at monthly (R1+), retention policy

**[Risk] JSONB schema drift** → Validate in application layer, version schemas

**[Risk] UUID performance** → Acceptable for our scale, use uuid_generate_v4()

**[Trade-off] Shared schema isolation** → Simpler ops vs stronger isolation. RLS can be added later if needed.

**[Trade-off] No soft deletes by default** → Use status fields (ARCHIVED) where business requires history, hard delete otherwise to reduce complexity.

## Migration Plan

1. V001: Create enum types
2. V002: Create tenants, users, user_roles
3. V003: Create devices, device_groups, device_group_members
4. V004: Create assets, asset_versions
5. V005: Create channels, schedule_versions, schedule_items
6. V006: Create overlay_profiles, overlay_bindings, overlay_states
7. V007: Create audit_log, asrun_events
8. V008: Create tenant_contacts, device_actions, alerts, jobs
9. V009: Create indexes

Each migration is idempotent and can be rolled back.
