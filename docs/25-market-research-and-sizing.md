# 25 — Market research & sizing (для CEO/Founders/GTM)

Дата: 2026-02-01  
Роль: CEO / Founder / Head of GTM  
Цель: зафиксировать “где деньги”, кого атакуем первыми, с кем сравнивают, и какие требования рынка мы обязаны закрыть.

## 1) Executive summary (1 страница)

### 1.1 Категория и wedge

Мы в категории **digital signage / corporate communications** с фокусом на **Android TV endpoints** и “**data-layer поверх контента**” (overlay), где ценность создаётся не шаблонами, а:
- устойчивостью 24/7 (offline-first),
- скоростью публикации,
- эксплуатацией (fleet + audit/as-run),
- подключением данных (manual → REST pull → webhook).

Клин (wedge) для захода:
- ICP-A: “корпоративные офисы/филиалы” (коммуникации + IT)
- ICP-B: “очереди/табло” (операции + IT + интегратор источника данных)

### 1.2 Почему это покупают
Покупают не “плеер” и не “шаблоны”. Покупают:
- снижение ручного труда и выездов,
- управляемость и контроль изменений (audit/as-run),
- “данные на экране” без рендера уникальных видео.

### 1.3 Как продаётся (реальность рынка)
- В большинстве компаний это продаётся через **пилот/POC** и проходит через **procurement/security**.
- В многих сделках победитель — тот, кто **снимет риски эксплуатации** и даст **предсказуемый rollout**, а не тот, у кого больше шаблонов.

## 2) Размер рынка: как считать (без самообмана)

Важно: цифры “global digital signage market size” полезны для общей картины, но для ранней стадии важнее **bottom-up**:
- сколько потенциальных аккаунтов в выбранной географии/вертикали,
- сколько экранов у типового аккаунта,
- реалистичная цена за экран/мес,
- конверсия пилот → production,
- скорость установки (time-to-first-value).

### 2.0 “Top-down” ориентиры (для контекста, не для планов)

Источники расходятся по методологии и диапазонам, но дают масштаб категории:
- Grand View Research оценивает рынок **USD 28.83B (2024)** и прогнозирует **USD 45.94B (2030)** при **CAGR 8.1% (2025–2030)**.
- Fortune Business Insights (старый горизонт) пишет **USD 19.78B (2018)** → **USD 35.94B (2026)** при **CAGR 7.8% (2019–2026)**.

### 2.1 Bottom-up формула (рекомендуемая)

Пусть:
- `A` = число target accounts (реально достижимых в 12–18 мес)
- `S` = среднее число экранов у аккаунта в production
- `P` = цена за экран/мес
- `C` = конверсия “pilot → production”

Тогда “реалистичный revenue-потолок на горизонте”:
- `MRR ≈ A * S * P * C`

Отдельно учитывать:
- `PilotRevenue` (разовые деньги, часто основной early cashflow)
- `ServicesRevenue` (внедрение/интеграции)

### 2.2 Что нужно собрать в первые 2–3 пилота (обязательно)
- Реальный `U` (GB/экран/мес) и профиль обновлений контента.
- Реальный support load: инциденты/100 экранов/неделя и средний MTTR.
- Реальный цикл изменения контента: кто публикует, сколько шагов, как часто.

## 3) Сегментация и приоритет (где мы выигрываем)

### 3.1 ICP-A: корпоративные коммуникации (50–1000 экранов)
Сильные причины купить:
- сеть/эксплуатация сложнее “идеального SaaS” (филиалы, прокси, разные сети),
- важен контроль доступа и доказуемость изменений,
- контент обновляется регулярно, но не “каждые 5 минут”.

Критичные требования (часто блокеры сделки):
- SSO/OIDC (в production),
- понятная модель безопасности,
- контроль трафика/лимитов,
- поддерживаемый список Android TV устройств.

### 3.2 ICP-B: очередь/табло (20–500 экранов)
Сильные причины купить:
- “данные на экране” ценнее, чем “красивые шаблоны”,
- есть интегратор источника данных (CRM/queue/ERP), который может стать каналом продаж.

Критичные требования:
- понятный формат данных и SLA по обновлению,
- простая интеграция (REST pull/webhook),
- устойчивость к сети/обрывам.

### 3.3 Сегменты, где лучше не продавать на ранней стадии
- Ритейл, ожидающий “ad-tech/кампании/A-B/склад креативов” как обязательный стандарт.
- Клиенты, которые в R0 требуют **2–3 платформы** endpoints (Tizen/webOS/Windows) и не готовы принять Android TV-only.

## 4) Конкурентная среда: с кем реально сравнивают

Покупатели обычно сравнивают не конкретные бренды, а категории:
1) **Signage SaaS** (быстро, много шаблонов, но часто слабее enterprise-эксплуатация под “реальные сети”).
2) **DIY/самописка/USB** (дёшево, но нет контроля, нет статусов, нет audit/as-run).
3) **Enterprise AV** (дорого и долго, но закрывает procurement и крупные внедрения).

## 5) Pricing benchmarks: что считается “нормой” на рынке

Ниже — ориентиры per-screen pricing по публичным прайсам (проверять раз в квартал).

| Vendor | Позиционирование | Публичный pricing (USD) | Комментарий |
|---|---|---|---|
| ScreenCloud | signage SaaS | Core от **$20/screen/mo**, Pro от **$30/screen/mo**, Enterprise — quote (min 25 screens) | сильны в UX/шаблонах, enterprise: SSO/поддержка |
| Yodeck | signage SaaS | Basic **$8**, Premium **$11**, Enterprise **$15** / screen / month (1 экран free) | сильны в “быстро начать”; enterprise: SSO, audit logs |
| Rise Vision | signage SaaS | Basic **$11**, Advanced **$13** / display / month (есть enterprise варианты per school/year) | часто education/SMB; есть SSO (SAML) в enterprise |
| OptiSigns | signage SaaS | Standard **$9** (annual), Pro **$11.25** (annual), Pro Plus **$13.50** (annual) / screen / month | aggressive pricing; SSO/SAML в higher tiers |
| TelemetryTV | signage SaaS/enterprise | Entry **$8**, Core **$13**, Elite **$16** / device / month (annual term) | упор на enterprise rollout и лимиты storage/bandwidth |
| NoviSign | signage SaaS | Business **$18**, Business Plus **$26**, Premium **$44** / screen / month (annual) | mid-market/enterprise; Premium включает SSO, audit logs |

Практический вывод по рынку: публичный per-screen pricing “в коридоре” **~$8–$44/screen/month**, а enterprise почти всегда добавляет **SSO, audit logs, approval workflows, SLA/support**.

## 6) Требования procurement/security: что будет спрашивать enterprise

Список вопросов, которые почти гарантированно появятся:
- Где хостится (облако/он-прем), есть ли “изолированный контур”.
- SSO (OIDC/SAML), RBAC, журнал аудита.
- Шифрование in-transit/at-rest, управление ключами/секретами.
- Уязвимости: SAST/DAST, patch cadence, зависимости.
- Инциденты: уведомление, RTO/RPO, бэкапы, DR.
- Логи: что пишем, ретеншн, экспорт.
- Данные: какие персональные данные собираем, кто имеет доступ.

Документ-пакет и ответник: `docs/29-procurement-security-compliance-pack.md`.

## 7) Research backlog (что изучить дальше)

P0:
- 10 win/loss интервью (почему купили/не купили; какие блокеры).
- Android TV device landscape по выбранной географии (модели, MDM, прошивки).
- 5–10 интеграторов (AV/system integrators): что для них важно, как они продают.

P1:
- Глубокая сегментация verticals (банки/клиники/производства/склады).
- Бенчмарки “операционная стоимость signage” (выезды, downtime, ручные обновления).

## 8) Sources (проверено 2026-02-01)

```
Market size / growth:
- Grand View Research — Digital Signage Market Size (report page):
  https://www.grandviewresearch.com/industry-analysis/digital-signage-market-report
- Grand View Research — press release / summary:
  https://www.grandviewresearch.com/press-release/global-digital-signage-market
- Fortune Business Insights — Digital Signage Market (report page):
  https://www.fortunebusinessinsights.com/digital-signage-market-105004
- Fortune Business Insights — industry report page (market size summary):
  https://www.fortunebusinessinsights.com/industry-reports/digital-signage-market-101898

Competitor pricing (public pages):
- ScreenCloud pricing: https://screencloud.com/pricing
- Yodeck pricing: https://www.yodeck.com/pricing/
- Rise Vision pricing: https://www.risevision.com/pricing
- OptiSigns pricing: https://www.optisigns.com/pricing
- TelemetryTV pricing: https://www.telemetrytv.com/digital-signage-software-pricing
- NoviSign pricing: https://www.novisign.com/pricing/

Android device management / kiosk (context):
- Android Enterprise: https://www.android.com/enterprise/
- Android Device Policy & management APIs: https://developers.google.com/android/management
- Google Play (work) / managed Play: https://support.google.com/work/android/
- Android dedicated devices (incl. kiosk/single-use) overview:
  https://www.android.com/enterprise/solutions/dedicated-devices/
```
