# 11 — Метрики успеха, OKR и план измерений (telemetry)

Цель: формализовать, что считаем успехом и как это измеряем. Всё, что не измеряем, обычно не улучшается.

## 1) North Star Metric (NSM)

**Active Screens with Data Layer** = кол-во экранов, которые:
- online (получают heartbeat),
- воспроизводят контент (есть as-run/heartbeat snapshot),
- и имеют активный overlay binding.

Почему: это одновременно “работает” и “за что платят” (overlay ценность).

## 2) Ключевые метрики продукта (P0)

### 2.1 Time-to-publish

- определение: время от `publish` в админке до момента, когда экран начал воспроизводить новую `scheduleVersionId`.
- цели:
  - P50 ≤ 2 минуты
  - P95 ≤ 10 минут

### 2.2 Playback uptime

- определение: доля времени, когда устройство воспроизводит плейлист без “black screen”.
- цель: ≥ 99.9% / месяц.

### 2.3 Online rate

- определение: доля устройств, присылающих heartbeat в пределах `offline_threshold`.
- цель: ≥ 95% в любой момент (с учётом реальных сетей).

### 2.4 Overlay latency

- определение: время от обновления overlay state (manual/pull/webhook) до применения на устройстве.
- цель: P95 ≤ 2 секунды.

### 2.5 Overlay adoption

- определение: доля активных экранов с включённым overlay.
- цель: ≥ 60% (как индикатор product value).

## 3) Бизнес-метрики (P1)

- конверсия пилот → production (в % и в абсолюте)
- NPS/CSAT операторов (простая анкета после 2 недель использования)
- support cost: инциденты на 100 экранов/неделя
- gross margin драйверы: egress/storage/db size

## 4) OKR (пример для R0/R1)

### R0 (Pilot) — Objective

Запустить пилоты на 50–200 экранов, доказать устойчивый показ и ценность overlay.

Key Results:
- KR1: time-to-publish P95 ≤ 10 минут на пилоте.
- KR2: playback uptime ≥ 99.9% на пилоте.
- KR3: overlay adoption ≥ 50% экранов пилота.
- KR4: ≥ 95% устройств online в рабочие часы.

### R1 (Production hardening) — Objective

Подготовить продукт к 1k+ экранов и enterprise требованиям.

Key Results:
- KR1: SSO/OIDC включён у ≥ 1 enterprise клиента.
- KR2: time-to-publish P95 ≤ 5 минут на 1k экранов.
- KR3: инциденты “black screen” ≤ X/месяц (X фиксируется после пилота).

## 5) План измерений (что логируем/считаем)

### 5.1 События (event taxonomy)

Admin:
- `schedule_published`
- `asset_uploaded`, `asset_ready`, `asset_rejected`
- `device_assigned`
- `overlay_state_updated` (manual/pull/webhook)

Player:
- `heartbeat` (срез состояния)
- `playback_started`, `playback_error`
- `schedule_version_applied`
- `overlay_state_applied`

### 5.2 Где измеряем

- backend: метрики + audit/as-run
- player: heartbeat/as-run + локальные ошибки

### 5.3 Дашборды (минимум)

- Fleet: online/offline, last seen, версии приложения
- Publish: latency p50/p95, errors
- Overlay: latency, reconnect rate, % активных bindings
- Playback: errors by device/model, fallback rate

