package com.playoutedge.server.views.landing

import kotlinx.html.*

data class TemplateCatalogItem(
    val code: String,
    val name: String,
    val category: String,
    val useCase: String,
    val description: String,
    val previewHtml: String,
    val payloadHint: String,
    val integrations: List<String>
)

val TEMPLATE_CATALOG = listOf(
    TemplateCatalogItem(
        code = "ticker",
        name = "Breaking / Promo Ticker",
        category = "text",
        useCase = "TV, retail promos, branch announcements",
        description = "Бегущая строка для urgent-обновлений и промо без перепубликации канала.",
        previewHtml = """<div class=\"tp-ticker\"><div class=\"tp-ticker-label\">LIVE</div><div class=\"tp-ticker-track\"><span class=\"tp-ticker-text\">Adapto: Pilot 50-200 Android TV screens in 2-4 weeks with measurable KPI...</span></div></div>""",
        payloadHint = "{ \"text\": \"Ваше сообщение\", \"speed\": 40, \"theme\": \"dark\" }",
        integrations = listOf("Manual", "REST pull", "Webhook")
    ),
    TemplateCatalogItem(
        code = "kpi-tiles",
        name = "KPI Tiles",
        category = "data",
        useCase = "Corporate lobby, operations, NOC",
        description = "Показывает бизнес-метрики в брендированных карточках в realtime.",
        previewHtml = """<div class=\"tp-kpi-grid\"><div class=\"tp-kpi\"><span class=\"tp-kpi-val\">99.94%</span><span class=\"tp-kpi-lbl\">Uptime</span></div><div class=\"tp-kpi\"><span class=\"tp-kpi-val\">1.7s</span><span class=\"tp-kpi-lbl\">Latency p95</span></div><div class=\"tp-kpi\"><span class=\"tp-kpi-val\">842</span><span class=\"tp-kpi-lbl\">Online</span></div><div class=\"tp-kpi\"><span class=\"tp-kpi-val\">+18%</span><span class=\"tp-kpi-lbl\">Growth</span></div></div>""",
        payloadHint = "{ \"items\": [{\"label\":\"Uptime\",\"value\":\"99.9%\"}] }",
        integrations = listOf("Webhook", "REST pull")
    ),
    TemplateCatalogItem(
        code = "queue-table",
        name = "Queue / Service Table",
        category = "data",
        useCase = "Банки, сервис-центры, госсектор",
        description = "Табло очереди с текущим номером, статусом и временем ожидания.",
        previewHtml = """<div class=\"tp-table\"><div class=\"tp-table-hdr\"><span>Ticket</span><span>Desk</span><span>ETA</span></div><div class=\"tp-table-row\"><span>A-117</span><span>3</span><span>2m</span></div><div class=\"tp-table-row\"><span>A-118</span><span>5</span><span>5m</span></div><div class=\"tp-table-row\"><span>A-119</span><span>2</span><span>8m</span></div></div>""",
        payloadHint = "{ \"rows\": [{\"ticket\":\"A-117\",\"desk\":\"3\",\"eta\":\"2m\"}] }",
        integrations = listOf("Webhook", "Manual")
    ),
    TemplateCatalogItem(
        code = "lower-third",
        name = "Lower Third",
        category = "info",
        useCase = "События, интервью, корпоративные эфиры",
        description = "Имя/должность/сообщение с аккуратной анимацией в фирменном стиле.",
        previewHtml = """<div class=\"tp-lower-third\"><div class=\"tp-lt-name\">Ainur S.</div><div class=\"tp-lt-title\">COO, Adapto Kazakhstan</div></div>""",
        payloadHint = "{ \"name\": \"Имя\", \"title\": \"Должность\" }",
        integrations = listOf("Manual", "Webhook")
    ),
    TemplateCatalogItem(
        code = "qr-card",
        name = "QR Lead Card",
        category = "interactive",
        useCase = "Мероприятия, офферы, запись на демо",
        description = "CTA-карточка с QR для мгновенного перехода в форму/чат/приложение.",
        previewHtml = """<div class=\"tp-qr\"><div class=\"tp-qr-grid\"><div class=\"tp-qr-c\"></div><div class=\"tp-qr-c\"></div><div class=\"tp-qr-c\"></div><div class=\"tp-qr-c\"></div><div class=\"tp-qr-c tp-qr-w\"></div><div class=\"tp-qr-c\"></div><div class=\"tp-qr-c\"></div><div class=\"tp-qr-c\"></div><div class=\"tp-qr-c\"></div></div><div class=\"tp-qr-txt\">Scan to request pilot</div></div>""",
        payloadHint = "{ \"url\": \"https://...\", \"cta\": \"Scan me\" }",
        integrations = listOf("Manual", "Webhook")
    ),
    TemplateCatalogItem(
        code = "countdown",
        name = "Countdown",
        category = "time",
        useCase = "До старта акции, ивента, эфира",
        description = "Таймер обратного отсчёта с акцентом на дедлайн.",
        previewHtml = """<div class=\"tp-countdown\"><span class=\"tp-cd-num\">01</span><span class=\"tp-cd-sep\">:</span><span class=\"tp-cd-num\">42</span><span class=\"tp-cd-sep\">:</span><span class=\"tp-cd-num\">08</span></div>""",
        payloadHint = "{ \"targetAt\": \"2026-03-01T10:00:00+05:00\" }",
        integrations = listOf("Manual", "Webhook")
    )
)

fun HTML.templateCatalogView() {
    publicLayout("Template Catalog", currentPath = "/templates") {
        section("showcase-hero") {
            div("showcase-hero-content") {
                div("showcase-hero-badge") { +"Template Catalog" }
                h1 { +"Выберите шаблон под ваш сценарий" }
                p("showcase-hero-subtitle") {
                    +"Готовые шаблоны для Android TV signage: очереди, KPI, промо, ивенты. Быстрый старт с демо-шаблонов за 1–2 дня."
                }
                div("showcase-hero-actions") {
                    a(href = "/contact", classes = "btn btn-primary btn-lg") { +"Запросить демонстрацию шаблонов" }
                    a(href = "/showcase", classes = "btn btn-ghost btn-lg") { +"Смотреть live showcase" }
                }
            }
        }

        section("section showcase-section showcase-templates-section") {
            div("section-inner") {
                div("section-header") {
                    h2 { +"Каталог шаблонов" }
                    p { +"Фильтруйте по типу и открывайте карточку шаблона с деталями интеграции и payload." }
                }
                div("template-categories") {
                    templateCatalogPill("all", "Все", true)
                    templateCatalogPill("text", "Text")
                    templateCatalogPill("data", "Data")
                    templateCatalogPill("info", "Info")
                    templateCatalogPill("interactive", "Interactive")
                    templateCatalogPill("time", "Time")
                }
                div("templates-grid") {
                    id = "templates-catalog-grid"
                    TEMPLATE_CATALOG.forEach { item ->
                        templateCatalogCard(item)
                    }
                }
            }
        }

        script { unsafe { +templateCatalogScript() } }
    }
}

fun HTML.templateDetailView(templateCode: String) {
    val template = TEMPLATE_CATALOG.find { it.code == templateCode }
    if (template == null) {
        publicLayout("Template Not Found", currentPath = "/templates") {
            section("section") {
                div("section-inner") {
                    h1 { +"Шаблон не найден" }
                    p { +"Проверьте ссылку или вернитесь в каталог шаблонов." }
                    a(href = "/templates", classes = "btn btn-primary") { +"К каталогу" }
                }
            }
        }
        return
    }

    publicLayout("Template ${template.name}", currentPath = "/templates") {
        section("section") {
            div("section-inner template-detail-page") {
                a(href = "/templates", classes = "back-link") { +"← Назад к каталогу" }
                div("template-detail-grid") {
                    div("template-detail-preview") {
                        h1 { +template.name }
                        p("template-detail-subtitle") { +template.description }
                        div("template-detail-canvas") {
                            unsafe { +template.previewHtml }
                        }
                    }
                    div("template-detail-meta") {
                        detailBlock("Сценарии") { p { +template.useCase } }
                        detailBlock("Категория") { span("badge badge-neutral") { +template.category } }
                        detailBlock("Интеграции") {
                            div("template-integrations") {
                                template.integrations.forEach { tag -> span("badge badge-neutral") { +tag } }
                            }
                        }
                        detailBlock("Payload hint") {
                            div("demo-code-block") { code { +template.payloadHint } }
                        }
                        detailBlock("CTA") {
                            p { +"Подготовим кастом под брендбук и внедрим даже без смены вашего текущего подрядчика/платформы." }
                            a(href = "/contact", classes = "btn btn-primary btn-block") { +"Обсудить кастомизацию" }
                        }
                    }
                }
            }
        }
    }
}

private fun FlowContent.templateCatalogPill(category: String, label: String, active: Boolean = false) {
    button(classes = "template-pill${if (active) " active" else ""}") {
        attributes["data-category"] = category
        attributes["onclick"] = "filterTemplateCatalog('$category')"
        +label
    }
}

private fun FlowContent.templateCatalogCard(item: TemplateCatalogItem) {
    a(href = "/templates/${item.code}", classes = "template-card template-catalog-card") {
        attributes["data-category"] = item.category
        div("template-preview") { div("template-preview-inner") { unsafe { +item.previewHtml } } }
        div("template-info") {
            div("template-meta") {
                span("badge badge-neutral") { +item.category }
                span("template-code") { +item.code }
            }
            h4("template-name") { +item.name }
            p("template-desc") { +item.description }
            p("template-usecase") { +"Use case: ${item.useCase}" }
        }
    }
}

private fun FlowContent.detailBlock(title: String, content: FlowContent.() -> Unit) {
    div("template-detail-block") {
        h3 { +title }
        content()
    }
}

private fun templateCatalogScript(): String = """
function filterTemplateCatalog(category) {
    document.querySelectorAll('.template-pill').forEach(function(p) {
        p.classList.toggle('active', p.dataset.category === category);
    });
    document.querySelectorAll('.template-catalog-card').forEach(function(c) {
        c.style.display = (category === 'all' || c.dataset.category === category) ? '' : 'none';
    });
}
"""