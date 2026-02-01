# 26 — GTM operating system (воронка, процесс, метрики)

Дата: 2026-02-01  
Роль: CEO / Head of Sales / Head of Marketing  
Цель: превратить продажу “пилотов” в управляемый процесс с прогнозируемым выходом в production.

## 1) Воронка (стадии и определения)

Минимальная “рабочая” воронка для B2B:

1) **Lead** — контакт + компания идентифицированы.
2) **Qualified lead (QL)** — соответствует базовым критериям ICP (экраны, Android TV, боль).
3) **Discovery done (SQL)** — заполнен pilot passport и согласованы KPI пилота.
4) **Demo validated** — демо показало 3 ценности (publish, overlay, эксплуатация), подтверждён ключевой use-case.
5) **Pilot agreed** — подписан пилотный SOW, назначены ответственные, даты, объём.
6) **Pilot in progress** — еженедельные статусы + фиксация метрик.
7) **Pilot passed** — go/no-go пройден, есть отчёт.
8) **Production contract** — подписан контракт/заказ/подписка.
9) **Expansion** — рост экранов/аддоны.
10) **Renewal** — продление.

## 2) ICP scoring (быстро “продаём/не продаём”)

Скоринг (0–2 балла на пункт; порог = 8/12):
- `Screens now` (≥50) / `Screens 12m` (рост)
- Android TV уже есть (или готовы купить)
- Боль: эксплуатация (инциденты) или data-layer (очереди/KPI)
- Есть IT owner (готовы отвечать за сеть/MDM/доступы)
- Сеть: не “запрещено всё” (или готовы предоставить исключения)
- Готовность к платному пилоту

## 3) Обязательные артефакты на каждом этапе

- QL: 6 вопросов квалификации (см. `docs/19-sales-playbook.md`).
- SQL: заполнен `docs/17-pilot-scorecard-and-acceptance.md` (пороговые метрики + объём).
- Pilot agreed: SOW + RACI + сетевой чек-лист.
- Pilot passed: итоговый отчёт (метрики + инциденты + план production).

## 4) Метрики GTM (что считать каждую неделю)

P0:
- `#Qualified leads / week`
- `SQL conversion` (QL → SQL)
- `Pilot close rate` (SQL → Pilot agreed)
- `Pilot pass rate` (Pilot agreed → Pilot passed)
- `Pilot → Production conversion`
- `Median sales cycle` (QL → Pilot agreed; Pilot agreed → Production)

P1:
- `ACV/MRR` по сегментам
- `Gross margin` драйверы (egress/support)
- `Channel mix` (outbound/partners/inbound)

## 5) Cadence (ритм управления)

Еженедельно:
- Pipeline review (45–60 мин): стадия, next step, блокеры, дата пилота.
- Win/Loss notes по закрытым сделкам.

Ежемесячно:
- Review marketing experiments (контент, outbound, партнёры).
- Пересмотр ICP приоритетов (по win-rate и скорости цикла).

Ежеквартально:
- QBR с production клиентами (совместно Sales + CS).
- Пересмотр прайсинга и лимитов по фактическому `U` и support load.

## 6) Каналы привлечения (минимум, который работает в B2B)

P0:
- Outbound в ICP-A (HR/Comms + IT owner).
- Партнёры-интеграторы для ICP-B (очереди/табло).

P1:
- Контент “под возражения” (эксплуатация, сети, стоимость трафика).
- Рефералы от интеграторов/MDM партнёров.

## 7) CRM: минимальные поля, без которых нельзя прогнозировать

Account:
- Segment (ICP-A/B), vertical, geo
- Screens now / target
- Android TV ownership (client/partner/unknown)
- Procurement complexity (low/med/high)

Opportunity:
- Stage (из раздела 1)
- Primary pain (publish vs ops vs data-layer)
- Pilot scope (screens, locations, data source)
- KPI thresholds (publish p95, uptime, online rate, overlay latency/adoption)
- Next meeting date + owner
- Forecast category (commit/best case/pipeline)

## 8) Win/Loss процесс (обязательный, иначе вы не учитесь)

Формат:
- 15–20 минут интервью с покупателем или champion.
- 5 вопросов: почему купили/не купили, какой был блокер, с кем сравнивали, что было решающим, что улучшить.

Результат:
- 1 страница win/loss summary в общий лог (с тегами: сегмент, конкуренты, блокеры, price sensitivity).

