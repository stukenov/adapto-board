# 15 — MVP Backlog (R0) — детально и без расползания

Цель: зафиксировать то, что команда реально сделает в R0, с проверяемыми AC. Всё, что не входит — отдельный список “не сейчас”.

## 0) Правило R0

R0 считается успешным, если:
- Android TV экраны показывают контент 24/7 с кэшем и fallback,
- публикация расписания применима “в разумные сроки” (≤ 10 минут P95),
- есть статусы устройств, audit и базовый as-run,
- overlay работает минимум в manual mode + 1 источник данных.

## 1) Epic R0-A: Tenancy, Users, RBAC (минимум)

### R0-A1: Tenant bootstrap

- Story: как системный админ, я могу создать tenant, чтобы начать пилот.
- AC:
  - tenant создаётся со статусом ACTIVE
  - все записи изолированы по tenant_id

### R0-A2: Пользователи и роли

- Story: как TenantAdmin, я могу создать пользователя и назначить роль.
- AC:
  - роли v1 (lean): TenantAdmin/Operator
  - Viewer/Integrator откладываем: в R0 эти права покрываются TenantAdmin
  - доступы ограничены по роли
  - audit: CREATE_USER, ASSIGN_ROLE

## 2) Epic R0-B: Assets (upload + storage + выдача)

### R0-B1: Upload asset

- Story: как Operator, я могу загрузить видео/картинку.
- AC:
  - upload принимает только разрешённые типы
  - asset получает checksum_sha256
  - статус READY или REJECTED с причиной
  - audit: ASSET_UPLOADED

### R0-B2: Download URL

- Story: как Player, я получаю URL для скачивания assets.
- AC:
  - выдаётся URL с TTL
  - доступ ограничен tenant/device

## 3) Epic R0-C: Channels & Schedules (draft/publish)

### R0-C1: Канал

- Story: как Operator, я создаю канал и вижу его настройки.
- AC:
  - create/list/update
  - audit: CHANNEL_CREATED/UPDATED

### R0-C2: Draft schedule

- Story: как Operator, я создаю draft версию расписания.
- AC:
  - draft version создаётся с version+1
  - можно заменить список items целиком (v1 простота)

### R0-C3: Publish schedule

- Story: как Operator, я публикую расписание.
- AC:
  - published version immutable
  - устройства получают новую version через polling
  - audit: SCHEDULE_PUBLISHED

## 4) Epic R0-D: Fleet (enroll, assign, heartbeat)

### R0-D1: Enroll codes

- Story: как Admin, я генерирую enroll code для устройства.
- AC:
  - code одноразовый, TTL
  - можно инвалидировать code
  - (lean) enroll code может быть “предназначен” для конкретного channelId, чтобы сократить шаг assignment

### R0-D2: Device enroll (Android TV)

- Story: как Device, я ввожу code и получаю учётные данные.
- AC:
  - device появляется в админке
  - сохраняется device info (model/version/app)

### R0-D3: Assign channel

- Story: как Admin, я назначаю устройству канал.
- AC:
  - устройство применяет assignment при следующем config poll
  - audit: DEVICE_ASSIGNED
  - (lean) если enroll code был привязан к channelId, отдельный шаг assignment не обязателен

### R0-D4: Heartbeat

- Story: как Admin, я вижу online/offline по heartbeat.
- AC:
  - last_seen_at обновляется
  - offline threshold настраиваемый

## 5) Epic R0-E: Android TV player (кэш, playback, fallback)

### R0-E1: Config polling

- Story: как Player, я периодически получаю config.
- AC:
  - хранится last known good config
  - backoff при ошибках

### R0-E2: Playlist fetch + cache

- Story: как Player, я скачиваю manifest и кэширую assets.
- AC:
  - кэш на диске с квотой
  - checksum validation

### R0-E3: Playback loop

- Story: как Viewer, я вижу непрерывное воспроизведение.
- AC:
  - ошибка asset не останавливает показ (skip + log)
  - fallback screen при критической ошибке

## 6) Epic R0-F: Overlay (SSE + manual + 1 источник)

### R0-F1: SSE stream

- Story: как Player, я подписываюсь на overlay stream.
- AC:
  - `state` при подключении, `patch` далее
  - keepalive

### R0-F2: Manual overlay

- Story: как Operator, я меняю overlay вручную.
- AC:
  - изменение видно на устройствах канала
  - audit: OVERLAY_UPDATED

### R0-F3: REST pull connector

- Story: как TenantAdmin, я настраиваю pull endpoint для overlay.
- AC:
  - polling interval configurable
  - ошибки коннектора видны в админке

## 7) Epic R0-G: Audit + As-run (coarse)

### R0-G1: Audit log

- Story: как IT, я вижу кто и что менял.
- AC:
  - фильтры по entity/time
  - actor: USER/SYSTEM/DEVICE

### R0-G2: As-run events

- Story: как IT, я вижу что реально показывалось на устройстве.
- AC:
  - устройство шлёт события батчем
  - отчёт по периоду на устройстве/канале

## 8) Epic R0-H: Web Admin UI (минимально работоспособная)

Цель: операторы и админы могут выполнить R0 сценарии без CLI.

Минимальные экраны:
- Login
- Channels list/detail + publish
- Assets list/upload
- Devices list + assign
- Overlay manual edit
- Audit/as-run views

Формат UI v1 (lean):
- server-rendered (SSR) web UI, без “визуальных редакторов”, без drag&drop — только формы/таблицы.

## 9) “Не сейчас” (explicitly out of R0)

- Поддержка платформ кроме Android TV
- Сложный визуальный редактор шаблонов
- Мульти-профили перекодирования (если не требуется для пилота)
- Remote commands (reboot/app restart) — только в R1 при необходимости
- WebSocket transport (остаёмся на SSE)

## 10) TODO (новые UX/шаблонные функции)

- [ ] Добавить функционал деления экрана по секциям (region/split layout): отдельные независимые зоны для видео/трансляции, статики и информационных виджетов.
  - Пример: в одной секции только видео/скрин, в другой — только информационный блок.
  - Поддержать пресеты (50/50, 70/30, верх/низ, 3 зоны) и привязку виджетов по секциям.
