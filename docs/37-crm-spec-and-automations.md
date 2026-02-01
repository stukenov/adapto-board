# 37 — CRM spec & automations (под Playout Edge)

Дата: 2026-02-01  
Роль: Sales Ops / Founder / SDR  
Цель: описать CRM так, чтобы её можно было настроить за 1 день (HubSpot/Pipedrive/Airtable) и сразу начать продавать пилоты.

---

## 1) Объектная модель (минимум)

Используем стандартные сущности:
- **Company (Account)**
- **Contact**
- **Deal (Opportunity)**

Опционально (после 3–5 пилотов):
- **Partner** (если партнёрский канал активен)
- **Deployment** (если вы хотите трекать “контуры” отдельно от deals)

---

## 2) Pipeline стадий (фиксируем под пилотную модель)

Pipeline `Playout Edge — Pilot to Production`:
1) `Lead in` (новый лид)
2) `Qualified (QL)`
3) `Discovery scheduled`
4) `Discovery done (SQL)`
5) `Demo scheduled`
6) `Demo validated`
7) `Pilot proposed`
8) `Pilot agreed`
9) `Pilot in progress`
10) `Pilot passed`
11) `Production proposal`
12) `Closed won — Production`
13) `Closed lost`
14) `Nurture`

Правило UX в CRM: на каждой стадии должны быть “required fields” и “next step date”.

---

## 3) Поля (properties) — конкретно под продукт

### 3.1 Company fields
- `Segment` (enum): ICP-A Corporate / ICP-B Queue / Other
- `Vertical` (enum): Office/HR / Bank / Clinic / Gov / Manufacturing / Retail / Other
- `Geo` (text)
- `Screens_current` (number)
- `Screens_target_12m` (number)
- `AndroidTV_in_place` (enum): Yes / No / Mixed / Unknown
- `MDM_or_ManagedPlay` (enum): Yes / No / Unknown
- `Network_constraints` (multi-select): Proxy / DPI / Allowlist / On-prem-only / Unknown
- `SSO_required` (enum): Yes / No / Not sure
- `Procurement_complexity` (enum): Low / Medium / High

### 3.2 Contact fields
- `Persona` (enum): Buyer / Champion / Operator / IT Owner / Security / Integrator / Partner / Other
- `Preferred_language` (enum): RU / EN
- `Timezone` (text)

### 3.3 Deal fields (обязательные)
- `Primary_pain` (enum): Publish speed / Ops incidents / Data-layer / Compliance
- `Primary_use_case` (enum): Internal comms / Queue / KPI / Other
- `Pilot_scope_screens` (number)
- `Pilot_scope_locations` (number)
- `Pilot_data_source` (enum): Manual / REST pull / Webhook
- `Pilot_KPI_publish_p95_min` (number) default 10
- `Pilot_KPI_uptime_target` (text) default 99.9
- `Pilot_KPI_online_rate_target` (number) default 95
- `Pilot_KPI_overlay_latency_p95_sec` (number) default 2
- `Decision_date_target` (date)
- `Next_step_date` (date) **required**
- `Lead_source` (enum): Inbound / Outbound / Partner / Referral
- `UTM_source`, `UTM_medium`, `UTM_campaign`, `UTM_term`, `UTM_content` (text)
- `Competitor_in_play` (multi-select): SaaS signage / DIY / Enterprise AV / Unknown
- `Loss_reason` (enum): Multi-platform required / Wants visual editor / Price / Security blocker / No Android TV / No owner / Other

### 3.4 Compliance/consent fields (минимум для outbound)

Чтобы не убить репутацию домена/каналов и не попасть в блокировки, фиксируем поля:
- Contact:
  - `Consent_status` (enum): Unknown / Granted / Denied / Revoked
  - `Lawful_basis` (enum): Consent / LegitimateInterest / Contract / Other
  - `Consent_proof_ref` (text/url) — где лежит доказательство (форма, письмо, запись)
  - `Do_not_contact` (boolean)
- Company:
  - `Preferred_channels` (multi-select): Email / SMS / WhatsApp / Telegram / Calls

Техническая часть ingestion+очередей: `docs/45-lead-ingestion-llm-enrichment-and-consent-outreach.md`.

---

## 4) Автоматизации (минимум, чтобы экономить время)

### 4.1 Inbound forms → CRM
Trigger: submit “Request demo” или “Start pilot”.
Actions:
- create/update Contact + Company
- create Deal в стадии `Lead in`
- assign owner по правилу (round-robin или geo)
- set `Lead_source=Inbound`
- create task “Respond within 1 business day”

### 4.2 Авто‑письмо подтверждения (instant)
Trigger: inbound submit.
Email template RU:
Subject: `Получили заявку на Playout Edge`
Body:
- спасибо + ожидания времени ответа
- 3 вопроса для ускорения: screens, Android TV, data-layer
- ссылка на `/pilot` + “что входит/что не входит”

### 4.3 Discovery пакет (после назначения звонка)
Trigger: stage = `Discovery scheduled`.
Actions:
- отправить “Discovery agenda + подготовка” (чек‑лист: сеть/MDM/модели/данные)
- создать task “Fill pilot passport”

### 4.4 Pilot agreed → delivery handoff
Trigger: stage = `Pilot agreed`.
Actions:
- создать проект/таск‑лист (в Trello/Notion/Linear — что используете)
- email клиенту: kickoff agenda + prerequisites

---

## 5) Дашборды (чтобы управлять, а не угадывать)

Еженедельный:
- # new leads
- QL rate
- SQL rate
- pilot close rate
- pilot pass rate
- median time QL → Pilot agreed

Ежемесячный:
- pipeline value by stage
- win/loss reasons distribution
- segment performance (ICP-A vs ICP-B)

---

## 6) “One-click” шаблоны заметок (для скорости)

### 6.1 Discovery notes template
- Current setup (how content updates today)
- Screens now / target
- Network constraints
- Security/SSO requirement
- Data sources for overlay
- Pilot KPI thresholds agreed
- Owners (champion + IT)
- Next step + date

### 6.2 Win/Loss note template
- Outcome (won/lost)
- Competitors compared
- 3 decisive factors
- 1 thing to improve
