# 35 — UX Service Blueprint (end-to-end: компания ↔ люди ↔ система)

Дата: 2026-02-01  
Роль: UX Designer (B2B + Ops-heavy продукт)  
Цель: проработать каждый кейс и каждый шаг взаимодействия **и с компанией (процессы/люди/decision)**, и с **системой (Admin Web + Android TV player + support)** так, чтобы это можно было реализовать без додумываний.

Скоуп продукта v1: Kotlin-only, Admin UI — web, endpoints — Android TV, backend — монолит + Postgres, overlay transport — SSE.

---

## 0) Роли, цели и поверхности

### 0.1 Роли (персоны)
- **Buyer / Economic buyer** (CFO/COO/Head of Ops): покупает “снижение затрат/рисков”, хочет ROI и SLA.
- **Champion** (Head of Internal Comms / Ops lead): драйвит внедрение, хочет “быстро и без боли”.
- **Operator** (Marketing/HR/Comms): публикует контент, хочет “2 клика → видно на экранах”.
- **IT Owner / TenantAdmin** (infra/security): отвечает за сеть/MDM/безопасность, хочет контроль и наблюдаемость.
- **Integrator** (dev/подрядчик): подключает данные (очередь/KPI), хочет примеры и стабильные контракты.
- **Support/CS** (мы): снижает инциденты, управляет пилотом → прод.
- **Partner integrator** (AV/SI): ведёт внедрение, хочет повторяемый playbook и enablement kit.

### 0.2 Поверхности (touchpoints)
- **Лендинг/входящий lead**: “Request demo / Pilot”.
- **Pre-sales артефакты**: pilot passport, SOW, security/procurement pack.
- **Admin Web (SSR)**: каналы/расписания/ассеты/устройства/overlay/отчёты/настройки.
- **Android TV Player (kiosk)**: enroll, playback, overlay render, status screen.
- **Support канал**: email/чат + экспорт логов/отчётов.

---

## 1) Сквозной путь “Компания ↔ Мы ↔ Система” (end-to-end)

Ниже — основной “service blueprint” в фазах. Для каждой фазы: **действие компании → действия нашей команды → что должна сделать система → UX outcomes**.

### 1.1 Lead → Qualified lead (QL)

Компания:
1) Находит оффер “Pilot 2–4 weeks”.
2) Оставляет заявку.

Наша команда (sales):
3) Быстро квалифицирует (10 минут).

Система (маркетинг/CRM интеграция — минимум):
4) Создаёт lead/opportunity, фиксирует первичные поля:
   - screens now / target, Android TV yes/no, “нужны данные (очередь/KPI)?”.
5) Авто-письмо: “как работает пилот” (платный, KPI, 1 data source, сроки).

UX outcomes:
- Минимум трения при входе (форма короткая).
- Сразу “setting expectations” (чтобы не тратить 2 недели на неподходящий лид).

### 1.2 Discovery → SQL

Компания:
1) Champion + IT owner участвуют в discovery.
2) Согласуют KPI пилота и объём.

Наша команда:
3) Заполняет “Pilot passport” совместно с клиентом.
4) Определяет блокеры (SSO, сеть, on-prem).

Система:
5) Формирует “Pilot scope summary”:
   - N screens, locations, network constraints, owners, data source type.
6) Создаёт “checklist readiness” (что нужно сделать до старта).

UX outcomes:
- KPI и критерии go/no-go не “где-то в переписке”, а часть продукта и процесса.

### 1.3 Pilot agreed → Kickoff

Компания:
1) Подписывает SOW и назначает ответственных.

Наша команда:
2) Делает kickoff: сеть, MDM, модели устройств, контент-план.

Система:
3) Создаёт tenant / isolated environment.
4) Создаёт первых пользователей (TenantAdmin/Operator).
5) Даёт “Pilot setup wizard” (мастер первого запуска в Admin Web).

UX outcomes:
- Путь к first value структурирован: 1–2 часа до “первый экран играет”.

### 1.4 Deploy → Onboard devices → First publish

Компания (IT):
1) Поднимает доступ/сеть, ставит приложение на устройства.

Компания (Operator):
2) Загружает контент, делает канал, публикует расписание.

Наша команда:
3) Сопровождает первые 5–10 устройств, фиксирует проблемы.

Система:
4) Поддерживает “lean” flow:
   - enroll code может быть привязан к channel → устройство сразу показывает.
5) Показывает “Publish tracker”:
   - X/Y devices applied, список отстающих, причины.

UX outcomes:
- Operator не остаётся “в слепую”: видно, где именно застряло применение.

### 1.5 Pilot execution → Weekly review

Компания:
1) Работает в реальном режиме.
2) Сообщает о проблемах по support‑каналу.

Наша команда:
3) Weekly status: KPI, инциденты, топ-3 улучшения.

Система:
4) “Pilot dashboard”: publish p50/p95, online rate, overlay latency, incidents.
5) Экспорт отчёта (CSV/PDF).

UX outcomes:
- Пилот не превращается в “ощущения”; есть метрики и действия.

### 1.6 Pilot report → Production → Expansion/Renewal

Компания:
1) Принимает решение: go production / extend / stop.

Наша команда:
2) Готовит production plan (R1) и procurement/security ответы.

Система:
3) Генерит итоговый отчёт пилота.
4) Даёт “production blockers checklist”.

UX outcomes:
- У клиента есть документ для внутренней защиты бюджета (ROI + KPI).

---

## 2) Информационная архитектура Admin Web (SSR) — что должно быть в меню

Главный принцип UX: **Home = Ops** (здоровье системы), а не “список сущностей”.

### 2.1 Навигация (минимальная)
- **Home (Ops)**: Fleet health, Publish health, Overlay health, Incidents.
- **Channels**: список → detail (Schedule, Overlay, Devices, History).
- **Assets**: list/upload/validation rules.
- **Devices**: dashboard, enroll codes, groups (R1).
- **Overlay**: profiles, bindings, connectors, manual editor.
- **Reports**: audit, as-run, pilot scorecard.
- **Settings**: users/roles, tenant policies (codecs/bitrate), limits/quotas, exports.

### 2.2 “Пустые состояния” (must)
Каждый раздел в нулевой установке должен иметь один CTA и 3 строки объяснения.
- No channels → “Create first channel”
- No assets → “Upload sample pack”
- No devices → “Generate enroll code”
- No overlay → “Enable manual overlay”

---

## 3) Детальные UX‑кейсы внутри системы (Admin Web + Player)

Формат кейса:
- **Цель**
- **Шаги (UI)**
- **Реакция системы**
- **Ошибки/пограничные состояния**
- **UX заметки**

---

# A) Onboarding и доступы

## A1) Tenant settings (первый запуск)

Цель: задать политики, чтобы пилот не “сломали контентом/трафиком”.

Шаги (Admin Web):
1) Settings → Tenant:
   - timezone
   - offline threshold (например 2–10 минут)
2) Policies:
   - allowed codecs/containers
   - max bitrate / max resolution
   - max asset size
3) Limits:
   - storage quota
   - retention (audit/as-run)

Реакция системы:
- Сохраняет политики; применяет их к upload/publish; пишет audit для критичных настроек.

Ошибки/edge:
- Уменьшение лимита ниже текущего usage → показываем usage + варианты (purge/upgrade).

UX заметки:
- Политики должны быть видны в Upload UI (“почему rejected”) и в Schedule Publish (“почему blocked”).

## A2) Users & roles (RBAC)

Цель: разделить Operator и IT.

Шаги (Admin Web):
1) Settings → Users → Invite.
2) Выбор роли с объяснением “что можно/что нельзя”.

Реакция системы:
- Создаёт пользователя, отправляет invite, пишет audit.

Ошибки/edge:
- Email уже существует → “Resend invite / change role”.

UX заметки:
- Для пилота роли минимальны (TenantAdmin/Operator), но UI должен уметь “показывать roadmap ролей” (Viewer/Integrator).

---

# B) Каналы и расписание (Operator‑центричный UX)

## B1) Создать канал

Цель: завести “что показываем”.

Шаги:
1) Channels → Create.
2) Name, (optional) default overlay profile.

Реакция системы:
- Создаёт channel + audit.
- Показывает next step: “Create schedule draft”.

Edge:
- Дубликат имени в tenant → предупреждение + auto-suggest “HQ / Branch-1”.

## B2) Upload assets (video/image)

Цель: загрузить библиотеку без сюрпризов.

Шаги:
1) Assets → Upload (drag&drop).
2) Прогресс + статус per file: Uploading → Validating → Ready/Rejected.
3) Если Rejected: раскрыть “причина + как исправить”.

Реакция системы:
- Считает checksum.
- Валидирует по policy (codec/bitrate/resolution/size).
- Пишет audit.

Ошибки/edge:
- Quota exceeded → показать usage + CTA “delete assets / request upgrade”.
- Upload interrupted → resume (если возможно) или “retry”.

UX заметки:
- “Rejected reason” должен быть читабельным для не‑инженера (например: “4K запрещён в пилоте”).

## B3) Schedule draft → Publish (core flow)

Цель: безопасно менять показ.

Шаги:
1) Channel detail → Schedule → Create draft.
2) Add items (таблица: order, asset, optional time window).
3) Валидация: подсветить not READY assets.
4) Publish → confirmation:
   - “Изменения применятся ≤ 10 минут (P95)”
   - “Покрытие устройств: X online / Y offline”
5) После publish → “Publish tracker”.

Реакция системы:
- Создаёт immutable scheduleVersion.
- Устройства при poll применяют версию.
- Система фиксирует “schedule_version_applied” и считает publish latency.

Publish tracker (must):
- X/Y devices applied (прогресс)
- список отстающих с причинами:
  - offline
  - old app version
  - playback errors / download errors
- CTA “view device details” для triage.

Ошибки/edge:
- Пустой плейлист → publish blocked + wizard “add fallback”.
- Конфликт версий (двойной draft) → “refresh / create new draft”.

UX заметки:
- Для Operator самое важное: понять “видно ли на экранах и когда будет”.

## B4) Rollback

Цель: быстро откатить ошибку.

Шаги:
1) Channel → Schedule history → выбрать версию.
2) Rollback → обязательное поле “причина”.

Реакция системы:
- Делает rollback, пишет audit.
- Показывает tracker как при publish.

Edge:
- Если нужные assets уже удалены/недоступны → предупреждение и блок.

---

# C) Fleet / Devices (IT‑центричный UX)

## C1) Generate enroll codes (массовый onboarding)

Цель: подключить парк устройств без ручной рутины.

Шаги:
1) Devices → Enroll codes → Generate:
   - count
   - TTL
   - (optional) bind to channel
   - label/location
2) Результат: список code + QR payload + “copy/print”.

Реакция системы:
- Генерит одноразовые коды, пишет audit.

Edge:
- TTL слишком большой по policy → подсказка и ограничение.

UX заметки:
- “Bind to channel” должен быть в UI как рекомендуемый “pilot shortcut”.

## C2) Player enroll (Android TV)

Цель: устройство становится управляемым.

Шаги (Player):
1) Enter code.
2) Enrolling… (показываем device info).
3) Success:
   - Assigned → Starting playback
   - Not assigned → Waiting screen + deviceId
4) Hidden shortcut: Status screen.

Реакция системы:
- Создаёт device, выдаёт refresh token + device JWT, фиксирует device info.

Edge:
- Code expired/used → понятная ошибка + “request new code”.
- Network down → retry/backoff, объяснение “видео будет играть из кэша после первичной настройки”.

## C3) Devices dashboard (Ops view)

Цель: IT видит здоровье парка.

UI:
- Таблица: online/offline, last seen, channel, schedule version applied, app version, last error, SSE status.
- Фильтры: location, channel, status, version.

Реакция системы:
- Считает offline по threshold.
- Хранит last known good config/playlist info per device.

Edge:
- Flapping connectivity → пометка “unstable”.

## C4) Assign channel (если не через enroll code)

Шаги:
1) Device detail → Assign channel → confirmation.
2) Показываем “next poll at ~ …”.

Реакция системы:
- Сохраняет assignment, audit event.

## C5) Revoke device (security)

Шаги:
1) Device detail → Revoke → причина обязательна.

Реакция системы:
- Инвалидирует токены/версию, audit.

---

# D) Overlay / Data layer (Operator + Integrator)

## D1) Overlay profiles (без визуального редактора)

Цель: давать “ценность данных” без сложного UI.

Шаги:
1) Overlay → Profiles → Create.
2) Выбор шаблона: Ticker / KPI tiles / Queue table / QR card.
3) Настройки: позиция, размеры, базовые стили.

Реакция системы:
- Сохраняет profile definition_json.

Edge:
- Слишком большой layout/state → лимит + объяснение.

## D2) Bind overlay to channel

Шаги:
1) Channel → Overlay → Enable.
2) Choose profile.
3) Choose source: Manual / REST pull / Webhook.
4) (Optional) Canary-only (показывать на группе устройств).

Реакция системы:
- Создаёт binding, audit.

## D3) Manual overlay updates (pilot-first)

Шаги:
1) Channel → Overlay → Manual editor (форма по виджетам).
2) Preview → Send.
3) Applied tracker: X/Y devices + latency p50/p95.

Реакция системы:
- Обновляет overlay state, генерит patch, пушит в SSE.

Edge:
- SSE down → UI показывает “доставлено на сервер; устройства подтянут при reconnect”.

## D4) REST pull connector (integrator-friendly)

Шаги:
1) Binding → Source config: URL, auth, polling interval.
2) Mapping preset (queue/kpi/ticker) + поля.
3) Test fetch → raw + mapped preview + validate limits.
4) Enable.

Реакция системы:
- Job polling; хранит last success, last error, connector status.

Edge:
- 401/403 → подсказка “token expired”.
- Payload too big → лимиты + “send only needed fields”.

## D5) Webhook push (enterprise)

Шаги:
1) Binding → Webhook:
   - endpoint URL
   - signing secret
   - пример payload
2) Logs: last 20 webhook calls (status/latency/errors).

Реакция системы:
- Проверяет подпись; rate limit; пишет audit для изменений конфигурации.

---

# E) Audit / As-run / отчётность (IT + Compliance + Support)

## E1) Audit log

Шаги:
1) Reports → Audit → фильтры: entity, actor, action, time.
2) View diff.
3) Export CSV.

Реакция системы:
- Возвращает события с requestId и actor info.

## E2) As-run report

Шаги:
1) Reports → As-run.
2) Выбор device/channel + период.
3) Таймлайн + summary.
4) Export CSV/PDF.

Реакция системы:
- Агрегирует события; показывает “unknown gaps” если устройство было offline.

UX заметки:
- Для комплаенса важнее “доказуемость” и “экспорт”, чем красота.

---

# F) Инциденты и поддержка (support-grade UX)

## F1) Alerts (минимум)

UI:
- Alerts list (tenant-scoped):
  - DB down
  - app down
  - online devices < X%
  - publish failures spike
  - overlay connector failures

Каждый alert:
- What happened
- Likely causes
- Next actions (конкретные шаги)

## F2) Triage “экран не показывает”

Admin Web:
1) Devices dashboard → device detail.
2) Видим: last seen, current asset, schedule version, last errors, SSE status.
3) “Copy support bundle” (deviceId, tenantId, timestamps, last errors).

Player:
- Status screen:
  - network
  - last config time
  - last playlist time
  - current asset
  - SSE status
  - last errors
  - deviceId

UX заметки:
- Главное: минимизировать “переписку” и дать support однозначные данные.

## F3) Publish не применился

Admin Web:
- Publish tracker показывает отстающих.
- Для каждого устройства: причина + ссылка на device detail.

---

## 4) “UX Guardrails” (обязательные ограничения в интерфейсе)

Чтобы пилоты не сжигали команду:
- Явные product boundaries в UI (“Android TV only”, “1 data source in pilot”, “no visual editor”).
- Везде подсказки “что будет дальше” (apply time, polling interval, offline threshold).
- Везде “debuggability”:
  - applied X/Y
  - last seen
  - last error
  - last connector success
  - last publish time

---

## 5) Чек‑лист реализации UX (что должно появиться в продукте)

P0 (для R0 пилота):
- Onboarding wizard в Admin Web.
- Publish tracker (X/Y applied + причины).
- Assets upload с человеко‑понятными reject reasons.
- Devices dashboard с last seen + last error + app version.
- Manual overlay editor + applied tracker.
- Player status screen + экспорт логов (support bundle).
- Reports: audit + as-run с экспортом CSV.

P1 (для production hardening):
- Canary rollout support (на уровне планов/групп).
- Webhook logs и подпись.
- Procurement/security “download pack” + response library.
- CS health score и expansion triggers.

