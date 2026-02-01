# 27 — Pricing architecture & discounting (как монетизировать безопасно)

Дата: 2026-02-01  
Роль: CEO / Sales / Finance  
Цель: иметь pricing, который масштабируется, защищает маржу (egress/support) и легко продаётся.

## 1) Принципы

1) **Unit = screen/month** (продаётся, понимается, масштабируется).
2) Любой pricing должен включать **лимиты** (storage/egress/ретеншн/коннекторы), иначе маржа “утечёт”.
3) Ранний cashflow часто делает **пилот/внедрение**, а не MRR.

## 2) Архитектура цены (рекомендуемая)

### 2.1 Подписка (MRR)

База:
- `Price_per_screen_month` × `#screens`

Tiers:
- Starter (до 200)
- Business (до 1000)
- Enterprise (1000+)

### 2.2 Разовые (setup / pilot / onboarding)

- Платный пилот по умолчанию (2–4 недели).
- Setup fee (особенно если есть on-prem/isolated environment/SSO).

### 2.3 Аддоны

- SSO/OIDC (если не входит в tier).
- Расширенный ретеншн audit/as-run.
- Дополнительные data connectors (сверх 1–2 включённых).
- Isolated environment / on-prem.
- Premium support / on-call.

## 3) Лимиты (чтобы не “убило” egress)

Обязательные лимиты в коммерческом предложении:
- `Storage_GB_included` (assets + logs)
- `Egress_GB_included` или “fair use” + оверейдж
- `Retention_days` (audit/as-run)
- `Max_bitrate / allowed codecs` (особенно для пилота)
- `Max_SSE_connections` (как технический guardrail, если надо)

Модель расчёта egress и COGS: `docs/23-unit-economics.md`.

## 4) Discount policy (anti-chaos)

Цель: скидки управляемые, предсказуемые, не ломают маржу.

Рекомендуемые уровни:
- до 10%: Head of Sales
- 10–20%: CEO
- >20%: CEO + Finance (и обязателен trade-off: предоплата/срок/объём/ограничения)

Правила:
- скидки только за **коммит** (год, объём экранов, предоплата, reference case).
- скидки запрещены, если клиент не принял лимиты egress/storage/retention.

## 5) Бенчмарки рынка (зачем нужны)

Бенчмарки нужны, чтобы:
- понимать “психологический коридор” per-screen pricing,
- видеть, как конкуренты упаковывают лимиты и enterprise,
- аргументировать цену через ценность (ops + data-layer), а не через “шаблоны”.

Снимок по публичным прайсам (USD, 2026-02-01) показывает коридор примерно **$8–$44 / screen / month** в signage SaaS (в зависимости от tier), а enterprise чаще всего монетизируется через:
- SSO / audit logs / approval workflows,
- premium support / SLA,
- лимиты storage/bandwidth/retention.

Источники и ссылки: `docs/25-market-research-and-sizing.md`.

## 6) Pricing experiments (когда не ясно “сколько брать”)

План экспериментов:
1) 3–5 сделок: фиксируем willingness-to-pay по tier и по аддонам (особенно SSO + isolated).
2) В пилоте собираем `U` и support load, корректируем лимиты.
3) Каждые 4–6 недель пересматриваем “Pilot fee” и conversion в production.
