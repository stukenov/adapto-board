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
