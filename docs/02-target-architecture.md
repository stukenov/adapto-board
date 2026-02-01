# 02 — Целевая архитектура (минимум систем, full-stack Kotlin)

## 1) Архитектурный принцип v1

“Один сервис, одна БД, один тип клиента (Android TV)”.

Это снижает:
- стоимость разработки (одна codebase, один язык),
- операционную сложность (меньше точек отказа),
- время пилота (меньше интеграций).

## 2) Минимальный технологический стек (рекомендация)

### 2.1 Backend (Kotlin)

- Kotlin 2.x
- Ktor (HTTP API + SSE)
- Kotlinx Serialization (JSON)
- PostgreSQL (основной storage)
- Flyway (миграции)
- Exposed (ORM)
- Micrometer + Prometheus (метрики), structured logs (JSON)

Опционально (только при необходимости):
- S3-compatible storage (MinIO/AWS S3) для production
- CDN перед storage (CloudFront/Fastly/Cloudflare) при росте трафика
- Sentry/OpenTelemetry collector (если уже есть в компании)

### 2.2 Admin UI — Web (Kotlin)

Цель: web-админка на Kotlin (без отдельной JS-кодовой базы).

Фиксируем v1: **Compose Multiplatform for Web на Kotlin/JS** (одна Kotlin-кодовая база для web UI).

Деплой v1 (минимум систем):
- UI собирается в статические файлы и отдаётся тем же Kotlin-монолитом (один домен, один deployable).
- В R1 допускается вынести статику на CDN, если это снижает нагрузку/ускоряет загрузку.


## 3) Компонентная модель (внутри монолита)

Монолит разбивается на модули (границы — логические, деплой один):

- `auth` (RBAC, JWT, OIDC в R1)
- `tenancy` (изоляция, квоты)
- `assets` (upload, статусы, хранение, выдача подписанных URL)
- `channels` (канал, версии расписания)
- `scheduling` (расчёт плейлиста/окна показа)
- `devices` (enroll, assignment, heartbeat, конфиг)
- `overlay` (SSE стрим, state+patch, источники данных)
- `audit` (audit log)
- `asrun` (фактический лог воспроизведения)
- `ops` (health, metrics, admin-only endpoints)

## 4) Потоки (end-to-end)

### 4.1 Публикация контента

1. Operator загружает `Asset` (видео/изображение).
2. Backend валидирует (формат/размер/длительность), присваивает `READY` или `REJECTED`.
3. Asset хранится в storage (local disk/S3).
4. Админ добавляет Asset в `Schedule Draft`.
5. Publish создаёт `ScheduleVersion` с неизменяемым набором `ScheduleItem`.

### 4.2 Конфиг и плейлист на устройстве

1. Device enroll → получает device credentials.
2. Device регулярно `GET /player/config`:
   - channel assignment,
   - ссылка на “активную версию расписания”,
   - overlay binding,
   - политика кэширования.
3. Device скачивает manifest плейлиста и недостающие assets (предзагрузка).
4. Playback идёт из локального кэша; при сети — догоняет изменения.

### 4.3 Overlay (realtime data layer)

1. Device открывает `SSE /player/overlay/stream`.
2. Backend шлёт `state` при подключении, далее `patch` события.
3. Источники данных обновляются:
   - `REST pull` (по расписанию),
   - `webhook push`,
   - `manual` (в UI).
4. Backend нормализует данные в “overlay state” и публикует в SSE.

### 4.4 Audit и as-run

- Audit: каждое изменение (asset/channel/schedule/device/overlay) пишет событие с `who/when/what`.
- As-run: устройство отправляет события воспроизведения (coarse) либо периодические “срезы текущего item”.

## 5) Multi-tenant изоляция (минимально)

Инвариант: почти каждая таблица содержит `tenant_id`.

Технически:
- application-level checks (обязательные фильтры tenant_id)
- опционально Postgres Row Level Security (RLS) в R1/R2 (когда возрастает риск ошибок)

## 6) “Меньше систем”: как заменить инфраструктурные зависимости

### 6.1 Без Redis / брокеров

- Кэш конфигов и вычислений: локальный in-memory (Caffeine) с TTL.
- “События для SSE”: Postgres `LISTEN/NOTIFY` или таблица outbox + polling.
- Фоновые задачи: один scheduler в монолите + Postgres advisory lock, чтобы не было двух лидеров при нескольких инстансах.

### 6.2 Без отдельного transcoding service

Для пилота:
- только валидация: “если не совместимо — не принимаем”.

Для production (опционально):
- ffmpeg как subprocess внутри того же сервиса (job queue в Postgres) — всё ещё одна система, но появляется системная зависимость.

## 7) Масштабирование (эволюция без переписывания)

### 7.1 Pilot (50–200 экранов)

- 1 инстанс монолита
- Postgres (managed или single)
- local storage

### 7.2 Production (1k–10k экранов)

- 2–3 инстанса монолита (HA) + sticky-free SSE (SSE держится на одном инстансе; это нормально)
- storage S3-compatible + CDN
- отдельные read replicas Postgres (если отчёты/лог as-run)

### 7.3 Scale (10k+)

- сегментация по tenant (шардирование по БД, если нужно)
- edge cache / branch-cache (если трафик становится доминирующим расходом)
