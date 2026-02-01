# 38 — Marketing ops: UTM, tracking, analytics (лендинг → CRM)

Дата: 2026-02-01  
Роль: Growth / Marketing Ops  
Цель: быстро получить управляемую атрибуцию и не “слепые” каналы.

---

## 1) UTM стандарт (фиксируем)

Соглашение:
- `utm_source`: channel (linkedin, google, partner, email, referral)
- `utm_medium`: type (cpc, organic, outbound, newsletter, affiliate)
- `utm_campaign`: “pilot-2026q1”, “queue-icp-b”, “corp-comms”
- `utm_content`: creative/version (hero-a, hero-b)
- `utm_term`: keyword (для search)

Правило:
- UTM сохраняем в cookie/localStorage на 30 дней и прокидываем в hidden fields формы.

---

## 2) События (event spec) — минимум

### 2.1 Лендинг events
- `page_view` (page)
- `cta_click` (cta_id: demo/pilot/security/pricing/checklist)
- `form_start` (form_id)
- `form_submit` (form_id)
- `calendar_booked` (если используете calendly)
- `doc_click` (doc_id: media-spec/android-tv/network/security-pack)
- `video_play` (video_id: demo-2min)

### 2.2 CRM events (через ручной ввод или интеграцию)
- `discovery_scheduled`
- `discovery_done`
- `demo_done`
- `pilot_agreed`
- `pilot_passed`
- `closed_won`
- `closed_lost`

---

## 3) Минимальные отчёты (еженедельно)

- Visits → CTA click rate
- CTA → form submit conversion
- Form submit → discovery scheduled conversion
- By segment (ICP-A/B) conversion
- By channel (utm_source/medium) conversion

---

## 3.1 Event properties (чтобы можно было анализировать)

Для лендинг-событий (минимум):
- `utm_source`, `utm_medium`, `utm_campaign`, `utm_content`, `utm_term`
- `page` (например `/`, `/corp-comms`, `/queue-kpi`, `/pilot`, `/security`, `/pricing`)
- `segment` (ICP-A/ICP-B/Unknown) — определить по странице или выбору use-case
- `persona` (Operator/IT/Security/Integrator/Other) — из формы

Для CRM (минимум):
- stage (pipeline stage)
- loss_reason (если lost)

---

## 3.2 Воронка (definitions, чтобы не спорить)

- `Inbound lead` = form_submit (demo или pilot)
- `Qualified lead (QL)` = стадия QL в CRM (см. `docs/37-crm-spec-and-automations.md`)
- `SQL` = discovery done + заполнен pilot passport
- `Pilot` = pilot agreed

---

## 3.3 Naming для A/B тестов (чтобы не было хаоса)

Соглашение:
- `utm_campaign = {icp}-{offer}-{yyyyq#}`  
  пример: `corp-pilot-2026q1`
- `utm_content = {page}-{variant}`  
  пример: `hero-a`, `hero-b`, `cta-demo`, `cta-pilot`

---

## 4) Privacy/consent (минимум)

- На форме: “By submitting you agree…” (если нужно под ваш юрисдикционный контур).
- Не собирать лишнее: достаточно work email, company, role, screens.
- Для outbound: обязательные `unsubscribe`/suppression и запрет автоматических рассылок на контакты без согласия/законного основания (см. `docs/45-lead-ingestion-llm-enrichment-and-consent-outreach.md`).
