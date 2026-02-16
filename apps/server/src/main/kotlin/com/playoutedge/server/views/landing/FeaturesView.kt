package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.featuresView() {
    publicLayout("Features", currentPath = "/features") {
        section("section") {
            div("section-inner") {
                div("section-header") {
                    h1 { +"Функции платформы" }
                    p { +"Собрано под B2B-сценарии: ТВ, банки, ритейл, госсектор, корпоративные экраны." }
                }

                div("feature-grid feature-grid-full") {
                    featureDetailCard("Каналы и расписания", "Draft/publish workflow, быстрые обновления и контроль версий расписаний.")
                    featureDetailCard("Overlay data-layer", "Manual + webhook + REST pull для тикеров, KPI, очередей и branded widgets.")
                    featureDetailCard("Template catalog", "Готовые шаблоны + отдельные detail-страницы с payload примерами и CTA.")
                    featureDetailCard("Device fleet", "Enroll, online/offline мониторинг, группы устройств, базовая диагностика.")
                    featureDetailCard("Audit + As-Run", "История изменений и отчёты по факту показа для комплаенса и операций.")
                    featureDetailCard("Безопасность", "RBAC, device auth, signed URLs; SSO/OIDC в Enterprise (R1).")
                }

                div("features-live-stack") {
                    div("features-live-item") {
                        div("features-live-header") {
                            h3 { +"Live channel preview" }
                            span("badge badge-neutral") { +"/embed/{channelId}" }
                        }
                        div("features-live-screen") {
                            div("features-screen-top") {
                                span("features-live-dot") {}
                                span { +"News 24 • эфир активен" }
                                span("features-screen-muted") { +"14:32" }
                            }
                            div("features-lower-third") {
                                strong { +"Ainur S." }
                                span { +"COO, Adapto Kazakhstan" }
                            }
                            div("features-ticker") {
                                span("features-ticker-tag") { +"UPDATE" }
                                span { +"Overlay обновлён через webhook без перезапуска канала" }
                            }
                        }
                    }

                    div("features-live-item") {
                        div("features-live-header") {
                            h3 { +"Overlay control snapshot" }
                            span("badge badge-neutral") { +"ops panel" }
                        }
                        div("features-control-grid") {
                            featureToggle("Ticker", "ON", true)
                            featureToggle("Queue table", "ON", true)
                            featureToggle("Emergency banner", "STANDBY", false)
                            featureToggle("Weather", "AUTO", true)
                        }
                        div("features-payload-line") {
                            code { +"{ \"ticket\":\"A-204\", \"window\":\"7\", \"status\":\"Вызов\" }" }
                        }
                    }
                }
            }
        }
    }
}

private fun FlowContent.featureDetailCard(title: String, description: String) {
    div("feature-card") {
        h3 { +title }
        p { +description }
    }
}

private fun FlowContent.featureToggle(name: String, state: String, on: Boolean) {
    div("features-toggle ${if (on) "on" else "off"}") {
        span { +name }
        b { +state }
    }
}
