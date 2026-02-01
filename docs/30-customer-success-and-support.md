# 30 — Customer Success & Support (удержание и расширение)

Дата: 2026-02-01  
Роль: Head of CS / Support / Delivery  
Цель: после продажи сделать так, чтобы продукт приносил измеримую ценность и расширялся.

## 1) Определение успеха клиента

Успех = клиент достигает согласованных KPI:
- publish latency (P95),
- uptime,
- online rate,
- overlay adoption/latency (если релевантно),
и может обслуживать парк без “ручного героизма”.

## 2) Onboarding (0–30 дней)

### Неделя 0 (kickoff)
- RACI (клиент: IT owner + operator owner; мы: delivery owner).
- подтверждение scope и KPI (pilot passport / success plan).
- сетевые требования и план установки.

### Неделя 1
- обучение operator: каналы/расписания/publish.
- установка baseline мониторинга (fleet + publish).

### Неделя 2
- overlay: manual + (опционально) 1 data source.
- первые “операционные runbooks”: что делать при offline/ошибках.

### Неделя 3–4
- стабилизация и закрепление процессов (контент-план, релиз-окна, роли).

## 3) Health score (простая модель)

P0 сигналы:
- % online screens
- % screens с успешным playback (as-run/heartbeat)
- publish failures rate
- support tickets / 100 screens
- overlay adoption (если продаём “data-layer”)

## 4) Поддержка (support tiers)

Минимум:
- Канал поддержки (email/чат) + время реакции по уровням (P0/P1/P2).
- Эскалация: кто on-call на релиз/инцидент.
- Postmortem для P0.

Аддоны:
- 24/7 on-call
- выделенный CSM
- регулярные QBR

## 4.1 Что должно быть в продукте для дешёвой поддержки (must)

Чтобы support/CS не превращались в ручную переписку, в продукте фиксируем минимальный “support-grade” функционал:
- Ops dashboards (fleet/publish/overlay health) и device detail (last seen, version, last errors).
- “Copy support bundle” одной кнопкой (deviceId, tenantId, версии, таймстемпы, последние ошибки).
- Publish tracker (X/Y applied + причины отставания).
- Alerts list (online rate / publish failures / connector failures) с “likely causes” и next actions.
- Maintenance mode (опционально) для плановых работ с audit и TTL.

Техническая реализация и rollout per company: `docs/44-company-ops-and-per-company-rollout.md`.

## 5) Expansion playbook

Триггеры расширения:
- добавление экранов,
- подключение новых источников данных,
- увеличение ретеншна и комплаенс требований,
- изолированный контур/SSO.

Процесс:
- success review → предложение → SOW/заказ → rollout план.
