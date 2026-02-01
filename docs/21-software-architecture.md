# 21 — Архитектура ПО (Software Architecture Spec)

Этот документ дополняет `docs/02-target-architecture.md` уровнем “как именно это будет устроено в коде”: модули, границы ответственности, ключевые runtime-потоки, контрактные библиотеки, подход к многотенантности, фоновые задачи, SSE и тестовая стратегия.

Ограничения v1 (не обсуждаются в реализации):
- Весь стек на Kotlin.
- Admin UI — только web.
- Устройства — только Android TV.
- Backend — один deployable Kotlin монолит + Postgres; минимум внешних систем.

## 1) Архитектурные драйверы (что определяет дизайн)

1. **Надёжность воспроизведения** важнее сложности фич: игрок должен жить в плохих сетях.
2. **Меньше систем**: предпочтение встроенным механизмам Postgres, библиотекам и in-process решениям.
3. **B2B эксплуатация**: аудит, наблюдаемость, предсказуемые релизы и совместимость.
4. **Единые контракты**: shared Kotlin DTO/contract library между server и client apps.

## 2) Монорепо и модульная структура (Gradle)

Цель: один репозиторий, но жёсткие границы модулей, чтобы монолит не превратился в “большой файл”.

Рекомендуемая структура:

- `apps/server` — Ktor монолит (HTTP, SSE, jobs, migrations, storage adapters)
- `apps/admin-web` — не отдельное приложение; web-админка v1 — SSR внутри `apps/server`
- `apps/player-androidtv` — Android TV player (Media3 + Compose overlay)
- `libs/contracts` — Kotlin DTO + OpenAPI schema sources (если применимо)
- `libs/domain` — доменная модель и правила (без Ktor/DB/JSON)
- `libs/persistence` — доступ к Postgres (Exposed), транзакции, миграции
- `libs/auth` — RBAC, JWT/OIDC адаптеры, device auth
- `libs/storage` — интерфейсы storage (LOCAL/S3) + подписанные URL
- `libs/overlay` — state+patch модель, редьюсер, сериализация событий SSE
- `libs/observability` — метрики/логирование/корреляция

Правило зависимостей:
- `domain` ни от чего не зависит.
- `contracts` не зависит от server/UI/Android.
- `server` зависит от `domain + contracts + persistence + auth + storage + overlay + observability`.

## 3) DDD-lite: доменная модель и инварианты

### 3.1 Aggregate boundaries (минимальные)

- `Tenant` (контейнер изоляции и квот)
- `Channel` (корень, связывает расписание и overlay binding)
- `ScheduleVersion` (immutable после publish)
- `Device` (enroll/assign/heartbeat, жизненный цикл доступа)
- `Asset` (жизненный цикл загрузки/готовности)
- `OverlayBinding` (канал ↔ профиль ↔ источник данных)

### 3.2 Инварианты (must enforce в коде + в БД)

- Любой запрос по данным всегда scoped `tenant_id` (см. раздел 4).
- Нельзя публиковать расписание с `asset.status != READY`.
- `ScheduleVersion` после `PUBLISHED` не меняет `schedule_items`.
- `enroll_code` одноразовый и имеет TTL; повторное использование запрещено.
- Device token не даёт доступа к admin endpoints.

## 4) Многотенантность: enforcement pattern (anti-footgun)

Задача архитектора: сделать так, чтобы “забыть tenant filter” было сложно технически.

Рекомендованный паттерн:

- В Ktor pipeline на каждом admin request извлекать `TenantContext(tenantId, userId, roles)`.
- В persistence слой передавать `TenantId` как обязательный параметр во все репозитории.
- Запретить “сырой” доступ к DSL/SQL из HTTP handlers.

Пример интерфейса (идея, не код):
- `ChannelRepository.list(tenantId: TenantId, ...)`
- `AssetRepository.get(tenantId: TenantId, assetId: AssetId)`

Дополнительно (R1/R2):
- Postgres RLS, если риск ошибок многотенантности становится критичным.

## 5) API дизайн (единый стандарт ответов)

### 5.1 Версионирование

- `/api/admin/v1/...`
- `/api/player/v1/...`

### 5.2 Error envelope (единый формат)

Везде возвращаем:
- `code` (стабильный string)
- `message` (коротко)
- `details` (опционально)
- `requestId`

Примеры `code`:
- `ASSET_NOT_READY`
- `SCHEDULE_VERSION_CONFLICT`
- `DEVICE_NOT_ENROLLED`
- `FORBIDDEN_ROLE`
- `TENANT_QUOTA_EXCEEDED`

### 5.3 Pagination и фильтры

- Для list endpoints: `pageSize`, `pageToken` (или offset для MVP), сортировка по времени.
- Любые admin list endpoints фильтруются по `tenant_id`.

### 5.4 Idempotency

Где критично:
- `POST /enroll` и операции publish/assign — поддержать idempotency key (R1) или обеспечить безопасный повтор (MVP).

## 6) Фоновые задачи без внешних брокеров

Требования:
- задачи должны быть воспроизводимыми и наблюдаемыми,
- при нескольких инстансах не должно быть “двойного выполнения”.

Реализация v1:
- таблица `jobs` в Postgres (тип, payload, status, next_run_at, attempts)
- scheduler внутри монолита:
  - выбирает due jobs,
  - берёт Postgres advisory lock (per job id/type) перед выполнением,
  - пишет статус и метрики.

Use-cases jobs:
- overlay REST pull polling
- ретеншн/очистка as-run/audit
- “интеграционная нормализация” (если нужно)

## 7) Overlay: state+patch и SSE реализация

### 7.1 Модель данных overlay

Overlay state хранится в Postgres как документ:
- `overlay_states(tenant_id, channel_id, state_json, version, updated_at)`

`version` — монотонно растёт при каждом обновлении.

### 7.2 Patch формат v1 (domain patch, стабильная схема)

Фиксируем v1: **domain patch по `widgetId`**.

Рекомендуемая форма `patch` (концептуально):
- `upsert`: список виджетов (id + payload), которые нужно создать/обновить
- `remove`: список `widgetId`, которые нужно удалить/скрыть

Требования к применению на клиенте:
- операции идемпотентны (повторный `upsert` не ломает состояние),
- неизвестные `widgetId` в `remove` игнорируются,
- валидация размера/схемы перед применением (защита от “сломать экран данными”).

### 7.3 SSE runtime

- Подписка: `GET /api/player/v1/overlay/stream?channelId=...`
- При connect: сервер отправляет `state` (полный) + `version`.
- Далее: `patch` события с `version`.
- Keepalive каждые N секунд.
- При пропуске версии: клиент запрашивает/ожидает новый `state`.

Серверная реализация без брокера:
- in-memory pub/sub per channel внутри инстанса,
- триггер обновления через:
  - прямое update (manual/webhook),
  - job polling (REST pull),
  - опционально Postgres `LISTEN/NOTIFY` для multi-instance синхронизации.

## 8) Assets storage и доставка (без лишних систем)

### 8.1 Абстракция storage

Интерфейс:
- `put(key, stream, metadata)`
- `getSignedUrl(key, ttl, constraints)`
- `delete(key)`

Адаптеры:
- `LOCAL` (файловая система + nginx/CDN позже)
- `S3` (MinIO/AWS) для production

### 8.2 Upload pipeline v1

- upload → сохранение → checksum → статус READY/REJECTED
- перекодирование не делаем по умолчанию (только после требования/подтверждения)

## 9) Аутентификация и авторизация (практический минимум)

Admin:
- JWT (pilot) → OIDC (R1)
- RBAC enforcement на уровне route groups и сервисного слоя.

Player:
- enroll code → refresh token → short-lived device JWT
- revoke устройства: делает device JWT недействительным (через version/rotation).

Signed URL:
- короткий TTL
- привязка к `tenantId` и `assetId` в подписи

Audit:
- middleware, которое пишет “кто/что/когда” для важных команд.

## 10) Observability by design

Обязательное:
- `requestId` в каждый ответ и лог
- structured logs (JSON) с `tenantId`, `userId/deviceId` (если есть)
- метрики (Micrometer) по API, SSE connections, jobs, DB pool
- health endpoints:
  - liveness
  - readiness (DB + storage доступность)

## 11) Тестовая стратегия (реалистичная для MVP)

### 11.1 Unit tests

- доменные инварианты (publish rules, RBAC decisions)
- overlay reducer (state+patch)

### 11.2 Integration tests

- репозитории + миграции на Postgres (Testcontainers)
- ключевые API команды (publish, enroll, assign, overlay update)

### 11.3 Contract tests (полезно при трёх приложениях)

- `libs/contracts` служит “source of truth” DTO
- сериализация совместима между server ↔ admin-web ↔ android-tv

### 11.4 Smoke tests для пилота

- “publish применился на устройстве”
- “overlay обновился”
- “offline cache проигрывает контент”

## 12) Performance/scale guardrails (чтобы не упереться неожиданно)

- Ограничить число SSE соединений на инстанс (конфиг), мониторить рост.
- Ограничить размер overlay state (например, 256KB–1MB) и валидировать.
- As-run: батчирование на player, ретеншн на server, индексы по времени.
- Кэширование:
  - in-memory на server (Caffeine) для config/playlist manifests
  - на player — disk cache Media3

## 13) Зафиксированные решения v1 (без открытых вопросов)

Эти решения уже приняты и должны соблюдаться при реализации:

1) Admin UI: только web (см. `docs/DECISIONS.md`, D006).
2) Admin web runtime: SSR внутри Ktor (см. `docs/DECISIONS.md`, D007).
3) Persistence: Exposed (см. `docs/DECISIONS.md`, D008).
4) Overlay patch format: domain patch по `widgetId` (см. `docs/DECISIONS.md`, D009).
5) Storage: pilot — LOCAL, production — S3-compatible по необходимости (см. `docs/01-requirements-and-assumptions.md` и `docs/02-target-architecture.md`).
