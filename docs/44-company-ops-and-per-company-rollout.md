# 44 — Обслуживание компаний (Company Ops) и выкатки “per company”

Дата: 2026-02-01  
Цель: дописать “как реализуем правильно” два практических блока, которые часто ломают пилоты при маленьком бюджете:

1) обслуживание клиента (support/CS/delivery) внутри продукта без зоопарка систем;  
2) управляемые выкатки “по компаниям” (backend + player) без дорогого оркестратора.

Документ опирается на:
- `docs/DECISIONS.md` (особенно D010),
- `docs/06-ops-security-sre.md`,
- `docs/16-release-notes-rollout-and-comms.md`,
- `docs/21-software-architecture.md`,
- `docs/35-ux-service-blueprint-end-to-end.md` (раздел F “Инциденты и поддержка”).

---

## 1) Термины и режимы (важно для дешёвой реализации)

### 1.1 “Company” = tenant (продуктовый уровень)

В терминах продукта компания = `tenant` (организация/клиент).

### 1.2 Два режима эксплуатации (GTM → Scale)

**Режим A (рекомендовано до PMF): 1 компания = 1 контур (single-tenant per deployment)**  
Зафиксировано в `docs/DECISIONS.md` D010. Это самый дешёвый способ:
- ускорить пилоты (простая история для security/procurement),
- уменьшить риск мульти-тенантных багов,
- сделать “rollout per company” тривиальным (деплоим только конкретного клиента).

**Режим B (после PMF): shared multi-tenant**  
Один сервис обслуживает несколько `tenant` в одной среде. Требует более жёсткой дисциплины (tenant context, RLS/тесты/feature flags).

Практика: даже в режиме A данные остаются tenant-aware (`tenant_id` в таблицах), чтобы миграция к режиму B была миграцией инфраструктуры, а не переписыванием домена.

---

## 2) Company Ops: какой “доп. функционал обслуживания” нужен и как сделать дёшево

Задача Company Ops: минимизировать ручную переписку и время triage, дав support/CS **однозначные** данные по состоянию контуров, устройств и публикаций.

### 2.1 Роли и доступы (RBAC)

Добавляем “внутренние” роли (для наших сотрудников), отделяя их от ролей клиента:

- `SupportAgent` — доступ к диагностике и “support bundle” (read‑mostly), без прав на контент по умолчанию.
- `SupportAdmin` — расширенный доступ (например, временное включение maintenance mode, reset enroll codes, revoke devices).

Правило безопасности: любые “опасные” действия (`revoke`, `maintenance`, remote actions) требуют:
- явного подтверждения (UI modal + reason),
- записи в audit log,
- опционально “break-glass” режима (временный elevated доступ с TTL).

### 2.2 Минимальный набор страниц в Admin Web (support-grade)

Это то, что реально разгружает команду уже в R0/R1:

1) **Home (Ops)**: агрегаты health по устройствам / publish / overlay.
2) **Devices dashboard**: online/offline, app version, last error, last config/playlist, фильтры.
3) **Device detail**: текущий channel, scheduleVersion, текущий asset, SSE статус, последние ошибки.
4) **Publish tracker**: X/Y устройств применили версию + причины отставания.
5) **Overlay health**: “последний успешный pull/webhook”, ошибки коннекторов, размер state.
6) **Alerts list** (простая): online rate ниже порога, spike publish failures, connector failures.
7) **Support bundle**: “Copy support bundle” одной кнопкой.

UX/сценарии уже описаны в `docs/35-ux-service-blueprint-end-to-end.md` (раздел F); здесь фиксируем техническую реализацию.

### 2.3 “Support bundle” (дешёвый must-have)

**Идея:** вместо просьб “пришлите логи/версию/скрин” — система отдаёт один JSON (или ZIP), который можно сразу вставить в тикет/чат.

**Состав (минимум):**
- tenant: `tenant_id`, имя, tier (если есть), контакты
- device: `device_id`, `display_name`, hardware info, `app_version`, `android_version`
- runtime: `last_seen_at`, `last_config_at`, `last_playlist_at`, `sse_connected_at`, `current_asset_id`
- publish: `assigned_channel_id`, `schedule_version_id`, `applied_at`, `apply_status`, `apply_error`
- последние N heartbeat ошибок (коды + сообщения)
- последние N событий as-run (или summary)
- request correlation: `request_id` последних ключевых вызовов (если логируем)

**Где хранить:** не хранить долго. Генерировать “на лету” из DB + последних событий и отдавать как JSON; при необходимости — краткоживущий файл с TTL (например, 24 часа) в storage.

### 2.4 Remote actions (опционально, но полезно)

Дорогие и рискованные действия (reboot/clear cache) можно отложить до R1, но “дешёвые” команды дают эффект:

- `force_config_refresh` (устройство делает немедленный poll `/player/config`)
- `force_playlist_refresh`
- `rotate_device_token` (для security triage)

Технически: actions кладутся в DB (outbox/таблица команд), player забирает их на следующем poll и подтверждает ack.

### 2.5 Минимальные данные в БД для Company Ops

Не создаём отдельную “support систему”. Достаточно расширить модель:

- `tenants`:
  - `support_tier` (BASIC/…)
  - `release_ring` (STABLE/CANARY/BETA) — для feature gating и процессов
  - `maintenance_mode` (bool) + `maintenance_reason` + `maintenance_until`
- `tenant_contacts(tenant_id, type, name, email, phone, created_at)`
- `device_health_snapshots(tenant_id, device_id, at, state_json)` (опционально; можно начать без таблицы, используя heartbeat)
- `alerts(tenant_id, id, type, status, first_seen_at, last_seen_at, payload_json)`
- `device_actions(tenant_id, id, device_id, action, params_json, status, created_by, created_at, ack_at)`

Если хочется ещё дешевле: `tenants.*` + `device_actions` достаточно; alerts можно получать из метрик/логов без таблицы.

---

## 3) Rollout per company: как сделать правильно без дорогого оркестратора

Rollout = управление изменениями **в двух местах**:
- backend/admin (серверный деплой),
- Android TV player (клиентское приложение).

### 3.1 Backend rollout per company (режим A: 1 компания = 1 контур)

Самая дешёвая и надёжная схема:

1) Каждый клиент = отдельная среда (отдельный домен/поддомен, отдельная БД, отдельный storage root).
2) Один Docker image на версию (`app:1.2.3`), но конфиг разный per customer (`.env`/secrets).
3) Выкатка “на компанию” = деплой конкретного окружения (manual approval в CI + чек-лист).

**Группы/кольца (rings):**
- `CANARY`: 1–2 “дружелюбных” клиента для ранней проверки.
- `STABLE`: остальные.

**Rollback:** держим N последних образов, откат = `docker compose pull app:<prev>` + миграции “вперёд‑совместимые” (см. ниже).

### 3.2 Backend rollout per tenant (режим B: shared multi-tenant)

Если несколько tenants живут в одной среде, “rollout per company” делается через:

- **feature flags per tenant** (в БД),
- **compatibility gates** (например, “новый overlay schema только для CANARY tenants”),
- **rate limits/quotas per tenant**.

Это дешевле по инфраструктуре, но дороже по инженерным рискам. Поэтому по умолчанию — режим A.

### 3.3 Player rollout per company

Два дешёвых варианта:

**Вариант 1 (предпочтительно для enterprise): Managed Google Play / MDM группы**  
Определяем группы устройств (canary/stable) и обновляем приложение по группам (описано в `docs/16-release-notes-rollout-and-comms.md`).

**Вариант 2 (самый простой): один публичный трек + server-driven совместимость**  
Не пытаемся “развести” обновления по компаниям в сторе, а вместо этого:
- backend поддерживает N-1 версии player API,
- backend в `/player/config` отдаёт:
  - `min_supported_player_version`,
  - (опционально) `update_required=true` с дедлайном.

### 3.4 Миграции и совместимость (чтобы rollout был возможен)

Правило для маленькой команды: **только backward-compatible миграции** в обычных релизах.

Практика:
- `expand → migrate → contract` (двухшаговый/трёхшаговый паттерн),
- фичи прячутся за флагом до “contract” шага,
- если нужно “ломающее” изменение — это отдельный релиз с окном и планом отката (см. `docs/16-release-notes-rollout-and-comms.md`).

---

## 4) “Админка для выкладки” или скрипты: что дешевле и когда

### 4.1 MVP (до 5–10 клиентов): без отдельной админки деплоя

Не строим систему ради системы.
Дешевле и быстрее:
- хранить “описание окружений” как файлы (например, `ops/envs/<company>.env` и `ops/compose/<company>.yml`),
- деплоить через CI workflow с ручным выбором target company,
- иметь чек‑лист релиза + фиксированный “owner on call”.

Админка продукта (SSR) при этом обслуживает **только** контент/устройства/overlay и support-функции для конкретного контура.

### 4.2 Growth (10+ клиентов): лёгкий Control Plane для операций

Когда ручные выкладки начинают “съедать” время, добавляем минимальный “ops control plane”:
- список компаний/контуров,
- кольца rollout (CANARY/STABLE),
- кнопка “deploy version X to rings” (по сути UI над тем же CI/CD),
- health summary.

Важно: это отдельная подсистема; делать её раньше времени — лишние расходы.

---

## 5) Открытые вопросы (чтобы не ошибиться в assumptions)

Ответы на вопросы ниже определяют, насколько нужна “центральная админка” и какой режим (A/B) мы реально выбираем:

1) Сколько компаний ожидаем в первые 3 месяца: 1–3, 5–10, 20+?
2) У компаний обязателен “выделенный контур” по требованиям безопасности или это “nice to have”?
3) Кто будет обслуживать: 1–2 инженера + part-time support, или нужен отдельный support?
4) Как клиенты обновляют Android TV: MDM/managed Play есть у большинства или нет?
5) Нужно ли “управляемое окно изменений” (maintenance windows) на backend и на player?

