# План изменений под стратегию продаж 2026

> Что нужно изменить в коде, сайте и инфраструктуре, чтобы реализовать стратегию из `strategy/Playout_Edge_Strategy.docx`.
> Приоритеты: P0 = блокер продаж, P1 = нужно до первых пилотов, P2 = до конца Q2, P3 = Q3-Q4.

---

## 1. САЙТ (tv.adapto.kz) — Публичные страницы

### 1.1 [P0] Русификация публичных страниц

**Проблема:** Сайт на английском, целевой рынок — Казахстан/СНГ. Sales не может отправить клиенту ссылку на сайт.

**Файлы для изменения:**
- `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/LandingView.kt`
- `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/FeaturesView.kt`
- `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/PricingView.kt`
- `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/FaqView.kt`
- `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/ContactView.kt`
- `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/SignupView.kt`
- `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/PublicLayout.kt`

**Что сделать:**
- Перевести весь текст на русский
- Добавить переключатель языков RU/EN/KZ (в навбар)
- Вынести строки в ресурсные файлы или объект-словарь для i18n
- Обновить meta-теги (title, description) на русском

**Оценка:** 2-3 дня

---

### 1.2 [P0] Обновление тарифов на странице /pricing

**Проблема:** На сайте $29/$79/custom — не соответствует B2B-стратегии ($500/$2,000/custom).

**Файл:** `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/PricingView.kt`

**Что сделать:**
- Заменить тарифы:
  - Basic → **Pilot**: $500/мес, до 50 устройств, 3 канала, базовые оверлеи, email-поддержка
  - Premium → **Business**: $2,000/мес, до 500 устройств, 10 каналов, все оверлеи, аудит, приоритетная поддержка
  - Enterprise → **Enterprise**: по запросу, безлимит, выделенный менеджер, SLA 99.9%
- Добавить строку "Бесплатный пилот 4-6 недель" с CTA
- Добавить toggle "Месяц / Год (скидка 20%)"
- Добавить сравнительную таблицу фич по тарифам

**Связано с:** `docs/24-kz-pricebook.md`, `docs/27-pricing-architecture-and-discounting.md`

**Оценка:** 1 день

---

### 1.3 [P0] SEO-оптимизация

**Проблема:** Нет meta-тегов, OG-тегов, sitemap, robots.txt — сайт невидим для Google.

**Файлы для изменения:**
- `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/PublicLayout.kt` — добавить meta/OG
- `apps/server/src/main/kotlin/com/playoutedge/server/routes/landing/PublicLandingRoutes.kt` — маршруты для robots.txt, sitemap.xml

**Что добавить:**

```kotlin
// В PublicLayout.kt — в <head>
meta(name = "description", content = "Playout Edge — платформа автоматизации вещания...")
meta(name = "keywords", content = "playout, автоматизация вещания, digital signage, казахстан")
meta(name = "author", content = "Adapto")
meta(property = "og:title", content = pageTitle)
meta(property = "og:description", content = pageDescription)
meta(property = "og:image", content = "/admin/static/og-image.png")
meta(property = "og:url", content = "https://tv.adapto.kz$currentPath")
meta(property = "og:type", content = "website")
meta(name = "twitter:card", content = "summary_large_image")
link(rel = "canonical", href = "https://tv.adapto.kz$currentPath")
```

**Новые маршруты:**
- `GET /robots.txt` — стандартный robots.txt
- `GET /sitemap.xml` — XML sitemap со всеми публичными страницами
- Structured data (JSON-LD) для Organization и SoftwareApplication

**Оценка:** 1-2 дня

---

### 1.4 [P1] Аналитика и трекинг конверсий

**Проблема:** Нет Google Analytics — невозможно измерить эффект маркетинга.

**Файл:** `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/PublicLayout.kt`

**Что добавить:**
- Google Analytics 4 (gtag.js) — в `<head>`
- UTM-параметры: парсинг и сохранение в cookie/session
- События конверсий:
  - `signup_started` — открыта форма регистрации
  - `signup_completed` — регистрация завершена
  - `pricing_viewed` — просмотр тарифов
  - `contact_submitted` — отправка формы контактов
  - `demo_requested` — запрос демо (новый CTA)
- Facebook Pixel (опционально, для ретаргетинга)
- Yandex.Metrica (для KZ/RU трафика)

**Связано с:** `docs/38-marketing-ops-tracking-and-analytics.md`

**Оценка:** 1 день

---

### 1.5 [P1] Форма "Запросить демо" и лидогенерация

**Проблема:** Нет CTA для запроса демо — основная точка входа по стратегии.

**Новые файлы:**
- `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/DemoRequestView.kt`
- `apps/server/src/main/kotlin/com/playoutedge/server/routes/landing/DemoRequestRoutes.kt`

**Что сделать:**
- Новая страница `/demo` с формой: имя, компания, должность, email, телефон, сегмент (dropdown), комментарий
- CTA-кнопка "Запросить демо" на каждой странице (лендинг, фичи, цены)
- При отправке:
  - Сохранить лид в таблицу `demo_requests` (новая таблица)
  - Отправить email-нотификацию на sales@adapto.kz
  - Отправить auto-reply клиенту с подтверждением
  - Трекинг конверсии в GA4
- Popup "Запросить демо" при exit-intent (опционально)

**Новая таблица:**
```sql
CREATE TABLE demo_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    position VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    segment VARCHAR(50),  -- tv, bank, retail, government, other
    comment TEXT,
    utm_source VARCHAR(255),
    utm_medium VARCHAR(255),
    utm_campaign VARCHAR(255),
    status VARCHAR(20) DEFAULT 'new',  -- new, contacted, qualified, converted, lost
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
```

**Оценка:** 2-3 дня

---

### 1.6 [P1] Блог / Контент-хаб

**Проблема:** Нет блога — стратегия предусматривает 2 статьи/месяц для SEO и лидогенерации.

**Варианты реализации:**
1. **Простой (рекомендуется для MVP):** Markdown-файлы в `resources/blog/` → SSR через kotlinx.html
2. **Средний:** Headless CMS (Strapi/Ghost) + API
3. **Минимальный:** Внешний блог на habr.com/vc.ru (без изменений в коде)

**Для варианта 1:**

Новые файлы:
- `apps/server/src/main/kotlin/com/playoutedge/server/routes/landing/BlogRoutes.kt`
- `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/BlogListView.kt`
- `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/BlogPostView.kt`
- `apps/server/src/main/resources/blog/*.md` — статьи в Markdown

**Маршруты:**
- `GET /blog` — список статей
- `GET /blog/{slug}` — статья
- RSS-фид: `GET /blog/rss.xml`

**Первые статьи (по контент-плану):**
1. "5 причин автоматизировать плейаут в 2026 году"
2. "Playout Edge vs Digital Signage: в чём разница?"
3. "Как банки используют динамические экраны для управления очередями"

**Оценка:** 3-5 дней (движок) + ongoing (контент)

---

### 1.7 [P2] Страница кейсов / Social proof

**Проблема:** Одна testimonial на signup. Нет кейсов — стратегия требует social proof.

**Новый файл:** `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/CaseStudiesView.kt`

**Что сделать:**
- Страница `/cases` с карточками кейсов
- Структура кейса: компания, сегмент, проблема, решение, результат, цитата
- Лого клиентов на главной странице (раздел "Нам доверяют")
- Первые кейсы появятся после пилотов Q1-Q2

**Оценка:** 1-2 дня (шаблон)

---

## 2. КОД / BACKEND

### 2.1 [P1] Email-нотификации

**Проблема:** Нет отправки email. Нужно для: демо-запросы, welcome-письма, алерты.

**Что сделать:**
- Добавить зависимость на email-библиотеку (jakarta.mail или kotlinx-email)
- Создать `EmailService` с шаблонами:
  - `demo_request_notification` → sales@adapto.kz
  - `demo_request_confirmation` → клиенту
  - `signup_welcome` → новому пользователю
  - `pilot_weekly_report` → клиенту на пилоте
- SMTP config через env variables (Mailgun/SendGrid/SMTP)
- Очередь email через существующий job scheduler

**Новые файлы:**
- `apps/server/src/main/kotlin/com/playoutedge/server/services/EmailService.kt`
- `apps/server/src/main/kotlin/com/playoutedge/server/services/EmailTemplates.kt`

**Оценка:** 2-3 дня

---

### 2.2 [P1] Webhook для CRM-интеграции

**Проблема:** По стратегии нужна интеграция с CRM (Bitrix24/AmoCRM). Лиды должны попадать автоматически.

**Что сделать:**
- Webhook-endpoint для отправки событий: новый signup, демо-запрос, пилот начат
- Формат: JSON POST на configurable URL
- Retry logic через job scheduler
- Начать с простого webhook → потом нативные интеграции

**Связано с:** `docs/37-crm-spec-and-automations.md`

**Оценка:** 1-2 дня

---

### 2.3 [P2] Мультиязычность (i18n)

**Проблема:** Сайт только на EN, admin UI на EN. Нужно RU минимум, KZ желательно.

**Что сделать:**
- Создать систему i18n для kotlinx.html views
- Ресурсные файлы: `messages_ru.properties`, `messages_en.properties`, `messages_kz.properties`
- Определение языка: URL-параметр (`?lang=ru`) → cookie → Accept-Language header
- Приоритет: публичные страницы (P1) → admin UI (P3)

**Оценка:** 3-5 дней (framework) + 2-3 дня (перевод)

---

### 2.4 [P2] Admin Dashboard — метрики для sales

**Проблема:** Dashboard не показывает метрики, полезные для sales (кол-во просмотров, активность устройств, ROI-данные).

**Что добавить в dashboard:**
- Общее кол-во устройств online/offline (уже есть частично)
- Средний uptime за период
- Топ-5 активов по воспроизведениям
- Графики: устройства online за 30 дней, воспроизведения/день
- Экспортируемый "Monthly Report" для клиента (PDF)

**Связано с:** `docs/30-customer-success-and-support.md`

**Оценка:** 3-5 дней

---

### 2.5 [P2] API для интеграции с 1С

**Проблема:** Стратегия указывает интеграцию с 1С как конкурентное преимущество. Сейчас нет.

**Что сделать:**
- Webhook/REST endpoint для получения данных из 1С (очереди, KPI, расписания)
- Документация формата данных
- Пример скрипта интеграции для 1С
- Overlay data source: `type = "rest_poll"` с поддержкой 1С-формата

**Оценка:** 3-5 дней

---

## 3. DEPLOY / ИНФРАСТРУКТУРА

### 3.1 [P1] Email-сервер (SMTP)

**Что сделать:**
- Настроить Mailgun или SendGrid (SPF/DKIM/DMARC для adapto.kz)
- Добавить env variables в `deploy/group_vars/production.yml`:
  ```yaml
  smtp_host: smtp.mailgun.org
  smtp_port: 587
  smtp_user: postmaster@adapto.kz
  smtp_password: "{{ vault_smtp_password }}"
  smtp_from: noreply@adapto.kz
  ```
- Настроить DNS-записи (MX, SPF, DKIM) для adapto.kz

**Оценка:** 0.5 дня

---

### 3.2 [P1] Google Analytics + Search Console

**Что сделать:**
- Создать GA4 property для tv.adapto.kz
- Добавить GA4 ID в env config
- Подключить Google Search Console
- Отправить sitemap.xml
- Настроить goals/конверсии

**Оценка:** 0.5 дня

---

### 3.3 [P2] Staging-окружение

**Проблема:** Нет staging — все изменения идут сразу в prod. Рискованно при частых обновлениях.

**Что сделать:**
- Добавить staging host в `deploy/inventory.ini`
- Отдельная БД для staging
- Поддомен: staging.tv.adapto.kz
- CI/CD: auto-deploy в staging при push в develop

**Оценка:** 1-2 дня

---

## 4. МАРКЕТИНГОВЫЕ АКТИВЫ (не код)

### 4.1 [P0] OG-изображение для соцсетей

**Что сделать:**
- Создать `og-image.png` (1200×630px) с лого и tagline
- Разместить в `apps/server/src/main/resources/static/og-image.png`

### 4.2 [P1] Демо-видео для YouTube

**Что сделать:**
- 3-минутный скринкаст: создание канала → загрузка → расписание → публикация → отображение на устройстве
- Встроить на лендинг и страницу features

### 4.3 [P1] One-pager PDF

**Что сделать:**
- 1-страничный PDF для быстрой рассылки по email
- Содержание: проблема, решение, 3 USP, тарифы, контакт
- Разместить в `docs/sales/`

### 4.4 [P2] Telegram-канал

**Что сделать:**
- Создать @playoutedge (или @adapto_kz)
- Контент: новости продукта, кейсы, инсайты индустрии
- Ссылка на сайте в footer

### 4.5 [P2] LinkedIn Company Page

**Что сделать:**
- Создать страницу Adapto на LinkedIn
- Настроить LinkedIn Ads targeting (CTO/IT directors в КЗ)

---

## 5. СОЦИАЛЬНЫЕ ЭЛЕМЕНТЫ САЙТА

### 5.1 [P2] Footer с соцсетями и контактами

**Файл:** `apps/server/src/main/kotlin/com/playoutedge/server/views/landing/PublicLayout.kt`

**Что добавить:**
- Ссылки: Telegram, LinkedIn, YouTube
- Email: sales@adapto.kz, support@adapto.kz
- Телефон
- Юридическое лицо и адрес (КЗ)
- Ссылки на Privacy Policy и Terms of Service

### 5.2 [P3] Live Chat / Support Widget

**Что сделать:**
- Интегрировать Tawk.to или Crisp (бесплатные планы)
- Скрипт в PublicLayout.kt
- Время работы: 9:00-18:00 ALMT

---

## 6. СВОДНАЯ ТАБЛИЦА ПРИОРИТЕТОВ

| # | Задача | Приоритет | Оценка | Квартал |
|---|--------|-----------|--------|---------|
| 1.1 | Русификация публичных страниц | P0 | 2-3 дня | Q1 |
| 1.2 | Обновление тарифов | P0 | 1 день | Q1 |
| 1.3 | SEO (meta, OG, sitemap, robots) | P0 | 1-2 дня | Q1 |
| 4.1 | OG-изображение | P0 | 0.5 дня | Q1 |
| 1.4 | Аналитика (GA4, Yandex.Metrica) | P1 | 1 день | Q1 |
| 1.5 | Форма "Запросить демо" + таблица лидов | P1 | 2-3 дня | Q1 |
| 2.1 | Email-нотификации | P1 | 2-3 дня | Q1 |
| 2.2 | Webhook для CRM | P1 | 1-2 дня | Q1 |
| 3.1 | SMTP (Mailgun/SendGrid) | P1 | 0.5 дня | Q1 |
| 3.2 | GA4 + Search Console | P1 | 0.5 дня | Q1 |
| 4.2 | Демо-видео YouTube | P1 | 1 день | Q1 |
| 4.3 | One-pager PDF | P1 | 0.5 дня | Q1 |
| 1.6 | Блог / контент-хаб | P2 | 3-5 дней | Q2 |
| 1.7 | Страница кейсов | P2 | 1-2 дня | Q2 |
| 2.3 | Мультиязычность (i18n) | P2 | 5-8 дней | Q2 |
| 2.4 | Dashboard метрики для sales | P2 | 3-5 дней | Q2 |
| 2.5 | API для 1С | P2 | 3-5 дней | Q2 |
| 3.3 | Staging-окружение | P2 | 1-2 дня | Q2 |
| 4.4 | Telegram-канал | P2 | 0.5 дня | Q2 |
| 4.5 | LinkedIn Company Page | P2 | 0.5 дня | Q2 |
| 5.1 | Footer с соцсетями | P2 | 0.5 дня | Q2 |
| 5.2 | Live Chat widget | P3 | 0.5 дня | Q3 |

**Итого P0 (блокеры):** ~5-7 дней
**Итого P1 (до пилотов):** ~9-14 дней
**Итого P2 (до конца Q2):** ~18-29 дней

---

## 7. ПОРЯДОК РЕАЛИЗАЦИИ (рекомендуемый)

### Спринт 1 (неделя 1): Базовая видимость
1. SEO (meta, OG, sitemap, robots.txt)
2. Обновление тарифов на /pricing
3. OG-изображение
4. GA4 + Search Console setup

### Спринт 2 (неделя 2): Русификация
5. Перевод всех публичных страниц на русский
6. Footer с контактами и соцсетями

### Спринт 3 (неделя 3): Лидогенерация
7. Форма "Запросить демо" + таблица demo_requests
8. SMTP setup (Mailgun)
9. Email-нотификации (демо-запрос, welcome)

### Спринт 4 (неделя 4): Интеграции
10. Webhook для CRM
11. Yandex.Metrica
12. One-pager PDF

### Спринт 5-6 (недели 5-6): Контент
13. Блог-движок
14. Первые 2-3 статьи
15. Демо-видео

### Q2: Масштабирование
16. i18n (KZ)
17. Страница кейсов
18. Dashboard метрики
19. API для 1С
20. Staging
