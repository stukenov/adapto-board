## Context

Playout Edge — multi-tenant SaaS. Каждый tenant (клиент) имеет свои данные: assets, channels, devices. Критически важно предотвратить случайный доступ к данным другого tenant.

Текущее состояние: все таблицы имеют `tenant_id` column, но нет enforcement на уровне приложения.

## Goals / Non-Goals

**Goals:**
- TenantContext извлекается из JWT и доступен во всех handlers
- Все repository методы требуют TenantId как параметр
- Невозможно "забыть" tenant filter — compile-time enforcement
- Tenant quotas: storage, devices, concurrent connections

**Non-Goals:**
- Postgres RLS (Row Level Security) — отложено на R1/R2
- Шардирование данных по tenant
- Tenant-specific database connections

## Decisions

### 1. TenantContext Data Class

**Решение:** Value class `TenantId` + data class `TenantContext`

```kotlin
@JvmInline
value class TenantId(val value: UUID)

data class TenantContext(
    val tenantId: TenantId,
    val userId: UUID?,
    val roles: Set<UserRole>
)
```

**Альтернативы:**
- Plain UUID — теряем type safety, легко перепутать с другими UUID
- ThreadLocal context — не работает с coroutines

### 2. TenantContext из JWT

**Решение:** Ktor plugin `TenantPlugin` извлекает context из JWT claims и кладёт в call attributes

```kotlin
val TenantContextKey = AttributeKey<TenantContext>("TenantContext")

val call.tenantContext: TenantContext
    get() = attributes[TenantContextKey]
```

### 3. Repository Pattern с TenantId

**Решение:** Все repository методы принимают `TenantId` как первый параметр

```kotlin
interface AssetRepository {
    suspend fun findById(tenantId: TenantId, assetId: UUID): Asset?
    suspend fun findAll(tenantId: TenantId): List<Asset>
    suspend fun create(tenantId: TenantId, asset: CreateAssetRequest): Asset
}
```

**Альтернативы:**
- TenantContext в конструкторе repository — создаёт stateful repositories
- Implicit context из coroutine — сложно отлаживать, легко ошибиться

### 4. Tenant Quotas

**Решение:** `TenantQuotas` data class + проверка в service layer

```kotlin
data class TenantQuotas(
    val maxStorageBytes: Long,
    val maxDevices: Int,
    val maxConcurrentConnections: Int
)
```

Quotas хранятся в tenant settings (JSONB) или отдельной таблице.

### 5. Enforcement Strategy

**Решение:** Compile-time через типы + runtime validation

1. **Compile-time:** TenantId как value class, repository API требует TenantId
2. **Runtime:** Repository implementation добавляет WHERE tenant_id = ? ко всем queries
3. **Tests:** Integration tests проверяют cross-tenant isolation

## Risks / Trade-offs

**[Risk] Developer забывает использовать tenantId из context**
→ Mitigation: Code review, linting rules, integration tests

**[Trade-off] Verbose API (tenantId везде)**
→ Type safety важнее краткости. IDE autocomplete помогает.

**[Trade-off] Нет RLS на уровне Postgres**
→ Application-level enforcement достаточен для v1. RLS добавим позже для defense in depth.
