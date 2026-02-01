# 36 — Landing MVP spec (быстро, дёшево, под Playout Edge)

Дата: 2026-02-01  
Роль: Growth/Marketing + Sales + Founder  
Цель: описать лендинг так, чтобы его можно было сделать за 1–2 дня (Webflow/Framer/Tilda/Next.js) и сразу подключить к CRM.

Оффер и границы v1 (обязательная честность):
- **Android TV only**
- **Admin UI — web**
- **Value**: 24/7 playback + data-layer overlay + эксплуатация (fleet + audit/as-run)
- **Pilot**: 2–4 недели, 50–200 экранов, 1 data source + manual mode, KPI go/no-go

---

## 1) IA (структура страниц)

Минимальный набор:
1) `/` — основной лендинг (всё в одном).
2) `/pilot` — деталь пилота (scope, KPI, prerequisites, что не входит).
3) `/security` — “security overview” (коротко, для IT/procurement).
4) `/pricing` — упаковка/пакеты (без калькулятора на первом шаге).

Опционально:
- `/case-study/{client}` (1 шаблон + 1 реальный кейс после 1–2 пилотов).
- `/docs` (публичные выдержки: device requirements, supported media spec).

---

## 2) Лендинг `/` — блоки и готовый текст (RU)

### 2.1 Hero (above the fold)

H1:
> Android TV signage, которая работает 24/7 — и показывает данные, а не только видео.

Subheader:
> Playout Edge: web‑админка для каналов и расписаний, offline‑first Android TV плеер (кэш+fallback), realtime overlays (SSE) и эксплуатация уровня enterprise: статусы устройств, audit и as‑run.

Primary CTA:
- `Запросить демо` (форма/календарь)

Secondary CTA:
- `Запустить пилот (2–4 недели)`

Micro‑trust:
- “Pilot с KPI: publish p95 ≤ 10 мин, uptime ≥ 99.9%, online rate ≥ 95%.”
- “Android TV only (v1).”

### 2.2 “Кому подходит”

Карточки (3):
1) Корпоративные офисы и филиалы (HR/Comms + IT)
2) Сервисные точки с очередями (банки/клиники/гос)
3) KPI/операционные экраны (производства/склады/ops)

### 2.3 “3 ценности” (value pillars)

1) Надёжный показ 24/7  
Кэш + fallback. Работает в плохих сетях.

2) Data-layer поверх контента  
Очереди/KPI/уведомления обновляются в realtime (SSE). Не нужно рендерить “100 версий видео”.

3) Эксплуатация и контроль  
Статус каждого устройства, версии, ошибки. Audit изменений и as‑run отчёты.

### 2.4 “Как это работает” (4 шага)
1) Загружаете контент → создаёте канал и расписание → publish.  
2) Android TV кэширует и играет оффлайн при сбоях сети.  
3) Overlay подключает данные (manual / REST pull / webhook) и обновляется в realtime.  
4) IT видит парк: online/offline, версии, ошибки, аудит.

### 2.5 Proof points (что обещаем измеримо)

Таблица “метрика → цель”:
- Time‑to‑publish: P50 ≤ 2 мин, P95 ≤ 10 мин
- Playback uptime: ≥ 99.9% / мес
- Online rate: ≥ 95% (с учётом сетей)
- Overlay latency: P95 ≤ 2 сек

### 2.6 “Pilot offer”

Заголовок:
> Pilot за 2–4 недели на 50–200 экранах

Что входит:
- web‑админка, Android TV player, каналы/расписания, overlay, fleet, audit/as‑run
- manual overlay + 1 источник данных (REST pull или webhook)
- отчёт по KPI + план production

Что не входит (жёстко):
- другие платформы endpoints
- визуальный редактор “как Canva”
- 5 интеграций в пилоте

CTA: `Запустить пилот`

### 2.7 “Security (кратко)”

Пункты:
- RBAC (роль Operator/IT)
- device enroll + revoke
- signed URLs на медиа
- audit log изменений
- SSO/OIDC в production hardening (при необходимости)

CTA: `Скачать security overview` (ведёт на `/security`)

### 2.8 FAQ (обязательные)

Q: Поддерживаете Tizen/webOS/Windows?  
A: В v1 — только Android TV, чтобы гарантировать качество и снизить стоимость поддержки.

Q: Что если интернет пропал?  
A: Показ продолжается из дискового кэша; сеть нужна для обновлений и overlay.

Q: Наши прокси режут WebSocket.  
A: Мы используем SSE и polling fallback.

Q: Можно on‑prem?  
A: Да, по отдельному SOW; чаще всего на старте быстрее “isolated environment” (1 клиент = 1 контур).

---

## 3) Формы (lead capture) — поля и UX

### 3.1 Мини‑форма “Request demo” (1 экран)
- Full name
- Work email
- Company
- Role (dropdown): Operator / IT / Security / Integrator / Other
- Screens now (number)
- Android TV already? (Yes/No/Not sure)
- Primary use-case (dropdown): Internal comms / Queue / KPI / Other
- Message (optional)

UX требования:
- UTM сохранять в hidden fields (см. `docs/38-marketing-ops-tracking-and-analytics.md`).
- После submit: экран “что дальше” (ожидайте ответ < 1 business day) + ссылка на `/pilot`.

### 3.2 Форма “Start pilot”
Добавить обязательные:
- Locations count
- Network constraints (proxy/allowlist) (checkboxes)
- Need SSO? (Yes/No/Not sure)

---

## 4) SEO/тех‑минимум (чтобы не стыдно)

Требования:
- 1 primary keyword cluster: “Android TV digital signage”, “corporate TV”, “digital signage player”.
- Title/description уникальные на `/`, `/pilot`, `/security`, `/pricing`.
- OpenGraph + favicon.
- Скорость: LCP < 2.5s на мобильном (без тяжёлых видео).

---

## 5) Интеграция с CRM (обязательное)

Минимум:
- На submit формы создаётся **Lead/Contact + Company + Deal/Opportunity** в CRM.
- Проставляются:
  - source (UTM),
  - segment (ICP-A/B),
  - screens now,
  - use-case,
  - need SSO.

Спецификация CRM: `docs/37-crm-spec-and-automations.md`.

---

## 6) Acceptance criteria (лендинг считается готовым)

- Есть 4 страницы: `/`, `/pilot`, `/security`, `/pricing`.
- Две формы (demo + pilot) отправляют лиды в CRM с UTM.
- Есть авто‑письмо “получили заявку” (см. `docs/37-crm-spec-and-automations.md`).
- Копирайт соответствует продуктовым границам (Android TV only, pilot scope).

