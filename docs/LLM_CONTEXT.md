# Playout Edge — LLM Context File

> Этот файл создан для быстрого понимания проекта LLM-системами (Claude, GPT, etc.).
> Если ты LLM — начни отсюда.

## Что это за проект

**Playout Edge** — SaaS-платформа автоматизации ТВ-вещания и digital signage. Управляет контентом на тысячах экранов (Android TV) через единую веб-консоль. Разработка компании **Adapto** (Казахстан).

## Техническая суть

- **Backend:** Kotlin/JVM + Ktor (Netty), монолит
- **DB:** PostgreSQL + Exposed DAO (lazy entity refs, map to DTO inside `newSuspendedTransaction {}`)
- **Frontend:** kotlinx.html SSR (attributes BEFORE content)
- **Auth:** JWT HMAC256, RBAC (ADMIN/OPERATOR/VIEWER/INTEGRATOR)
- **Realtime:** SSE для оверлеев
- **Player:** Android TV (Media3, Jetpack Compose)
- **Deploy:** Docker + Ansible → tv.adapto.kz (38.107.234.137)
- **MCP:** JSON-RPC 2.0 на `/api/mcp`, 24+ tools

## Структура репозитория

```
apps/server/         — Ktor-приложение (routes, views, services, MCP)
libs/persistence/    — Exposed DAO (entities, tables, migrations)
libs/auth/           — JWT, password hashing, sessions
deploy/              — Ansible, Docker Compose, nginx
docs/                — 45+ документов (архитектура, бизнес, GTM, sales)
docs/sales/          — Презентация, Sales Kit (docx/pptx)
docs/strategy/       — Стратегия продаж и маркетинга 2026
docs/CHANGELOG.md    — План изменений в коде под стратегию продаж
scripts/             — Утилиты
```

## Ключевые модули продукта

1. **Channels** — каналы вещания (ACTIVE/PAUSED/ARCHIVED)
2. **Assets** — медиабиблиотека (видео, изображения, HTML, слайдшоу) + S3/LOCAL
3. **Schedules** — расписания с версионностью (Draft → Published), откат
4. **Devices** — парк Android TV устройств (QR-enrollment, heartbeat, remote actions)
5. **Overlays** — реальное время: профили + состояние + SSE-стрим (< 2 сек)
6. **Audit** — полный лог всех действий с diff-ами
7. **As-Run** — запись воспроизведений (STARTED/ENDED/ERROR)
8. **Alerts** — алерты (offline, unavailable, quota exceeded)

## Публичный сайт

Маршруты: `/` (лендинг), `/features`, `/pricing`, `/faq`, `/contact`, `/signup`
SSR через kotlinx.html. CSS: Editorial luxury design system (DM Sans, stone palette, accent orange).

## Текущие тарифы на сайте vs стратегия

| | На сайте | В стратегии (B2B) |
|-|----------|-------------------|
| Базовый | $29/мес (10 устройств) | $500/мес (50 устройств) |
| Средний | $79/мес (50 устройств) | $2,000/мес (500 устройств) |
| Enterprise | custom | custom |

→ Нужно привести в соответствие. См. `CHANGELOG.md`.

## Карта документации

### Техническая
- `01-requirements-and-assumptions.md` — требования
- `02-target-architecture.md` — архитектура
- `03-data-model.md` — модель данных
- `04-api-contracts.md` — API контракты
- `21-software-architecture.md` — архитектура ПО

### Продуктовая
- `09-product-vision-and-positioning.md` — vision
- `10-icp-personas-and-jtbd.md` — ICP и персоны
- `12-pricing-and-packaging.md` — цены
- `15-mvp-backlog-r0.md` — MVP backlog

### Sales & Marketing
- `19-sales-playbook.md` — playbook продаж
- `33-competitive-battlecards.md` — battlecards
- `26-gtm-operating-system.md` — GTM операционка
- `sales/` — готовые материалы (pptx, docx)
- `strategy/` — стратегия 2026

### Операционная
- `06-ops-security-sre.md` — деплой и SRE
- `30-customer-success-and-support.md` — CS
- `37-crm-spec-and-automations.md` — CRM
- `CHANGELOG.md` — план изменений

## Что делать дальше

Текущий приоритет: реализация плана изменений из `CHANGELOG.md` — SEO, аналитика, русификация сайта, обновление тарифов, лидогенерация.
