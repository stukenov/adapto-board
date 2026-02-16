package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.contactView() {
    publicLayout("Contact", currentPath = "/contact") {
        section("section") {
            div("section-inner") {
                div("section-header") {
                    h1 { +"Контакты" }
                    p { +"Соберём discovery, зафиксируем KPI пилота и дадим план внедрения." }
                }
                div("contact-grid") {
                    div("contact-card") {
                        h3 { +"Sales / Pilot" }
                        p { +"Discovery 20–30 минут: scope, сети, устройства, KPI и pilot passport." }
                        p { a(href = "mailto:sales@adapto.kz") { +"sales@adapto.kz" } }
                    }
                    div("contact-card") {
                        h3 { +"Template Studio" }
                        p { +"Кастомные шаблоны под брендбук (showroom/service/welcome), включая быстрый демо-пакет." }
                        p { a(href = "mailto:templates@adapto.kz") { +"templates@adapto.kz" } }
                    }
                    div("contact-card") {
                        h3 { +"Enterprise / Partners" }
                        p { +"Isolated/on-prem, интеграторы, крупные сети 1000+ экранов, SLA и security requirements." }
                        p { a(href = "mailto:enterprise@adapto.kz") { +"enterprise@adapto.kz" } }
                    }
                }
            }
        }
    }
}
