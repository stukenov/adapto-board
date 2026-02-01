# 06 — Ops / Security / SRE (минимум процедур, максимум предсказуемости)

## 1) Деплой-модель (pilot)

Один `docker-compose`:

- `app` (Ktor монолит)
- `postgres` (или managed Postgres)
- `storage`:
  - pilot: volume на диске (NFS/локальный SSD)
  - production: S3-compatible

Принцип: “одна команда, чтобы поднять пилот”.

### 1.1 Деплой “по компаниям” (GTM режим)

По умолчанию до PMF: `docs/DECISIONS.md` D010 — **1 компания = 1 контур**. Практически это означает:
- отдельный домен/поддомен на компанию,
- отдельная БД (минимум — отдельный database+role),
- отдельный storage root/bucket,
- отдельные секреты и лимиты.

Это даёт дешёвый и безопасный `rollout per company`: деплой обновления в конкретный контур без риска затронуть других клиентов.

Варианты размещения (от дешёвого к более изолированному):
1) несколько контуров на одном VM (несколько compose‑стеков + reverse proxy) — дешево, подходит для первых клиентов;
2) 1 VM на компанию — дороже, но проще продать enterprise как “выделенный контур”;
3) Kubernetes/namespace на компанию — только когда команда/нагрузка оправдывают (на старте избегаем).

## 2) Конфигурация

Все конфиги через env vars + secret store (на уровне платформы):

- `DATABASE_URL`
- `JWT_SECRET` (pilot) / `OIDC_*` (R1)
- `STORAGE_MODE=LOCAL|S3`
- `ASSET_SIGNING_SECRET`
- `TENANT_DEFAULT_TIMEZONE`
- квоты и лимиты (макс размер файла, макс устройств, max SSE connections)

## 3) Observability

### 3.1 Метрики (must)

- API latency (p50/p95/p99), error rates
- число online devices, heartbeat lag
- SSE connections count, reconnect rate
- ingest: upload/processing failures
- storage: выдача URL, download errors (если проксируем)
- DB: pool saturation, slow queries

### 3.2 Логи

- JSON логи с `tenant_id`, `request_id`, `device_id` (если есть).
- Уровни: INFO/WARN/ERROR, без PII в логах.

### 3.3 Алерты (pilot)

- “DB down”
- “app down”
- “online devices < X%” (на тенант)
- “publish failures” / “overlay connector failures”

## 4) Безопасность (v1 → R1)

### 4.1 Минимум v1

- RBAC в тенанте.
- Device auth:
  - enroll code одноразовый, TTL (например 10–30 минут)
  - device refresh token хранится безопасно (Android Keystore при возможности)
  - возможность revoke устройства в админке
- Signed URLs на assets (TTL).
- Audit log обязателен.

### 4.2 R1 (enterprise)

- OIDC/SSO.
- Политики паролей/2FA (если без SSO).
- IP allowlist (если требуется).
- Отдельные “integration keys” для webhook/pull источников.

## 5) Data protection / compliance

- Шифрование:
  - TLS везде
  - шифрование дисков (на уровне инфраструктуры)
- PII:
  - минимизировать (email/имя в users)
- Multi-tenant:
  - обязательные фильтры по tenant_id в каждом запросе
  - audit доступов админов

## 6) Резервное копирование и восстановление

### 6.1 Postgres

- ежедневные бэкапы + point-in-time (если managed)
- тест восстановления раз в квартал

### 6.2 Assets storage

- pilot: snapshot volume
- production: versioning/bucket lifecycle (если S3)

## 7) Release process (без “ночных сюрпризов”)

- Версионирование API (v1).
- Backward compatibility для Player API минимум на 2 версии.
- “Read-only ops mode” (флаг), если нужно заморозить изменения в инцидент.
