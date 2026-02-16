package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.pricingView() {
    publicLayout("Pricing", currentPath = "/pricing") {
        section("section") {
            div("section-inner") {
                div("section-header") {
                    h1 { +"Прозрачные тарифы для Казахстана (KZT)" }
                    p { +"Лист-прайс и правила соответствуют docs/24-kz-pricebook.md. Без хаотичных скидок." }
                }
                div("pricing-grid") {
                    pricingCard(
                        name = "Starter",
                        price = "4 000 ₸",
                        period = "/экран/мес",
                        description = "1–200 экранов. Annual prepaid: 3 000 ₸/экран. Минимальный чек 150 000 ₸/мес.",
                        features = listOf(
                            "Overlay manual mode",
                            "Audit + as-run (30 дней)",
                            "REST pull connector",
                            "Поддержка 8×5",
                            "Fair use: 10 GB/экран/мес"
                        ),
                        cta = "Запросить расчёт",
                        highlighted = false
                    )
                    pricingCard(
                        name = "Business",
                        price = "3 000 ₸",
                        period = "/экран/мес",
                        description = "201–1000 экранов. Annual prepaid: 2 200 ₸/экран. Минимальный чек 600 000 ₸/мес.",
                        features = listOf(
                            "Webhook overlay (R1)",
                            "Device groups",
                            "Audit + as-run (90 дней)",
                            "Расширенная поддержка 8×5",
                            "Fair use: 15 GB/экран/мес"
                        ),
                        cta = "Запустить pilot",
                        highlighted = true
                    )
                    pricingCard(
                        name = "Enterprise",
                        price = "2 200 ₸",
                        period = "/экран/мес",
                        description = "1000+ экранов. Annual prepaid: 1 800 ₸/экран. Минимальный чек 1 800 000 ₸/мес.",
                        features = listOf(
                            "SSO/OIDC (R1)",
                            "Audit + as-run (365 дней)",
                            "SLA и приоритетная поддержка",
                            "Isolated / on-prem опционально",
                            "Fair use: 20 GB/экран/мес"
                        ),
                        cta = "Обсудить enterprise",
                        highlighted = false
                    )
                }

                div("mt-5") {
                    div("card") {
                        div("card-body") {
                            h3 { +"Pilot / POC" }
                            p { +"Self-serve POC: 750 000 ₸ (до 20 экранов, 2 недели). Pilot: 3 000 000 ₸ (до 200 экранов, 2–4 недели)." }
                            p { +"Пилот может быть зачтён в первый годовой контракт (до 100%) при подписании в течение 30 дней после отчёта." }
                            a(href = "/contact", classes = "btn btn-primary") { +"Получить pilot passport" }
                        }
                    }
                }
            }
        }
    }
}

private fun FlowContent.pricingCard(
    name: String,
    price: String,
    period: String,
    description: String,
    features: List<String>,
    cta: String,
    highlighted: Boolean
) {
    div("pricing-card${if (highlighted) " pricing-card-highlighted" else ""}") {
        if (highlighted) {
            div("pricing-badge") { +"Most requested" }
        }
        h3("pricing-name") { +name }
        div("pricing-price") {
            span("pricing-amount") { +price }
            span("pricing-period") { +period }
        }
        p("pricing-description") { +description }
        ul("pricing-features") {
            features.forEach { feature ->
                li {
                    span("pricing-check") { +"✓" }
                    +feature
                }
            }
        }
        a(href = "/contact", classes = "btn ${if (highlighted) "btn-primary" else "btn-ghost"} btn-block") { +cta }
    }
}
