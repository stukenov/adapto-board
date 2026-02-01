# 43 — Lean marketing playbook (founder-led, без бюджета)

Дата: 2026-02-01  
Роль: предприниматель‑маркетолог / founder‑led GTM  
Цель: описать маркетинг так, чтобы его можно было делать **быстро, дёшево и повторяемо** под Playout Edge: лид → discovery → пилот → production.

---

## 1) Принципы (чтобы не слить время)

1) **Не продаём “platform”. Продаём измеримый пилот.**  
2) **Один оффер → две ICP‑страницы.** Не мешать ICP‑A и ICP‑B в одном тексте.  
3) **Одна “доказательная” демонстрация ценности** (publish + overlay + fleet).  
4) **Trust assets важнее “брендинга”.** Сеть/устройства/контент — главный источник фейлов, снимаем их заранее документами.  
5) **Маркетинг = distribution + repetitions.** Один материал → 10 касаний.

Связанные документы:
- messaging и офферы: `docs/18-marketing-kit.md`
- landing: `docs/36-landing-mvp-spec.md`
- CRM: `docs/37-crm-spec-and-automations.md`
- tracking: `docs/38-marketing-ops-tracking-and-analytics.md`
- публичные prerequisites: `docs/40-public-supported-media-spec.md`, `docs/41-public-android-tv-device-requirements.md`, `docs/42-public-network-requirements.md`

---

## 2) Offer ladder (что “продаём” на каждом шаге)

1) **Lead magnet (free)**: “Pilot readiness checklist” (сеть + Android TV + медиа).  
   - ссылка на public docs (`docs/40-…`, `docs/41-…`, `docs/42-…`)
2) **Discovery (20–30 мин)**: заполнить pilot passport (KPI + prerequisites).  
   - `docs/17-pilot-scorecard-and-acceptance.md`
3) **Demo (15 мин)**: publish → экран + overlay update + fleet + audit/as‑run.  
   - `docs/39-demo-and-pilot-ops-kit.md`
4) **Paid pilot (2–4 недели)**: 50–200 экранов, KPI go/no-go, 1 data source + manual.  
5) **Production**: SSO/exports/retention/isolated/on‑prem по необходимости.

---

## 3) Минимальный “trust pack” (сделать за 1 день)

P0:
- 2‑мин видео‑демо (запись экрана админки + камера на ТВ).
- 3 скриншота “правды”:
  - devices dashboard
  - publish tracker
  - manual overlay editor
- Security overview page (коротко) + “download pack” (`docs/29-procurement-security-compliance-pack.md`).
- Public prerequisites docs:
  - media spec (`docs/40-public-supported-media-spec.md`)
  - Android TV checklist (`docs/41-public-android-tv-device-requirements.md`)
  - network requirements (`docs/42-public-network-requirements.md`)

Почему это дешево и эффективно:
- снижает количество “неподходящих” лидов,
- ускоряет IT review,
- повышает конверсию demo → pilot.

---

## 4) Каналы, которые реально работают без бюджета

### 4.1 Founder-led outbound (ICP‑A)

Цель: 10–20 качественных касаний в день.

Рутина (60–90 минут/день):
1) 10 новых контактов/день (champion + IT owner).
2) 10 follow‑ups/день.
3) 1–2 демо/неделю.

Шаблоны:
- email/DM sequences: `docs/20-collateral-templates.md`

### 4.2 Партнёры‑интеграторы (ICP‑B)

Рутина (2 часа/неделю):
1) Составить список 20 интеграторов.
2) 5 outreach сообщений в неделю.
3) 1 партнёрский call в неделю.
4) Отдать enablement kit (one‑pager + demo checklist + pilot SOW).

Док: `docs/31-partner-program.md`

### 4.3 Контент “под возражения” (вместо “новостей”)

Пишем только то, что снимает фейлы сделок:
- “почему экраны тухнут” (ops)
- “почему SSE проходит через прокси” (network)
- “как считать трафик (U) и не убить маржу” (unit economics)
- “как провести пилот без героизма” (checklists)

---

## 5) Контент‑движок: 1 материал → 10 касаний

Формат “1 большой материал/неделя”:
- 1 статья (1000–1500 слов)
- 1 пост LinkedIn (коротко)
- 1 “тред” (5–7 пунктов)
- 1 чек‑лист (в PDF/Google Doc)
- 1 короткое видео (2–3 минуты)

Distribution checklist (каждый материал):
- отправить 5–10 тёплым контактам (“могу поделиться — актуально?”)
- 10 outbound follow‑ups с ссылкой на материал
- 1 партнёру как enablement (если релевантно)

---

## 6) 4-недельный план запуска (нулевой бюджет)

### Week 1 — упаковка и trust
- Запустить 4 страницы: `/`, `/corp-comms`, `/queue-kpi`, `/security` (см. `docs/36-landing-mvp-spec.md`).
- Добавить public docs ссылки (media/device/network).
- Записать 2‑мин видео‑демо.
- Настроить CRM pipeline + авто‑письма (см. `docs/37-crm-spec-and-automations.md`).

### Week 2 — outbound ритм
- 50 ICP‑A контактов (champion + IT) + 50 ICP‑B (ops + integrator).
- Запустить sequence (3 касания) из `docs/20-collateral-templates.md`.
- Цель: 2 discovery + 1 demo.

### Week 3 — партнёры
- 10 интеграторов: 5 outreach + 2 calls + 1 “pilot co-sell” договорённость.
- Отдать enablement kit.

### Week 4 — первый кейс
- Дожать 1 пилот до “pilot report”.
- Оформить 1‑страничный кейс‑стади (шаблон в `docs/20-collateral-templates.md`).
- Добавить на сайт (social proof).

---

## 7) Experiment backlog (что тестировать без бюджета)

Landing:
- ICP‑A hero A/B (publish vs ops) и CTA (demo vs pilot).
- ICP‑B hero A/B (queue vs KPI).

Offer:
- “Pilot readiness checklist” как lead magnet (gated vs ungated).
- “Security pack download” как mid‑funnel CTA.

Outbound:
- subject lines (“publish за минуты” vs “audit/as‑run”).
- 2‑строчный email vs 6‑строчный.

Партнёры:
- referral fee vs delivery‑partner.

---

## 8) KPI (реалистичные для ранней стадии)

Неделя:
- 50–100 outbound touches
- 2–4 discovery calls
- 1–2 demos

Месяц:
- 2–4 pilots proposed
- 1–2 pilots agreed

Главный KPI:
- conversion **pilot → production** и время до first value.

