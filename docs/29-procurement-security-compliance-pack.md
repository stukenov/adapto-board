# 29 — Procurement / Security / Compliance pack (для enterprise)

Дата: 2026-02-01  
Роль: Sales Engineer / Security / IT Owner  
Цель: сократить время security review и закрывать типовые анкеты без “каждый раз заново”.

## 1) Что входит в “пакет” (список документов)

Минимум (P0):
- Архитектура и компоненты: `docs/02-target-architecture.md`
- Модель данных и ретеншн: `docs/03-data-model.md`
- Ops/SRE и процессы релизов: `docs/06-ops-security-sre.md`, `docs/16-release-notes-rollout-and-comms.md`
- Аутентификация/авторизация и device auth: `docs/04-api-contracts.md`
- Пилотные KPI и acceptance: `docs/17-pilot-scorecard-and-acceptance.md`

Enterprise дополнительно (P1, как roadmap):
- SSO/OIDC, расширенный RBAC
- Экспорт audit/as-run (CSV), расширенный ретеншн
- Опционально: RLS в Postgres, pentest, SOC2/ISO планы

## 2) Быстрые ответы (response library)

### 2.1 Данные и приватность
- Какие PII? Минимально: email/имя пользователей (admin). Данные overlay зависят от клиента; рекомендуем не включать PII.
- Data retention: audit/as-run — настраиваемо, есть политики удаления.

### 2.2 Доступ и идентичность
- RBAC на уровне tenant.
- Device auth: одноразовый enroll code + device refresh token + short-lived device JWT; возможность revoke устройства.
- SSO/OIDC — production hardening (при необходимости клиента).

### 2.3 Шифрование
- In-transit: TLS.
- At-rest: зависит от окружения (шифрование дисков/объектного хранилища на уровне инфраструктуры).

### 2.4 Логи и аудит
- Audit лог изменений (кто/что/когда).
- As-run (что реально показывалось).
- Structured logs без PII (по политике).

### 2.5 Бэкапы и восстановление
- Postgres: ежедневные бэкапы + тест восстановления по расписанию.
- Assets: snapshot/версии (зависит от storage).

### 2.6 Инциденты
- Канал поддержки, критерии P0/P1, коммуникации, postmortem (см. `docs/16-release-notes-rollout-and-comms.md`).

## 3) Network requirements (для IT)

Минимально:
- outbound с устройств к backend (HTTPS 443)
- доступ к storage/CDN (если используется) по HTTPS 443

Рекомендуется:
- allowlist доменов backend/storage
- стабильный DNS

## 4) Deployment options (для procurement)

Бизнес-упаковка (практичная для early stage):
- **Isolated environment** (1 клиент = 1 контур) — снижает риски и ускоряет согласования.
- On-prem / customer cloud — по отдельному SOW и цене.

## 5) Security questionnaire template (скелет)

Используйте как основу для ответов клиенту:
- Identity & access management (SSO/RBAC/MFA)
- Data protection (TLS, encryption at rest, secrets)
- Application security (SDLC, dependencies, vuln management)
- Operations (monitoring, backups, DR)
- Incident response (SLA, postmortems)
- Compliance (если требуется)

Примечание: это не юридический документ; финальные ответы согласуются с тем, как именно развёрнут контур клиента.

