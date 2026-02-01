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
- `cta_click` (cta_id: demo/pilot/security/pricing)
- `form_start` (form_id)
- `form_submit` (form_id)
- `calendar_booked` (если используете calendly)

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

## 4) Privacy/consent (минимум)

- На форме: “By submitting you agree…” (если нужно под ваш юрисдикционный контур).
- Не собирать лишнее: достаточно work email, company, role, screens.

