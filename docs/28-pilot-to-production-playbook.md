# 28 — Pilot → Production playbook (как делать внедрение управляемым)

Дата: 2026-02-01  
Роль: Sales + Delivery/CS + Product  
Цель: проводить пилоты так, чтобы они конвертировались в production и не сжигали команду.

## 1) Pilot как продукт

Пилот — это “упакованный проект”:
- фиксированный срок (2–4 недели),
- фиксированный скоуп (50–200 экранов),
- фиксированные KPI (go/no-go),
- заранее определённые роли и ответственность сторон.

Документы:
- Pilot passport: `docs/17-pilot-scorecard-and-acceptance.md`
- Pilot SOW template: `docs/20-collateral-templates.md`

## 2) Предстарт (обязательные проверки)

### 2.1 Техническая готовность клиента
- Сеть: DNS/443, прокси/allowlist (если есть).
- Модель обновлений Android TV app: MDM/managed Google Play/ручной.
- Список моделей Android TV (whitelist) и тест 2–3 устройств заранее.
- Источник данных для overlay (или manual mode).

### 2.2 Бизнес готовность
- Назначен champion (operator owner) и IT owner.
- Согласованы “сценарии must work” и метрики.
- Утверждён план контента на 2–4 недели.

## 3) Runbook пилота (недели)

Неделя 0 (0–3 дня):
- доступы, поднятие окружения, установка приложения, enroll/assign, базовые каналы.

Неделя 1:
- publish-процессы (оператор), baseline метрик, manual overlay.

Неделя 2:
- подключение 1 data-source (REST pull или webhook), сбор overlay latency/adoption.

Неделя 3–4 (если есть):
- стабилизация, устранение top-3 инцидентов, подготовка отчёта.

## 4) Отчёт пилота (что обязательно включить)

Структура: `docs/17-pilot-scorecard-and-acceptance.md`.
Минимум:
- метрики p50/p95 (publish, overlay latency), uptime, online rate,
- инциденты + причины + MTTR,
- список “production blockers” (если есть),
- план перехода в production (scope R1).

## 5) Переход в production: типовые блокеры и как закрывать

Частые блокеры:
- SSO/OIDC и политика ролей.
- Изолированный контур / on-prem требования.
- Процедуры поддержки и SLA.
- Экспорт audit/as-run для комплаенса.
- Ограничения контента (bitrate/codec) и реальные сети филиалов.

Пакет для security/procurement: `docs/29-procurement-security-compliance-pack.md`.

