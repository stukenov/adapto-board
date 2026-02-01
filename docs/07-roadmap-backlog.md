# 07 — Роадмап, backlog, оценки, риски

## 1) Релизная стратегия (реально выполнимая, минимум систем)

### R0 — Pilot (4–6 недель)

Цель: 50–200 Android TV экранов, работа 24/7, базовая эксплуатация.

Скоуп:
- Tenants/Users/RBAC (минимум)
- Channels + Schedules (draft/publish)
- Assets upload + storage (local) + выдача URL
- Android TV Player:
  - enroll
  - config/playlist polling
  - disk cache
  - fallback
  - heartbeat + as-run (coarse)
- Overlay:
  - SSE
  - 3–5 виджетов
  - manual data + простой REST pull
- Audit log
- Минимальные метрики и health

Критерии готовности:
- “в любой момент на экране что-то корректно отображается”
- публикация расписания видна на 95% экранов ≤ 10 минут
- админ может увидеть offline devices и последнюю ошибку

### R1 — Production hardening (6–10 недель)

Цель: 1k+ экранов, enterprise требования, управляемая эксплуатация.

- OIDC/SSO + расширенный RBAC
- S3 storage + signed URL политика, CDN (если трафик)
- Улучшение fleet:
  - device groups
  - канареечные rollout-процессы (на уровне MDM/магазина)
  - команды “reboot/app restart” (если разрешено)
- Overlay:
  - state+patch строгая версия
  - webhook push источники
  - шаблоны маппинга данных
- Отчёты: audit/as-run экспорт (CSV)
- Ретеншн/архивация логов

### R2 — Scale pack (после подтверждения спроса)

- Branch cache / edge appliance (если себестоимость трафика доминирует)
- Offline-first расширенный режим (предзагрузка недельных плейлистов)
- Политики enterprise (allowlist, отчёты комплаенса)
- Мульти-БД/шардирование по tenant при необходимости

## 2) Эпики → user stories (MVP)

### Epic A: Каналы и расписание

- US-A1: Operator создаёт канал и видит список.
  - AC: канал создаётся только в рамках tenant, audit event CREATE.
- US-A2: Operator создаёт draft расписание и добавляет assets.
  - AC: draft не влияет на экраны.
- US-A3: Operator публикует расписание.
  - AC: создаётся immutable version, audit PUBLISH, устройства получают новую версию при следующем poll.

### Epic B: Контент (assets)

- US-B1: Upload видео/изображение.
  - AC: статусы UPLOADING→READY/REJECTED, checksum сохраняется.
- US-B2: Asset можно удалить (soft delete).
  - AC: удалённый asset не выдаётся в новых плейлистах.

### Epic C: Device enrollment и управление

- US-C1: Админ генерирует enroll code, пользователь вводит на Android TV.
  - AC: code одноразовый, TTL, устройство появляется в админке.
- US-C2: Админ назначает устройству канал.
  - AC: устройство при следующем poll начинает показывать канал.
- US-C3: Статусы устройств (online/offline) на дашборде.
  - AC: last_seen_at обновляется, offline threshold настраиваемый.

### Epic D: Playback устойчивость

- US-D1: Player кэширует медиа и играет из кэша при отсутствии сети.
  - AC: воспроизведение не останавливается при отключении сети на 30 минут.
- US-D2: Fallback при ошибке asset.
  - AC: пропуск item, error отправлен в heartbeat.

### Epic E: Overlay/data layer

- US-E1: Устройство подключается к SSE и отображает текстовый overlay.
  - AC: overlay обновляется ≤ 2 сек P95 при нормальной сети.
- US-E2: Manual state update из админки.
  - AC: изменение видно на всех устройствах канала.
- US-E3: REST pull источник.
  - AC: backend по расписанию обновляет overlay state и пушит patch в SSE.

### Epic F: Audit + As-run

- US-F1: Любое изменение пишет audit log.
  - AC: фильтры по entity и времени.
- US-F2: As-run события с устройства.
  - AC: можно увидеть “что показывалось” за период по устройству.

## 3) Команда и роли (минимально)

Для R0 (реалистично):

- 1 Kotlin fullstack engineer (Ktor/Postgres + SSR web admin UI)
- 1 Android TV engineer (Media3/Compose)
- 0.2–0.5 SRE/DevOps (инфра, мониторинг, релизы)
- продукт/проект (part-time) — чтобы пилот не расползся

## 4) Основные риски и как снижать

1. **Разный парк Android TV устройств** (кодеки/память/прошивки).
   - Митиг.: поддерживаемый список устройств v1, жёсткая валидация контента, fallback.
2. **Себестоимость трафика** при 1k+ экранов.
   - Митиг.: S3+CDN, профили качества, лимиты, позже branch cache.
3. **Сетевые ограничения у клиентов** (прокси, DPI).
   - Митиг.: SSE (а не WS) + короткие таймауты, polling fallback.
4. **Multi-tenant ошибки в коде**.
   - Митиг.: единый слой доступа к данным + тесты + (опционально) RLS в R1.

## 5) Оценка инфраструктурной себестоимости (черновая модель)

Драйверы затрат:
- storage (GB)
- egress (GB/мес) — самый большой риск
- DB size (audit/as-run)

Быстрая формула (для прикидки):

- В нашей модели Android TV **скачивает assets и крутит из кэша**, поэтому:
  - `egress_gb ≈ screens * U`, где `U` = GB новых/обновлённых медиа на экран в месяц.
- Формула “битрейт × часы” — верхняя оценка только для стриминга:
  - `egress_gb_streaming ≈ screens * hours_per_day * bitrate_mbps * 3600 * days / 8 / 1024`

Примеры (порядок величины):
- distribution: 200 экранов * 4 GB/экран/мес = ~0.8 TB/мес egress
- streaming upper bound: 200 экранов * 10 ч/день * 3 Mbps ≈ ~2.6 TB/мес egress

Это и определит необходимость CDN/branch-cache и лимитов на контент.
