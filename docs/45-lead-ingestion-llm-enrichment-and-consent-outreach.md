# 45 — Lead ingestion + LLM enrichment + consent-based outreach (email/SMS/WhatsApp/Telegram/calls)

Дата: 2026-02-01  
Цель: описать дешёвый и эффективный pipeline “контакты → качественные лиды → очереди коммуникаций”, но **только в легальном/полиси‑совместимом режиме**.

Важно (границы): система **не предназначена** для спама, “серых” купленных баз без согласий, обхода правил операторов/мессенджеров или скрытого сбора персональных данных. Если нет законного основания/согласия — контакт не должен попадать в outbound очереди.

Связанные документы:
- `docs/37-crm-spec-and-automations.md` (CRM объекты и pipeline)
- `docs/38-marketing-ops-tracking-and-analytics.md` (tracking + privacy/consent минимум)
- `docs/20-collateral-templates.md` (шаблоны писем/коммерческих материалов)

---

## 1) Принцип “дешево”: не строим свой CRM, строим LeadOps слой

Рекомендация для маленького бюджета:
- **CRM остаётся внешним** (HubSpot/Pipedrive/Airtable).
- Мы добавляем тонкий **LeadOps слой** (скрипт/джоб/микросервис), который:
  1) импортирует сырьё (файлы/таблицы),
  2) нормализует и дедуплицирует,
  3) делает LLM-enrichment/классификацию,
  4) проверяет consent/policy,
  5) пушит “готовые” записи и задачи в CRM,
  6) ставит коммуникации в очереди через провайдеров (если разрешено).

Так дешевле, чем “свой CRM”, и безопаснее, чем смешивать продукт Playout Edge и outbound‑автоматизацию в одном сервисе.

---

## 2) Источники данных и обязательная трассируемость

### 2.1 Допустимые “сигналы” для поиска компаний (не = контакты)

Иногда хочется брать “рыночные сигналы” (например, новые домены/новые компании) и ставить их в очередь на исследование. Это можно делать, но принцип такой:
- сигнал ≠ персональные данные; на этом этапе храним **только** company‑уровень (домен, индустрия, город, сайт),
- контактные данные добавляются позже **только** из разрешённых источников и только с фиксацией `source_*` и `lawful_basis`,
- любые попытки “добывать” персональные контакты из источников, где это запрещено правилами/законом, должны блокироваться политиками импорта.

### 2.2 Обязательная трассируемость

Каждая запись контакта обязана иметь:
- `source_type` (INBOUND_FORM/REFERRAL/EVENT/PARTNER/SELF_PROSPECTING/OTHER)
- `source_ref` (ссылка/идентификатор: форма, событие, партнёр, import batch id)
- `collected_at`
- `lawful_basis` (CONSENT/LEGITIMATE_INTEREST/CONTRACT/OTHER) — как минимум “для внутреннего контроля”
- `consent_proof` (если применимо): текст, чекбокс, double opt-in, запись разговора/заявка, дата/канал

Без `source_*` и `lawful_basis` запись не может:
- уходить в массовые sequences,
- уходить в SMS/WhatsApp/Telegram,
- уходить в автодозвон.

---

## 3) LLM ingestion/enrichment: как “качественно загонять” контакты

### 3.1 Входные форматы (дешёвый минимум)

- CSV/XLSX export (контакты/компании)
- VCF (визитки)
- “сырой текст” (копипаста из письма/списка) — через UI “Paste & parse”

### 3.2 Пайплайн обработки (детерминированный + LLM)

1) **Parse**: извлечь поля (name/company/role/email/phone/city/site) → черновик.
2) **Normalize**:
   - телефоны → E.164
   - домены/email → lowercase + punycode safe
   - компании → канонизация (LLM может предложить “нормализованное имя”, но сохраняем raw)
3) **Dedup**:
   - email exact match
   - phone exact match
   - company+name fuzzy (опционально)
4) **Enrichment (LLM)** — строго в рамках минимизации данных:
   - `persona` (Buyer/Champion/IT Owner/Operator/Integrator/Other) как в `docs/37-crm-spec-and-automations.md`
   - `segment` (ICP-A/ICP-B/Other)
   - “суть компании” (1–2 предложения) по публичным данным, которые вы законно предоставили в контекст
   - `confidence_score` (0–1) и причины
5) **Routing**:
   - если `consent_ok=true` → можно в outbound очереди
   - если `consent_unknown` → только “manual research task” в CRM, без отправок

### 3.3 Технические требования к LLM (чтобы было безопасно и воспроизводимо)

- LLM получает **минимум PII** (не отправляем лишние поля, не добавляем “скрытые” источники).
- Любое LLM решение должно иметь:
  - `input_hash`, `model`, `prompt_version`, `output_json`, `confidence`
  - возможность повторить прогон (re-run) на том же input
- Все LLM outputs валидируются JSON-schema (иначе отклоняем batch).

---

## 4) Outreach: очереди и каналы (только consent-based)

### 4.1 Каналы и ключевые ограничения

- **Email**: обязательно `unsubscribe`, suppression list, доменная репутация (SPF/DKIM/DMARC).
- **SMS**: только при допустимом основании и с opt-out; rate limits.
- **WhatsApp**: только через официальные каналы (WhatsApp Business) и только при допустимых правилах/оптах; шаблоны сообщений.
- **Telegram**: как правило, только если пользователь сам начал диалог/подписался (бот/канал) — иначе спам-риски и блокировки.
- **Calls (Twilio)**: только при законном основании/согласии, с логированием attempts и DNC.

### 4.2 Универсальная модель очереди (outbox)

Не нужен Kafka/Redis — достаточно Postgres (см. принципы в `docs/02-target-architecture.md`).

Таблица (логическая модель):
- `outreach_messages(id, contact_id, company_id, channel, template_id, payload_json, status, scheduled_at, attempt, last_error, provider_message_id, created_at)`

Статусы:
- `DRAFT` (создано, но не отправляется)
- `QUEUED`
- `SENT`
- `DELIVERED` (если провайдер даёт)
- `FAILED` / `BOUNCED`
- `UNSUBSCRIBED` / `SUPPRESSED`

Правила отправки:
- перед каждым `SENT` выполняем policy-check:
  - есть ли `consent_ok`
  - нет ли `suppression`
  - не превышены ли лимиты (per contact/per company/per day)

### 4.3 Human-in-the-loop (экономит деньги и снижает риск)

Для холодных/сомнительных записей:
- LLM может предложить “draft message”, но отправка только после ручного approve.

---

## 5) Минимальная “анализ/скоринг” часть (чтобы было полезно)

Дешёвый анализ, который реально помогает продажам:
- **Lead quality score** (0–100): размер компании/релевантность/наличие Android TV/роль/срок.
- **Next best action**: “назначить discovery”, “попросить IT owner”, “отправить prerequisites pack”.
- **Воронка по источникам**: откуда приходят реально качественные лиды (связать с UTM в `docs/38...`).

---

## 6) Минимальный список “красных флагов” (автоблок)

Запись не попадает в outbound очереди, если:
- отсутствуют `source_type` и `lawful_basis`
- `consent_required=true`, но `consent_proof` отсутствует
- контакт помечен `do_not_contact=true`
- канал запрещён политиками провайдера/клиента (например, WhatsApp без opt-in)

---

## 7) Что сделать за 1–3 дня (реалистичный MVP)

1) Import batch UI (CSV upload) + `import_batch_id` + preview ошибок.
2) Нормализация + дедуп по email/phone.
3) LLM классификация persona/segment + confidence.
4) Export “готовых” лидов в CRM (через CSV или API).
5) Email sequence только для consent_ok + unsubscribe.
6) Suppression list (ручная + автоматическая из bounces/unsubs).
