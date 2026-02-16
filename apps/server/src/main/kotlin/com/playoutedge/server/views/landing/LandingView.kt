package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.landingView() {
    publicLayout("Digital Signage Platform", currentPath = "/") {
        section("hero") {
            div("hero-content") {
                h1 { +"Android TV signage + realtime data-layer" }
                p("hero-subtitle") {
                    +"Публикация контента за минуты, устойчивый player (cache + fallback), и live overlays для KPI/очередей/уведомлений."
                }
                div("hero-actions") {
                    a(href = "/contact", classes = "btn btn-primary btn-lg") { +"Запросить демо" }
                    a(href = "/pricing", classes = "btn btn-ghost btn-lg") { +"Смотреть тарифы →" }
                }
            }
        }

        section("section") {
            div("section-inner") {
                div("section-header") {
                    h2 { +"Почему Adapto / Playout Edge" }
                    p { +"Пилот 2–4 недели на 50–200 экранов с измеримыми KPI: publish p95, uptime, online rate, overlay latency." }
                }
                div("feature-grid") {
                    featureCard(
                        "M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z",
                        "Стабильный playout 24/7",
                        "Android TV player с offline-first логикой, fallback и быстрым recovery в сложных сетях."
                    )
                    featureCard(
                        "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z",
                        "Overlay в realtime",
                        "SSE/webhook/manual mode для очередей, KPI, тикеров и брендированных шаблонов."
                    )
                    featureCard(
                        "M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z",
                        "Эксплуатация и контроль",
                        "Fleet статусы, audit, as-run и чистый контур внедрения: 1 клиент = 1 environment (v1)."
                    )
                }
            }
        }

        section("cta-section") {
            div("section-inner") {
                div("section-header") {
                    h2 { +"Нужны кастомные шаблоны под брендбук?" }
                    p { +"Сделаем premium шаблоны (showroom/service/welcome) и подключим их даже без смены текущей экранной платформы." }
                }
                a(href = "/templates", classes = "btn btn-primary btn-lg") { +"Открыть каталог шаблонов" }
            }
        }
    }
}

private fun FlowContent.featureCard(icon: String, title: String, description: String) {
    div("feature-card") {
        div("feature-icon") {
            unsafe {
                +"""<svg fill=\"none\" stroke=\"currentColor\" viewBox=\"0 0 24 24\" width=\"32\" height=\"32\"><path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" d=\"$icon\"></path></svg>"""
            }
        }
        h3 { +title }
        p { +description }
    }
}
