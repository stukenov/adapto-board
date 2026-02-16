package com.playoutedge.server.views.landing

import kotlinx.html.*

data class TemplateCatalogItem(
    val code: String,
    val name: String,
    val category: String,
    val screenType: String,
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
        screenType = "16:9 TV",
        useCase = "TV, retail promos, branch announcements",
        description = "Бегущая строка для urgent-обновлений и промо без перепубликации канала.",
        previewHtml = """<div class=\"tp-ticker\"><div class=\"tp-ticker-label\">LIVE</div><div class=\"tp-ticker-track\"><span class=\"tp-ticker-text\">Adapto: pilot 50–200 Android TV экранов за 2–4 недели с KPI...</span></div></div>""",
        payloadHint = "{ \"text\": \"Ваше сообщение\", \"speed\": 40, \"theme\": \"dark\" }",
        integrations = listOf("Manual", "REST pull", "Webhook")
    ),
    TemplateCatalogItem(
        code = "kpi-tiles",
        name = "KPI Tiles",
        category = "data",
        screenType = "16:9 TV",
        useCase = "Corporate lobby, operations, NOC",
        description = "Показывает бизнес-метрики в брендированных карточках в realtime.",
        previewHtml = """<div class=\"tp-kpi-grid\"><div class=\"tp-kpi\"><span class=\"tp-kpi-val\">99.94%</span><span class=\"tp-kpi-lbl\">Uptime</span></div><div class=\"tp-kpi\"><span class=\"tp-kpi-val\">1.7s</span><span class=\"tp-kpi-lbl\">Latency p95</span></div><div class=\"tp-kpi\"><span class=\"tp-kpi-val\">842</span><span class=\"tp-kpi-lbl\">Online</span></div><div class=\"tp-kpi\"><span class=\"tp-kpi-val\">+18%</span><span class=\"tp-kpi-lbl\">Growth</span></div></div>""",
        payloadHint = "{ \"items\": [{\"label\":\"Uptime\",\"value\":\"99.9%\"}] }",
        integrations = listOf("Webhook", "REST pull")
    ),
    TemplateCatalogItem(
        code = "queue-table",
        name = "Queue / Service Table",
        category = "queue",
        screenType = "Queue Board",
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
        screenType = "16:9 TV",
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
        screenType = "16:9 TV",
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
        screenType = "16:9 TV",
        useCase = "До старта акции, ивента, эфира",
        description = "Таймер обратного отсчёта с акцентом на дедлайн.",
        previewHtml = """<div class=\"tp-countdown\"><span class=\"tp-cd-num\">01</span><span class=\"tp-cd-sep\">:</span><span class=\"tp-cd-num\">42</span><span class=\"tp-cd-sep\">:</span><span class=\"tp-cd-num\">08</span></div>""",
        payloadHint = "{ \"targetAt\": \"2026-03-01T10:00:00+05:00\" }",
        integrations = listOf("Manual", "Webhook")
    ),

    // New scenario-first templates
    TemplateCatalogItem(
        code = "roadside-portrait-offer",
        name = "Roadside Portrait Offer Stack",
        category = "portrait",
        screenType = "Portrait 9:16",
        useCase = "Уличные вертикальные экраны, ТРЦ фасады, АЗС",
        description = "Вертикальная компоновка для roadside: крупный offer, CTA и weather-триггер по городу.",
        previewHtml = """<div class=\"tp-portrait\"><div class=\"tp-portrait-top\">ALMATY • 22:40 • +3°C</div><div class=\"tp-portrait-main\">-35%<span>на зимние шины</span></div><div class=\"tp-portrait-cta\">Сканируйте QR и получите купон</div></div>""",
        payloadHint = "{ \"city\":\"Алматы\", \"offer\":\"-35%\", \"subtitle\":\"на зимние шины\" }",
        integrations = listOf("Webhook", "REST pull", "Manual")
    ),
    TemplateCatalogItem(
        code = "roadside-portrait-wayfinding",
        name = "Portrait Wayfinding + Promo",
        category = "portrait",
        screenType = "Portrait 9:16",
        useCase = "Бизнес-центр/ТРЦ навигация на вертикальном экране",
        description = "Сочетает wayfinding и промо-блоки, чтобы экран продавал и направлял одновременно.",
        previewHtml = """<div class=\"tp-portrait\"><div class=\"tp-wayfinding-title\">Навигация • Блок B</div><div class=\"tp-wayfinding-row\"><span>4 этаж</span><span>Kaspi/Support</span></div><div class=\"tp-wayfinding-row\"><span>6 этаж</span><span>ЦОН Service Point</span></div><div class=\"tp-portrait-cta\">Лифт справа →</div></div>""",
        payloadHint = "{ \"rows\": [{\"floor\":\"4\",\"name\":\"Kaspi Support\"}] }",
        integrations = listOf("Manual", "Webhook")
    ),
    TemplateCatalogItem(
        code = "led-wall-hero",
        name = "LED Video Wall Hero",
        category = "video-wall",
        screenType = "LED / Video Wall",
        useCase = "Сцены, конференции, retail atrium",
        description = "Широкий hero-слой для больших LED/video wall, адаптированный под 21:9 и сверхширокие полотна.",
        previewHtml = """<div class=\"tp-wall\"><div class=\"tp-wall-chip\">LIVE FORUM • ASTANA</div><div class=\"tp-wall-title\">Digital Signage Future 2026</div><div class=\"tp-wall-sub\">Поток: 8 420 зрителей • latency 1.6s</div></div>""",
        payloadHint = "{ \"title\":\"Digital Signage Future\", \"viewers\":8420 }",
        integrations = listOf("Webhook", "Manual")
    ),
    TemplateCatalogItem(
        code = "led-wall-sponsor-grid",
        name = "LED Sponsor + Program Grid",
        category = "video-wall",
        screenType = "LED / Video Wall",
        useCase = "События, стадионы, выставки",
        description = "Композиция для sponsor loop + ближайшие сессии/матчи на одном большом экране.",
        previewHtml = """<div class=\"tp-wall\"><div class=\"tp-wall-grid\"><span>Main Sponsor</span><span>Session 14:30</span><span>Partner A</span><span>Panel Hall C</span></div></div>""",
        payloadHint = "{ \"sponsors\":[\"Main\",\"Partner A\"], \"next\":\"14:30\" }",
        integrations = listOf("Webhook", "REST pull")
    ),
    TemplateCatalogItem(
        code = "queue-cson-compact",
        name = "Queue Board • ЦОН Compact",
        category = "queue",
        screenType = "Queue Board",
        useCase = "ЦОН/госуслуги",
        description = "Понятное табло с приоритетами и окнами обслуживания, читаемое с расстояния.",
        previewHtml = """<div class=\"tp-queue\"><div class=\"tp-queue-head\">ЦОН Алматы • Живая очередь</div><div class=\"tp-queue-row\"><b>A-204</b><span>Окно 7</span><em>Вызов</em></div><div class=\"tp-queue-row\"><b>B-015</b><span>Окно 2</span><em>Ожидание 4 мин</em></div></div>""",
        payloadHint = "{ \"ticket\":\"A-204\", \"desk\":\"7\", \"status\":\"Вызов\" }",
        integrations = listOf("Webhook", "REST pull")
    ),
    TemplateCatalogItem(
        code = "queue-bank-priority",
        name = "Queue Board • Bank Priority",
        category = "queue",
        screenType = "Queue Board",
        useCase = "Банк/автосервис/сервисные центры",
        description = "Секция приоритетных клиентов + общий поток, без визуального перегруза.",
        previewHtml = """<div class=\"tp-queue\"><div class=\"tp-queue-head\">Отделение Bank • Priority</div><div class=\"tp-queue-row\"><b>P-031</b><span>VIP Desk</span><em>Сейчас</em></div><div class=\"tp-queue-row\"><b>C-442</b><span>Desk 4</span><em>ETA 6 мин</em></div></div>""",
        payloadHint = "{ \"priority\":[...], \"common\":[...] }",
        integrations = listOf("Webhook", "Manual")
    ),
    TemplateCatalogItem(
        code = "ad-live-overlay-split",
        name = "Live Stream + Ad Overlay Split",
        category = "ad-overlay",
        screenType = "16:9 / DOOH",
        useCase = "Рекламный экран с текущей трансляцией",
        description = "Накладывает ad overlays поверх live-трансляции: баннер, CTA, short-code и ticker.",
        previewHtml = """<div class=\"tp-ad\"><div class=\"tp-ad-live\">LIVE MATCH</div><div class=\"tp-ad-banner\">Партнёр эфира: Qazaq Oil • -7% по QR</div></div>""",
        payloadHint = "{ \"stream\":\"live\", \"overlay\":{\"sponsor\":\"Qazaq Oil\"} }",
        integrations = listOf("Webhook", "REST pull")
    ),
    TemplateCatalogItem(
        code = "ad-live-lower-third",
        name = "Live Stream Ad Lower Third",
        category = "ad-overlay",
        screenType = "16:9 / DOOH",
        useCase = "DOOH и indoor-реклама",
        description = "Мягкий рекламный lower-third, который не перекрывает основной видеоконтент.",
        previewHtml = """<div class=\"tp-ad\"><div class=\"tp-ad-tag\">Реклама</div><div class=\"tp-ad-banner\">Скачайте приложение и получите бонус 5 000 ₸</div></div>""",
        payloadHint = "{ \"label\":\"Реклама\", \"text\":\"Скачайте приложение...\" }",
        integrations = listOf("Manual", "Webhook")
    ),
    TemplateCatalogItem(
        code = "hotel-lobby-concierge",
        name = "Hotel Lobby Concierge TV",
        category = "hotel",
        screenType = "Hotel TV",
        useCase = "Отель: лобби",
        description = "Welcome-экран для лобби: погода, check-in reminders, события и city highlights.",
        previewHtml = """<div class=\"tp-hotel\"><div class=\"tp-hotel-title\">Добро пожаловать в Altyn Hotel</div><div class=\"tp-hotel-row\"><span>Check-in</span><span>14:00</span></div><div class=\"tp-hotel-row\"><span>Завтрак</span><span>07:00–10:30</span></div></div>""",
        payloadHint = "{ \"hotel\":\"Altyn Hotel\", \"events\":[...] }",
        integrations = listOf("Manual", "REST pull", "Webhook")
    ),
    TemplateCatalogItem(
        code = "hotel-room-guide",
        name = "Hotel Room Info Channel",
        category = "hotel",
        screenType = "Hotel TV",
        useCase = "Отель: каналы в номерах",
        description = "Инфо-канал для гостей в номере: room-service, spa, shuttle, promo-пакеты.",
        previewHtml = """<div class=\"tp-hotel\"><div class=\"tp-hotel-title\">Room 1216 • Service Guide</div><div class=\"tp-hotel-row\"><span>Room service</span><span>24/7</span></div><div class=\"tp-hotel-row\"><span>Spa booking</span><span>Dial 303</span></div></div>""",
        payloadHint = "{ \"room\":\"1216\", \"actions\":[...] }",
        integrations = listOf("Manual", "Webhook")
    ),
    TemplateCatalogItem(
        code = "bizcenter-elevator-feed",
        name = "Business Center Elevator Feed",
        category = "business-center",
        screenType = "Lobby/Elevator",
        useCase = "БЦ: лифтовые экраны",
        description = "Короткие digest-блоки для ожидания лифта: новости, встречи, пробки/погода.",
        previewHtml = """<div class=\"tp-bc\"><div class=\"tp-bc-title\">Sunkar Towers • Elevator Feed</div><div class=\"tp-bc-row\"><span>09:30</span><span>Meeting: Floor 8, Hall B</span></div><div class=\"tp-bc-row\"><span>Traffic</span><span>Абая +12 мин</span></div></div>""",
        payloadHint = "{ \"building\":\"Sunkar\", \"items\":[...] }",
        integrations = listOf("Webhook", "REST pull")
    ),
    TemplateCatalogItem(
        code = "bizcenter-floor-brief",
        name = "Business Center Floor Brief",
        category = "business-center",
        screenType = "Floor Display",
        useCase = "БЦ: этажные и лобби-экраны",
        description = "Этажный бриф: компании арендаторы, сервисные уведомления, welcome-slot для гостей.",
        previewHtml = """<div class=\"tp-bc\"><div class=\"tp-bc-title\">Floor 12 • Tenant Brief</div><div class=\"tp-bc-row\"><span>Company</span><span>Adapto Kazakhstan</span></div><div class=\"tp-bc-row\"><span>Guest</span><span>Kaspi Team • 11:00</span></div></div>""",
        payloadHint = "{ \"floor\":12, \"tenant\":\"Adapto\", \"guest\":\"Kaspi Team\" }",
        integrations = listOf("Manual", "Webhook")
    )
)

fun HTML.templateCatalogView() {
    publicLayout("Template Catalog", currentPath = "/templates") {
        section("showcase-hero") {
            div("showcase-hero-content") {
                div("showcase-hero-badge") { +"Template Catalog" }
                h1 { +"Реальные шаблоны под разные форматы экранов" }
                p("showcase-hero-subtitle") {
                    +"От 16:9 до portrait, video wall, queue board, hotel TV и экранов бизнес-центров. Готовые демо-шаблоны за 1–2 дня перед пилотом."
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
                    p { +"Фильтруйте по типу сценария и открывайте detail-страницы с интеграциями и payload hint." }
                }
                div("template-categories") {
                    templateCatalogPill("all", "Все", true)
                    templateCatalogPill("text", "Text")
                    templateCatalogPill("data", "Data")
                    templateCatalogPill("queue", "Queue")
                    templateCatalogPill("portrait", "Portrait")
                    templateCatalogPill("video-wall", "Video Wall")
                    templateCatalogPill("ad-overlay", "Ad Overlay")
                    templateCatalogPill("hotel", "Hotel TV")
                    templateCatalogPill("business-center", "Business Center")
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
                            templatePreviewScene(template)
                        }
                    }
                    div("template-detail-meta") {
                        detailBlock("Сценарии") { p { +template.useCase } }
                        detailBlock("Тип экрана") { span("badge badge-neutral") { +template.screenType } }
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
                            p { +"Подготовим premium шаблоны под ваш брендбук и внедрим как контент-пакет даже без замены текущего подрядчика/платформы." }
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
        div("template-preview") { templatePreviewScene(item) }
        div("template-info") {
            div("template-meta") {
                span("badge badge-neutral") { +item.category }
                span("badge badge-neutral") { +item.screenType }
            }
            h4("template-name") { +item.name }
            p("template-desc") { +item.description }
            p("template-usecase") { +"Use case: ${item.useCase}" }
            p("template-code") { +item.code }
        }
    }
}

private fun String.toTemplateToken(): String =
    lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

private fun FlowContent.templatePreviewScene(item: TemplateCatalogItem) {
    val categoryToken = item.category.toTemplateToken()
    val screenToken = item.screenType.toTemplateToken()
    div("template-preview-inner tp-scene tp-cat-$categoryToken tp-screen-$screenToken") {
        div("tp-scene-glow")
        div("tp-scene-grid")
        div("tp-frame") {
            div("tp-frame-top") {
                span("tp-frame-dot")
                span("tp-frame-dot")
                span("tp-frame-dot")
                span("tp-frame-title") { +item.screenType }
            }
            div("tp-screen") {
                unsafe { +item.previewHtml }
            }
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