# Playout Edge — детальный план (Kotlin-only + Android TV)

Источник продуктового PRD: `about.txt` (в корне репозитория). Этот каталог (`docs/`) расширяет PRD до полного плана: бизнес, требования, архитектура, данные, API, приложения, эксплуатация и роадмап.

## Как читать

1. `docs/01-requirements-and-assumptions.md` — требования, ограничения (Kotlin-only, Android TV-only), допущения.
2. `docs/02-target-architecture.md` — целевая архитектура “минимум систем”, состав модулей, ключевые потоки.
3. `docs/03-data-model.md` — модель данных Postgres (таблицы/индексы/инварианты).
4. `docs/04-api-contracts.md` — контракты API (Control Plane, Player, Overlay SSE).
5. `docs/05-android-tv-player.md` — архитектура Android TV приложения (Kotlin), кэш, воспроизведение, оверлеи.
6. `docs/06-ops-security-sre.md` — деплой, SLO, мониторинг, безопасность, процедуры.
7. `docs/07-roadmap-backlog.md` — релизы, эпики, user stories, критерии приёмки, риски/стоимость.
8. `docs/DECISIONS.md` — журнал ключевых решений и компромиссов.
9. `docs/08-business-gtm-and-pilot.md` — упаковка, GTM и план пилота.
10. `docs/09-product-vision-and-positioning.md` — vision, позиционирование, целевой рынок, “порог” возможностей.
11. `docs/10-icp-personas-and-jtbd.md` — ICP, персоны, JTBD, ключевые сценарии/болезни.
12. `docs/11-success-metrics-okrs-and-telemetry.md` — метрики успеха, OKR, что и как измеряем.
13. `docs/12-pricing-and-packaging.md` — цены/пакеты/лимиты, как считать себестоимость.
14. `docs/13-competitive-landscape.md` — конкуренты, матрица возможностей, дифференциация.
15. `docs/14-discovery-and-research-log.md` — план discovery + реестр гипотез/интервью/экспериментов.
16. `docs/15-mvp-backlog-r0.md` — детальный MVP backlog (эпики → stories → AC, “без расползания”).
17. `docs/16-release-notes-rollout-and-comms.md` — релизные артефакты: rollout, совместимость, коммуникации.
18. `docs/17-pilot-scorecard-and-acceptance.md` — “паспорт пилота”: критерии успеха, чек-листы приёмки, отчёт.
19. `docs/18-marketing-kit.md` — позиционирование для рынка: месседжи, сайт, контент, GTM.
20. `docs/19-sales-playbook.md` — playbook продаж: квалификация, discovery, демо, пилот, возражения, конкуренты.
21. `docs/20-collateral-templates.md` — готовые шаблоны (one-pager, письма, deck, SOW пилота, кейс-стади).
22. `docs/21-software-architecture.md` — архитектура ПО: модули, границы, кодовая структура, runtime-потоки, тестирование.
23. `docs/22-lean-shortcuts.md` — обходные пути для скорости, меньшей команды, меньшей себестоимости и более дешёвого CAC.
24. `docs/23-unit-economics.md` — юнит-экономика: COGS (трафик/инфра/хранилище), OPEX (команда/CAC), сценарии и пороги окупаемости.
25. `docs/24-kz-pricebook.md` — прайс-лист KZ: тарифы, минимальные чеки, скидки, оверейджи, правила торговли.
26. `docs/25-market-research-and-sizing.md` — market research: размер рынка, сегментация, конкурентные прайсы, procurement ожидания.
27. `docs/26-gtm-operating-system.md` — GTM “операционка”: воронка, определения стадий, метрики, cadence, CRM-поля.
28. `docs/27-pricing-architecture-and-discounting.md` — архитектура цены, лимиты, скидки, эксперименты, бенчмарки.
29. `docs/28-pilot-to-production-playbook.md` — playbook “пилот → production”: этапы, чек-листы, типовые блокеры.
30. `docs/29-procurement-security-compliance-pack.md` — пакет для procurement/security: список артефактов и ответник.
31. `docs/30-customer-success-and-support.md` — Customer Success & Support: onboarding, health score, поддержка, expansion.
32. `docs/31-partner-program.md` — партнёрская программа: типы партнёров, модель, enablement, lead registration.
33. `docs/32-roi-and-business-case.md` — ROI/business-case: шаблон расчёта и нарратив “продать внутри”.
34. `docs/33-competitive-battlecards.md` — battlecards: как выигрывать против SaaS/DIY/enterprise AV и red flags.
35. `docs/34-commercial-terms-and-sla-outline.md` — коммерческие условия и SLA: структура и ключевые пункты (скелет).

## Принцип “меньше систем”

Базовая цель v1: один Kotlin-сервис (монолит) + один DB (PostgreSQL) + файловое хранилище (локально) для пилота. Всё остальное — опционально и добавляется только при подтверждённой необходимости (трафик, масштаб, корпоративные требования).
