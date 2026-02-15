# 46 — Automation Blueprint: как запустить продажи и маркетинг практически без людей

Дата: 2026-02-15
Цель: для каждого блока стратегии (docs/strategy/) и плана изменений (docs/CHANGELOG.md) описать, как автоматизировать процесс так, чтобы человек участвовал минимально — только в точках принятия решений.

Связанные документы:
- `docs/CHANGELOG.md` (план изменений в коде)
- `docs/strategy/Playout_Edge_Strategy.docx` (стратегия продаж 2026)
- `docs/45-lead-ingestion-llm-enrichment-and-consent-outreach.md` (LeadOps pipeline)
- `docs/37-crm-spec-and-automations.md` (CRM)
- `docs/38-marketing-ops-tracking-and-analytics.md` (marketing ops)

---

## Принцип: "1 человек + автоматизация = отдел из 10"

Целевая модель: **1 founder/CEO** делает стратегические решения + **n8n/Make автоматизация** выполняет всё остальное. Человек нужен только для:
- Approve/reject на ключевых точках (отправка КП, закрытие сделки)
- Проведение живых демо (20 мин, но даже это можно частично автоматизировать)
- Финальные переговоры с ЛПР

Всё остальное — автоматизация.

---

## АРХИТЕКТУРА АВТОМАТИЗАЦИИ

```
┌─────────────────────────────────────────────────────────┐
│                    n8n (self-hosted)                      │
│              Центральный оркестратор                      │
├─────────────┬──────────────┬────────────┬───────────────┤
│ Lead Gen    │ Outreach     │ Content    │ Analytics     │
│             │              │            │               │
│ Apollo.io   │ Saleshandy   │ Claude API │ GA4           │
│ LinkedIn    │ Email seqs   │ Buffer     │ Yandex.Metrica│
│ Website     │ Telegram bot │ YouTube    │ Power BI      │
│ forms       │              │ LinkedIn   │               │
├─────────────┴──────────────┴────────────┴───────────────┤
│                    HubSpot CRM (free)                    │
│              Единый источник правды                       │
├─────────────────────────────────────────────────────────┤
│              Playout Edge Backend                         │
│     demo_requests table + webhook events + email          │
└─────────────────────────────────────────────────────────┘
```

**Стек автоматизации (бюджет ~$300-500/мес):**

| Инструмент | Назначение | Цена/мес |
|-----------|-----------|----------|
| n8n (self-hosted) | Оркестрация всех workflow | $0 (Docker на том же VPS) |
| HubSpot CRM (free) | Pipeline, контакты, сделки | $0 |
| Apollo.io | Lead enrichment + prospecting | $49 |
| Saleshandy | Email sequences | $25 |
| Buffer | Планирование соцсетей | $5 |
| SE Ranking | SEO мониторинг | $45 |
| Tidio | Чат-бот на сайте | $15 |
| Claude API | Генерация контента, enrichment, scoring | ~$50 |
| Mailgun | Транзакционные email | ~$15 |
| **Итого** | | **~$204/мес** |

---

## БЛОК 1: ЛИДОГЕНЕРАЦИЯ (было: 1 SDR fulltime → стало: 0 людей)

### Что автоматизируем

Весь процесс от "нет лидов" до "квалифицированный лид в CRM".

### 1.1 Входящие лиды (Inbound) — полностью автоматически

**Триггер:** Пользователь заполняет форму на tv.adapto.kz (/demo, /signup, /contact)

**Автоматический pipeline (n8n workflow):**

```
Форма на сайте → webhook в n8n →
  1. Сохранить в demo_requests (Playout Edge DB)
  2. Enrichment через Apollo.io API:
     - Размер компании
     - Индустрия
     - LinkedIn профиль
     - Телефон (если не указан)
  3. LLM scoring через Claude API:
     - Prompt: "Оцени лид для B2B playout/digital signage платформы в КЗ..."
     - Output: {score: 0-100, segment: "tv|bank|retail|gov", persona: "buyer|champion|it_owner", reason: "..."}
  4. Создать контакт + сделку в HubSpot
  5. Если score >= 70:
     → Отправить auto-reply с calendly-ссылкой на демо
     → Уведомить в Telegram бот: "Горячий лид: {company}, {score}"
  6. Если score 40-69:
     → Добавить в email nurturing sequence (Saleshandy)
  7. Если score < 40:
     → Тег "low priority" в CRM, без outreach
```

**Человек:** Не участвует. Получает Telegram-нотификацию о горячих лидах.

### 1.2 Исходящие лиды (Outbound) — 90% автоматически

**Еженедельный workflow (n8n, cron каждый понедельник):**

```
1. Apollo.io API → search companies:
   - Фильтр: Казахстан, 50+ сотрудников, industries: broadcasting, banking, retail
   - Лимит: 50 компаний/неделю
2. Для каждой компании:
   a. Найти контакты (CTO, IT Director, CEO) через Apollo
   b. LLM enrichment (Claude):
      - Проанализировать сайт компании
      - Определить потенциальную боль
      - Сгенерировать персонализированное intro
   c. Создать контакт в HubSpot (status: "research")
   d. Добавить в cold email sequence (Saleshandy):
      - День 1: Персонализированное письмо (LLM-generated)
      - День 3: Follow-up с кейсом из того же сегмента
      - День 7: "Видели ли вы наше демо-видео?"
      - День 14: Breakup email
3. Мониторинг ответов:
   - Positive reply → создать сделку в HubSpot, уведомить в Telegram
   - Negative/unsubscribe → suppress в CRM
   - No reply → архив после sequence
```

**Человек:** Просматривает список из 50 компаний раз в неделю (10 мин), может убрать нерелевантные. Всё остальное автоматически.

### 1.3 LinkedIn outreach — 80% автоматически

**Инструмент:** Dux-Soup или LinkedIn Sales Navigator + n8n

```
1. n8n генерирует список целевых профилей из Apollo
2. LinkedIn automation:
   - Визит профиля (view)
   - Через 1 день: Connection request с персонализированной заметкой (LLM)
   - После accept: Сообщение 1 (ценность, не продажа)
   - Через 3 дня: Сообщение 2 (кейс + CTA на демо)
3. При ответе → уведомление в Telegram, ручное продолжение
```

**Человек:** Отвечает на входящие LinkedIn сообщения (5-10 мин/день).

---

## БЛОК 2: КВАЛИФИКАЦИЯ И СКОРИНГ (было: sales manager → стало: LLM)

### 2.1 Автоматический BANT-KZ скоринг

**Trigger:** Новый контакт в CRM или ответ на outreach.

**n8n workflow:**

```
1. Собрать данные:
   - Из Apollo: размер компании, revenue, tech stack
   - Из формы: должность, комментарий
   - Из email: тон ответа, вопросы
2. Claude API scoring:
   Prompt: "Квалифицируй лид по BANT-KZ:
   - Budget: есть ли IT-бюджет? (0-25)
   - Authority: ЛПР или нет? (0-25)
   - Need: есть ли явная боль? (0-25)
   - Timeline: срочность? (0-25)
   Контекст: {company_data}, {form_data}, {email_thread}
   Return JSON: {score, budget, authority, need, timeline, next_action}"
3. Обновить HubSpot:
   - Deal stage по score:
     - 80+: "Demo Scheduled"
     - 60-79: "Qualified"
     - 40-59: "Nurturing"
     - <40: "Disqualified"
   - Next action в заметку
4. Уведомление если score >= 80
```

**Человек:** Не участвует в скоринге. Только проводит демо для score >= 80.

---

## БЛОК 3: ДЕМО И ПИЛОТЫ (было: sales + engineer → стало: 50% автоматически)

### 3.1 Запись на демо — полностью автоматически

```
Горячий лид → auto-email с Calendly ссылкой →
  лид выбирает слот →
  Calendly webhook → n8n →
  1. Создать событие в Google Calendar
  2. Отправить reminder за 24ч и за 1ч
  3. Обновить HubSpot deal stage → "Demo Scheduled"
  4. Подготовить demo environment:
     - Создать тестовый tenant через MCP API
     - Загрузить demo-контент (3 видео, 2 оверлея)
     - Сгенерировать demo credentials
  5. Отправить клиенту pre-demo brief:
     - "Что мы покажем на демо"
     - Credentials для самостоятельного изучения
```

### 3.2 Self-service демо — полностью автоматически

**Новая идея: интерактивное демо без человека.**

```
Страница /demo-sandbox на сайте:
  1. Клиент вводит email + компанию
  2. Backend автоматически:
     - Создаёт изолированный tenant (TTL: 7 дней)
     - Загружает demo data
     - Генерирует credentials
  3. Отправляет email с логином + guided tour (пошаговые подсказки в UI)
  4. Трекинг активности: что смотрел, сколько времени, какие фичи
  5. Через 24ч: auto-email "Как вам платформа? Запишитесь на обсуждение"
  6. Через 7 дней: sandbox expires, final follow-up
```

**Изменения в коде:**
- Новый endpoint: `POST /api/public/demo-sandbox` → создаёт tenant + user + demo data
- TTL job: удаление demo tenants старше 7 дней
- Activity tracking: логирование действий demo-пользователя

**Человек:** Проводит живое демо только для Enterprise лидов (score >= 80). Остальные используют self-service.

### 3.3 Пилот — 70% автоматически

```
Пилот запущен → n8n weekly cron:
  1. Собрать метрики из Playout Edge API (MCP tools):
     - Устройства online/offline
     - Кол-во воспроизведений
     - Uptime %
     - Активные оверлеи
  2. Claude API → сгенерировать weekly report:
     "На основе метрик: {data}, напиши отчёт пилота для клиента {company}.
      Включи: достижения недели, потенциальные проблемы, рекомендации."
  3. Отправить email клиенту (HTML-отчёт)
  4. Обновить HubSpot (deal custom fields: pilot_week, devices_online, uptime)
  5. Через 4 недели: auto-generate КП (Claude + template) → отправить на approve
```

**Человек:** Просматривает weekly report (2 мин), отвечает на вопросы клиента, проводит kick-off и go/no-go.

---

## БЛОК 4: КОНТЕНТ-МАРКЕТИНГ (было: маркетолог → стало: 0 людей)

### 4.1 Блог — полностью автоматически

**n8n workflow (cron: 1 и 15 число каждого месяца):**

```
1. Выбрать тему из контент-плана (таблица в Airtable/Google Sheets)
2. SEO research через SE Ranking API:
   - Keywords с объёмом поиска
   - Конкуренты на первой странице
   - Related questions
3. Claude API → сгенерировать статью:
   Prompt: "Напиши экспертную статью на русском для блога Playout Edge.
   Тема: {topic}
   Keywords: {keywords}
   Структура: intro, 3-5 секций, заключение с CTA
   Tone: профессиональный но доступный
   Длина: 1500-2000 слов
   Включи: конкретные примеры из broadcasting/digital signage в КЗ"
4. Сохранить как .md файл в resources/blog/
5. Git commit + deploy (CI/CD)
6. Отправить ссылку в Telegram канал
7. Создать 3-5 социальных постов (Claude):
   - LinkedIn (длинный пост)
   - Telegram (короткий пост + ссылка)
8. Запланировать в Buffer
```

**Человек:** Раз в квартал утверждает контент-план (15 мин). Статьи публикуются автоматически.

### 4.2 Социальные сети — полностью автоматически

**n8n workflow (cron: ежедневно 10:00 ALMT):**

```
1. Проверить очередь постов в Google Sheets
2. Если есть готовый пост → отправить через Buffer API:
   - LinkedIn (вт, чт)
   - Telegram (@playoutedge) через Bot API (пн, ср, пт)
3. Если очередь пуста → Claude API → сгенерировать:
   - Типы: industry insight, product tip, case study teaser, engagement question
   - Автовыбор типа по ротации
4. Мониторинг engagement (Buffer API) →
   если пост набрал > X лайков → отправить boost в Google Ads
```

### 4.3 YouTube — 80% автоматически

```
1. Запись скринкастов: OBS + скрипт (Claude generates script)
2. Монтаж: ffmpeg автоматический нарезка (intro + body + outro template)
3. Upload: YouTube API через n8n
4. Генерация:
   - Title + description (Claude, SEO-оптимизированные)
   - Thumbnail (Canva API или template)
   - Subtitles (Whisper API → SRT файл)
5. Кросс-публикация в Telegram + LinkedIn
```

**Человек:** Записывает скринкаст (10-15 мин). Всё остальное автоматически.

### 4.4 Email nurturing sequences — полностью автоматически

**Saleshandy sequences (настроить 1 раз):**

```
Sequence A: "Новый лид из inbound" (score 40-69)
  День 0: Welcome + ссылка на demo-sandbox
  День 3: Кейс из сегмента лида (LLM выбирает)
  День 7: "5 причин автоматизировать плейаут" (ссылка на блог)
  День 14: "Бесплатный пилот — как это работает"
  День 21: ROI калькулятор
  День 30: "Последний шанс на бесплатный пилот в Q1"
  → При ответе/клике → upgrade score в HubSpot → уведомление

Sequence B: "Cold outbound"
  День 1: Персонализированное intro
  День 3: Follow-up + кейс
  День 7: Видео-демо ссылка
  День 14: Breakup

Sequence C: "Post-demo nurture" (если не конвертировался)
  День 1: Спасибо за демо + резюме
  День 7: Дополнительный кейс
  День 14: Новая фича/обновление
  День 30: "Актуально ли?"
```

---

## БЛОК 5: SEO (было: SEO-специалист → стало: 0 людей)

### 5.1 Техническое SEO — одноразовая настройка + мониторинг

**Встроить в код (CHANGELOG.md задачи 1.3):**
- meta-теги, OG, sitemap.xml, robots.txt, JSON-LD
- Автогенерация sitemap при добавлении блог-поста

**n8n weekly monitoring:**
```
1. SE Ranking API → отчёт позиций по 20 ключевым словам
2. Google Search Console API → impressions, clicks, CTR
3. Если позиция упала > 5 мест → alert в Telegram
4. Monthly: Claude → анализ + рекомендации по оптимизации
```

### 5.2 Контентное SEO — встроено в блог-автоматизацию

Каждая статья автоматически оптимизирована (keyword research → Claude prompt включает keywords).

---

## БЛОК 6: CRM И PIPELINE (было: sales manager → стало: автоматические rules)

### 6.1 HubSpot автоматизации (free tier supports basic workflows)

```
Правила:
1. Новый контакт → auto-assign owner (round robin если >1 человек)
2. Deal stage "Demo Scheduled" + дата прошла → move to "Demo Completed"
3. Deal stage "Pilot" + 30 дней без activity → alert "Pilot stalling"
4. Deal "Won" → trigger onboarding sequence
5. Deal "Lost" → reason survey email + add to re-engagement sequence (6 мес)
```

### 6.2 Reporting — полностью автоматически

**n8n workflow (cron: каждый пнд 9:00):**
```
1. HubSpot API → pipeline metrics:
   - Deals по стадиям
   - Win rate
   - Average deal size
   - Cycle time
2. Claude → weekly sales report на русском
3. Отправить в Telegram + email CEO
```

---

## БЛОК 7: CUSTOMER SUCCESS (было: CSM → стало: автоматический health score)

### 7.1 Health Score — автоматически из продукта

**n8n daily job:**
```
Для каждого платного клиента:
1. Playout Edge MCP API → собрать:
   - % устройств online (за 7 дней)
   - Кол-во обновлений расписания
   - Кол-во входов в admin UI
   - Кол-во новых ассетов
2. Health Score = weighted average:
   - Device uptime: 40%
   - Admin activity: 30%
   - Content freshness: 20%
   - Support tickets: 10%
3. Сохранить в HubSpot custom property
4. Если score < 50 → alert "At risk" → auto-email "Нужна помощь?"
5. Если score > 80 → auto-email "Рассмотрите расширение тарифа"
```

### 7.2 Onboarding — 90% автоматически

```
Deal "Won" → n8n trigger:
  День 0: Welcome email + credentials + getting started guide (PDF)
  День 1: Auto-provision tenant (MCP API):
    - Создать каналы по шаблону (из пилота)
    - Импортировать ассеты
    - Назначить устройства
  День 3: "Как прошла настройка?" email
  День 7: Auto-generate usage report
  День 14: "Нужна ли обучение для вашей команды?"
  День 30: First monthly QBR (auto-generated report + Calendly для звонка)
```

### 7.3 Billing — автоматически

**Встроить в Playout Edge:**
```
Новый endpoint: POST /api/admin/billing/subscribe
  - Интеграция с Stripe (международные платежи)
  - Или Kaspi/Halyk Bank API (для КЗ рынка)
  - Автогенерация инвойса (PDF)
  - Auto-charge monthly
  - Dunning emails (неудачный платёж → retry + уведомление)
```

**n8n workflow:**
```
1. Stripe webhook "payment_succeeded" → обновить HubSpot deal
2. Stripe webhook "payment_failed" → 3 retry + alert
3. 30 дней до renewal → auto-email "Ваша подписка продлевается"
4. Invoice → auto-generate PDF + отправить на email клиента
```

---

## БЛОК 8: ПОДДЕРЖКА (было: support agent → стало: AI chatbot + escalation)

### 8.1 Tidio AI chatbot на сайте

```
Настройка (одноразовая):
1. Knowledge base: загрузить docs/*.md + FAQ + feature descriptions
2. Flows:
   - "Как начать?" → ссылка на getting started + demo sandbox
   - "Проблема с устройством" → чек-лист troubleshooting → если не решено, создать ticket
   - "Хочу купить" → квалификация → Calendly ссылка
   - "Цены" → ссылка на /pricing + "Хотите обсудить Enterprise?"
3. Fallback: если бот не может → создать тикет в HubSpot → Telegram alert
```

### 8.2 Тикет-система — встроить в Playout Edge

```
Новая таблица: support_tickets
  - id, tenant_id, contact_email, subject, description, priority, status, created_at

Новый endpoint: POST /api/support/ticket (public, rate-limited)

n8n workflow:
  1. Новый тикет → classify (Claude): bug/question/feature_request/billing
  2. Bug → check known issues DB → if match, auto-reply with fix
  3. Question → search knowledge base → if confident answer, auto-reply
  4. Если auto-reply не сработал → Telegram alert для human response
```

**Человек:** Отвечает только на тикеты, которые бот не смог решить (~20% тикетов).

---

## БЛОК 9: АНАЛИТИКА И ОТЧЁТНОСТЬ (полностью автоматически)

### 9.1 Marketing dashboard

**n8n → Google Sheets (free BI):**
```
Weekly data collection:
1. GA4 API → трафик, источники, конверсии
2. Yandex.Metrica API → KZ трафик специфично
3. SE Ranking API → SEO позиции
4. HubSpot API → pipeline, leads, deals
5. Buffer API → social engagement
6. Saleshandy API → email open/reply rates
7. Playout Edge API → platform metrics

→ Всё в Google Sheets → auto-update charts
→ Claude → weekly narrative summary → email + Telegram
```

### 9.2 Board-level report (ежемесячный)

**n8n workflow (1 число каждого месяца):**
```
1. Собрать все метрики (см. выше)
2. Claude API → генерация monthly report:
   - ARR и MRR
   - Pipeline value
   - New leads / conversion rate
   - Customer health
   - Content performance
   - Бюджет: план vs факт
3. Сгенерировать PDF (через Playout Edge API или weasyprint)
4. Отправить на email CEO
```

---

## БЛОК 10: ПАРТНЁРСКАЯ ПРОГРАММА (80% автоматически)

```
Страница /partners на сайте:
  1. Форма регистрации партнёра
  2. Auto-onboarding:
     - Создать партнёрский аккаунт в HubSpot
     - Отправить Welcome Kit (PDF + demo access)
     - Реферальная ссылка с UTM
  3. Трекинг:
     - UTM → лид приходит → связать с партнёром
     - Лид конвертируется → рассчитать комиссию
     - Auto-invoice партнёру (quarterly)
  4. Партнёрский портал:
     - Dashboard с лидами и комиссиями
     - Download маркетинговых материалов
     - Deal registration form
```

---

## БЛОК 11: КОНФЕРЕНЦИИ И СОБЫТИЯ (60% автоматически)

**До события (2 недели):**
```
1. Claude → сгенерировать:
   - LinkedIn пост-анонс
   - Email blast по базе (Saleshandy)
   - Landing page для регистрации
2. Buffer → запланировать посты
3. n8n → отправить reminders
```

**Во время события:**
```
1. Сбор визиток → фото → Claude Vision API → extract contacts
2. Автоматический импорт в CRM (через docs/45 pipeline)
3. Real-time Telegram уведомления
```

**После события (автоматически):**
```
1. День 1: Thank you email всем контактам
2. День 3: Follow-up с материалами + demo sandbox ссылка
3. День 7: Квалификационный звонок (Calendly)
```

**Человек:** Только присутствует на стенде и общается. Всё до/после — автоматически.

---

## СВОДНАЯ ТАБЛИЦА: УРОВЕНЬ АВТОМАТИЗАЦИИ

| Процесс | До автоматизации | После | Человек делает |
|---------|-----------------|-------|----------------|
| Inbound лидогенерация | 1 SDR, 8ч/день | 100% авто | Ничего |
| Outbound prospecting | 1 SDR, 8ч/день | 90% авто | Approve списка (10 мин/нед) |
| LinkedIn outreach | 2ч/день | 80% авто | Ответы на сообщения (10 мин/день) |
| Lead scoring/квалификация | 1ч/лид | 100% авто | Ничего |
| Demo scheduling | 30 мин/лид | 100% авто | Ничего |
| Живое демо | 30 мин/лид | 50% авто (sandbox) | Демо для Enterprise (2-3/нед) |
| Пилот management | 4ч/нед/клиент | 70% авто | Kick-off + go/no-go (1ч/нед) |
| Email sequences | 2ч/день | 100% авто | Ничего |
| Контент (блог) | 8ч/статья | 95% авто | Approve контент-плана (15 мин/кв) |
| Соцсети | 2ч/день | 100% авто | Ничего |
| SEO | 4ч/нед | 95% авто | Ничего |
| CRM/pipeline | 2ч/день | 90% авто | Обновление deal notes (10 мин/день) |
| Reporting | 4ч/нед | 100% авто | Просмотр (5 мин/нед) |
| Customer health | 2ч/день | 90% авто | Реагирование на alerts (по необходимости) |
| Onboarding | 8ч/клиент | 90% авто | Welcome звонок (30 мин) |
| Billing | 2ч/мес | 100% авто | Ничего |
| Support | 4ч/день | 80% авто | Ответ на эскалации (~20% тикетов) |
| Партнёры | 4ч/нед | 80% авто | Onboarding звонок |
| Конференции | 40ч/событие | 60% авто | Присутствие на стенде |

**Итого времени основателя:** ~10-15 часов/неделю на продажи+маркетинг (вместо 3 fulltime сотрудников).

---

## ПЛАН ВНЕДРЕНИЯ АВТОМАТИЗАЦИИ

### Фаза 1 (недели 1-2): Базовая инфраструктура
- [ ] Установить n8n на VPS (Docker, рядом с Playout Edge)
- [ ] Создать HubSpot аккаунт (free), настроить pipeline stages
- [ ] Зарегистрировать Apollo.io ($49/мес)
- [ ] Зарегистрировать Saleshandy ($25/мес)
- [ ] Получить Claude API ключ, настроить бюджет
- [ ] Настроить Mailgun (уже в CHANGELOG)

### Фаза 2 (недели 3-4): Inbound pipeline
- [ ] Реализовать demo_requests webhook → n8n
- [ ] n8n workflow: form → enrich → score → CRM → auto-reply
- [ ] Email sequence в Saleshandy: "Inbound nurture"
- [ ] Telegram bot для alerts

### Фаза 3 (недели 5-6): Outbound + Content
- [ ] n8n workflow: weekly prospecting (Apollo → enrich → sequence)
- [ ] n8n workflow: blog auto-generation (Claude → markdown → deploy)
- [ ] Buffer setup + social auto-posting
- [ ] SE Ranking setup + weekly monitoring

### Фаза 4 (недели 7-8): Demo + Support
- [ ] Self-service demo sandbox (код: новый endpoint + TTL job)
- [ ] Tidio chatbot на сайте
- [ ] Support ticket system (код: таблица + endpoint + auto-classify)

### Фаза 5 (месяц 3): Customer Success + Billing
- [ ] Health score automation (n8n + MCP API)
- [ ] Onboarding sequence
- [ ] Stripe/Kaspi интеграция
- [ ] Partner portal (страница + UTM tracking)

---

## ИЗМЕНЕНИЯ В КОДЕ PLAYOUT EDGE

Помимо задач из CHANGELOG.md, для автоматизации нужно добавить:

### Новые endpoints

```
POST /api/public/demo-sandbox        → создать demo tenant (TTL 7 дней)
GET  /api/public/demo-sandbox/status → статус demo (для tracking)
POST /api/webhooks/n8n               → generic webhook receiver
POST /api/support/ticket             → публичное создание тикета
GET  /api/admin/metrics/health-score → health score по tenant
GET  /api/admin/metrics/usage-summary → usage метрики для отчётов
POST /api/admin/billing/subscribe    → Stripe checkout session
POST /api/webhooks/stripe            → Stripe events receiver
```

### Новые таблицы

```sql
-- Demo sandbox tenants
CREATE TABLE demo_sandboxes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id),
    email VARCHAR(255) NOT NULL,
    company VARCHAR(255),
    expires_at TIMESTAMPTZ NOT NULL,
    activity_log JSONB DEFAULT '[]',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Support tickets
CREATE TABLE support_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id),
    contact_email VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    description TEXT,
    category VARCHAR(50), -- bug, question, feature, billing
    priority VARCHAR(20) DEFAULT 'normal',
    status VARCHAR(20) DEFAULT 'open',
    auto_response TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    resolved_at TIMESTAMPTZ
);

-- Partner referrals
CREATE TABLE partner_referrals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    partner_id UUID NOT NULL,
    lead_email VARCHAR(255) NOT NULL,
    utm_source VARCHAR(255),
    utm_campaign VARCHAR(255),
    deal_value DECIMAL(10,2),
    commission_rate DECIMAL(5,4) DEFAULT 0.10,
    commission_amount DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMPTZ DEFAULT now()
);
```

### Новые background jobs

```
DemoSandboxCleanupJob  — удаление expired demo tenants (daily)
HealthScoreJob         — расчёт health score для всех tenants (daily)
WeeklyMetricsJob       — агрегация метрик для reporting (weekly)
```

---

## ИТОГО: ЭКОНОМИКА АВТОМАТИЗАЦИИ

| Статья | Без автоматизации | С автоматизацией |
|--------|-------------------|------------------|
| SDR (1 чел) | $1,500/мес | $0 |
| Маркетолог (1 чел) | $1,500/мес | $0 |
| CSM (0.5 чел) | $750/мес | $0 |
| Support (0.5 чел) | $750/мес | $0 |
| **Люди итого** | **$4,500/мес** | **$0** |
| Инструменты | $0 | $204/мес |
| Claude API | $0 | $50/мес |
| **Общие расходы** | **$4,500/мес** | **$254/мес** |
| **Экономия** | — | **$4,246/мес ($50,952/год)** |

При целевом ARR $180K экономия $51K/год на персонале — это +28% к марже.
