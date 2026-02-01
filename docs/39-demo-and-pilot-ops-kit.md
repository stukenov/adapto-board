# 39 — Demo & Pilot Ops Kit (шаблоны, чтобы делать быстро)

Дата: 2026-02-01  
Роль: Sales Engineer / Delivery / CS  
Цель: иметь повторяемые “как провести демо и пилот” артефакты без перепридумывания.

---

## 1) Demo (15 минут): сценарий и тайминг

Цель демо: показать 3 ценности за 15 минут.

1) Publish за минуты (5 мин)
- загрузить asset (или взять готовый)
- изменить расписание → publish
- показать на Android TV, что версия применена

2) Overlay data-layer (5 мин)
- открыть manual overlay → поменять KPI/строку
- показать, что обновилось на экране быстро

3) Эксплуатация (5 мин)
- список устройств: online/offline, last seen, версия
- audit по изменению расписания
- as-run/snapshot “что играет”

Чек‑лист подготовки демо: уже есть в `docs/20-collateral-templates.md`.

---

## 2) Demo environment (что нужно иметь заранее)

Состав:
- demo tenant
- 1 demo channel + опубликованная scheduleVersion
- 2–3 assets (короткое видео + картинка)
- 1 Android TV устройство (или эмулятор, если используете)
- overlay profile + manual binding

---

## 3) “Pilot kickoff” (agenda 45 минут)

Участники: Champion + Operator owner + IT owner + (Integrator если есть).

Agenda:
1) Scope (screens/locations, что входит/не входит)
2) KPI thresholds (publish p95, uptime, online, overlay latency/adoption)
3) Tech prerequisites (MDM/allowlist/models/content spec)
4) План по неделям
5) Канал поддержки и эскалации

---

## 4) Weekly pilot status template (1 страница)

Разделы:
- Summary (зелёный/жёлтый/красный)
- KPI текущие:
  - publish p50/p95
  - online rate
  - uptime proxy
  - overlay latency/adoption
- Incidents (список: impact, root cause, MTTR)
- Blockers (что мешает production)
- Next week plan (3 пункта)

---

## 5) Go/No-Go meeting (30 минут)

Inputs:
- Pilot scorecard: `docs/17-pilot-scorecard-and-acceptance.md`
- Итоговый отчёт (metrics + incidents + blockers)

Decision options:
- Go production (R1 plan)
- Extend pilot (с чёткими изменениями и сроком)
- Stop (и фиксируем win/loss причины)

---

## 6) Production proposal outline (что отправлять после успешного пилота)

Структура:
1) Результаты пилота (цифры)
2) Scope production (SSO, storage/CDN, exports, retention)
3) Rollout plan (канарейка, группы, окна)
4) Pricing & limits
5) Support/SLA

Связанные документы:
- `docs/12-pricing-and-packaging.md`
- `docs/27-pricing-architecture-and-discounting.md`
- `docs/30-customer-success-and-support.md`

