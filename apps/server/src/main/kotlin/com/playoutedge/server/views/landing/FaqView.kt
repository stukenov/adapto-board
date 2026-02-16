package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.faqView() {
    publicLayout("FAQ", currentPath = "/faq") {
        section("section") {
            div("section-inner") {
                div("section-header") {
                    h1 { +"FAQ" }
                    p { +"Коротко про пилот, цены и внедрение." }
                }
                div("faq-list") {
                    faqItem("Какой минимальный формат старта?", "Self-serve POC: 750 000 ₸ на 2 недели (до 20 экранов, 1 локация).")
                    faqItem("Что входит в основной pilot?", "Pilot: 3 000 000 ₸, 2–4 недели, до 200 экранов и до 3 локаций, 1 data-source + manual mode.")
                    faqItem("Какие тарифы в production?", "Starter 4 000 ₸, Business 3 000 ₸, Enterprise 2 200 ₸ за экран в месяц. Annual prepaid дешевле — см. Pricing.")
                    faqItem("Есть ли free trial / self-serve SaaS тариф?", "Нет. Мы работаем через платный POC/Pilot и далее production-контракт с понятным scope.")
                    faqItem("Можно сделать кастомные шаблоны под брендбук?", "Да. Это обязательная часть нашего sales-подхода: premium шаблоны под клиента с быстрым демо за 1–2 дня.")
                    faqItem("Нужно ли менять текущего подрядчика/платформу?", "Не обязательно. Контент-пакет и шаблоны можно внедрять поверх текущего стека, как отдельную поставку.")
                    faqItem("Поддерживается on-prem / isolated?", "Да, для Enterprise и по отдельному проекту/SOW.")
                }
            }
        }
    }
}

private fun FlowContent.faqItem(question: String, answer: String) {
    details("faq-item") {
        summary { +question }
        p { +answer }
    }
}
