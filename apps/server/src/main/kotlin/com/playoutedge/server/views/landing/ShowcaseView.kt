package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.showcaseView() {
    publicLayout("Product Showcase", currentPath = "/showcase") {
        section("showcase-hero") {
            div("showcase-hero-widgets") {
                unsafe {
                    +"""
                    <div class="hero-float-widget hfw-1" style="top:17%;left:7%"><div class="hero-chip">Окна обслуживания: 12 online / 1 paused</div></div>
                    <div class="hero-float-widget hfw-2" style="top:29%;right:9%"><div class="hero-chip">Overlay latency p95: 1.8s</div></div>
                    <div class="hero-float-widget hfw-3" style="bottom:18%;left:10%"><div class="hero-chip">Центральный офис • 846 устройств online</div></div>
                    <div class="hero-float-widget hfw-4" style="bottom:28%;right:7%"><div class="hero-chip">Retail + Queue + Corporate сценарии</div></div>
                    """
                }
            }
            div("showcase-hero-content") {
                div("showcase-hero-badge") { +"Product Showcase" }
                h1 { +"See Adapto Board in Real Scenarios" }
                p("showcase-hero-subtitle") {
                    +"Реалистичные демо-каналы под 16:9, portrait, video wall, queue board, hotel TV и экраны бизнес-центров — как в рабочей эксплуатации."
                }
                div("showcase-hero-stats") {
                    div("hero-stat") {
                        span("hero-stat-num") { +"12" }
                        span("hero-stat-label") { +"Demo Channels" }
                    }
                    div("hero-stat") {
                        span("hero-stat-num") { +"18" }
                        span("hero-stat-label") { +"Template Scenarios" }
                    }
                    div("hero-stat") {
                        span("hero-stat-num") { +"24/7" }
                        span("hero-stat-label") { +"Live Overlays" }
                    }
                }
                div("showcase-hero-actions") {
                    a(href = "#demo-channels", classes = "btn btn-primary btn-lg") { +"View Demo Channels" }
                    a(href = "/templates", classes = "btn btn-ghost btn-lg") { +"Browse Templates" }
                }
            }
        }

        section("section showcase-section") {
            id = "demo-channels"
            div("section-inner") {
                div("section-header") {
                    h2 { +"Live Demo Channels" }
                    p { +"Каждый канал открывается сразу с контентом и активными overlay-сценариями без пустых экранов." }
                }
                div("demo-channels-grid") {
                    demoChannelCard("news-24", "News 24", "Эфир новостей: ticker, погодный блок, время и служебные алерты", "News & Media", listOf("Ticker", "Weather", "Clock", "Breaking"), "#dc2626")
                    demoChannelCard("sports-live", "Sports Live", "Спортивный эфир: табло матча, тайминг, статистика владения", "Sports", listOf("Score", "Reactions", "Stats", "Ticker"), "#059669")
                    demoChannelCard("corporate-lobby", "Corporate Lobby", "Лобби-композиция: KPI, welcome-блок, встречи и сервисные заметки", "Corporate", listOf("KPI Tiles", "QR Card", "Lower Third", "Clock"), "#2563eb")
                    demoChannelCard("retail-signage", "Retail Signage", "Retail-показы: акция, остатки по SKU, CTA-блок и promo ticker", "Retail", listOf("Poll", "Countdown", "Ticker", "QR"), "#d97706")
                    demoChannelCard("event-stream", "Event Stream", "Сценарий конференции: карточка спикера, таймер сессии, навигация", "Events", listOf("Lower Third", "Q&A", "Countdown", "Ticker"), "#7c3aed")
                    demoChannelCard("education-tv", "Education TV", "Кампус-экран: расписание, объявления и служебные сообщения", "Education", listOf("Schedule", "News", "Clock", "Logo"), "#0891b2")
                    demoChannelCard("roadside-portrait", "Roadside Portrait", "Вертикальный DOOH: offer stack, погодный триггер и QR-купон", "Portrait DOOH", listOf("Vertical Promo", "Weather", "QR"), "#ea580c")
                    demoChannelCard("led-wall-prime", "LED Wall Prime", "Сверхширокий wall: hero-сцена, программа блока, sponsor слот", "LED / Video Wall", listOf("Hero", "Sponsor", "Program"), "#0f766e")
                    demoChannelCard("queue-hub", "Queue Portrait", "Вертикальный queue-канал: вызовы, ETA и приоритетные окна обслуживания", "Portrait Queue", listOf("Portrait Queue", "ETA", "Priority"), "#1d4ed8")
                    demoChannelCard("ad-live-hybrid", "Ad Live Hybrid", "Live stream с рекламными overlay-слоями без остановки эфира", "Ad Overlay", listOf("Sponsor", "CTA", "Ticker"), "#be123c")
                    demoChannelCard("hotel-tv", "Hotel Portrait", "Вертикальный hotel-канал: welcome, сервисы и concierge в формате 9:16", "Portrait Hotel", listOf("Welcome", "Services", "Clock"), "#7c3aed")
                    demoChannelCard("business-center-flow", "Business Center Portrait", "Вертикальный digest-канал бизнес-центра: встречи, wayfinding и трафик", "Portrait Business Center", listOf("Meetings", "Wayfinding", "Traffic"), "#334155")
                }
            }
        }

        section("section showcase-section showcase-templates-section") {
            id = "templates"
            div("section-inner") {
                div("section-header") {
                    h2 { +"Template Catalog" }
                    p { +"Детальные template pages по каждому сценарию экрана: payload, интеграции, visual preview." }
                }
                div("card") {
                    div("card-body") {
                        h3 { +"Нужны premium шаблоны под бренд клиента?" }
                        p { +"Сделаем showroom/service/welcome пакеты и внедрим как готовый контент-слой, даже без смены текущего подрядчика." }
                        div("showcase-cta-actions") {
                            a(href = "/templates", classes = "btn btn-primary") { +"Открыть template catalog" }
                            a(href = "/contact", classes = "btn btn-ghost") { +"Запросить кастомный пакет" }
                        }
                    }
                }
            }
        }

        section("section showcase-section") {
            id = "demos"
            div("section-inner") {
                div("section-header") {
                    h2 { +"How It Works" }
                    p { +"See the complete workflow from channel creation to live broadcast." }
                }
                div("walkthrough-grid") {
                    walkthroughStep("1", "Создать канал", "Задаём профиль канала, регион и дефолтный overlay-пакет под тип экрана.", "create-channel")
                    walkthroughStep("2", "Собрать сетку показов", "Планируем контент по временным окнам и дням недели без пустых промежутков.", "schedule-content")
                    walkthroughStep("3", "Подключить данные", "Привязываем webhook/REST/manual источники для очередей, KPI, и сервисных уведомлений.", "configure-overlays")
                    walkthroughStep("4", "Запустить и контролировать", "Проверяем online-статус устройств, rollback и аудит показов в одном потоке.", "go-live")
                }
            }
        }

        section("section showcase-section showcase-usecases") {
            div("section-inner") {
                div("section-header") {
                    h2 { +"Built For Every Industry" }
                    p { +"От TV и DOOH до госуслуг, банков, отелей и бизнес-центров." }
                }
                div("usecases-grid") {
                    useCaseCard("📺", "Broadcast & TV", "Run 24/7 TV channels with scheduling, live overlays, and as-run logging.")
                    useCaseCard("🏢", "Corporate Displays", "KPI dashboards, meeting signage, and branded welcome screens.")
                    useCaseCard("🧾", "Queue Boards", "ЦОН, банк и сервисные табло с realtime вызовами и ETA.")
                    useCaseCard("🏨", "Hotel TV", "Lobby/in-room channels with concierge and service blocks.")
                    useCaseCard("🏙️", "Business Centers", "Elevator/lobby/floor digest screens with wayfinding and tenant info.")
                    useCaseCard("🛍", "Retail & DOOH", "Promotional content, QR engagement, and ad overlays over live stream.")
                }
            }
        }

        section("cta-section showcase-cta") {
            div("section-inner") {
                div("section-header") {
                    h2 { +"Готовы запустить пилот?" }
                    p { +"Соберём pilot passport, согласуем KPI и покажем branded шаблоны за 1–2 дня до старта пилота." }
                }
                div("showcase-cta-actions") {
                    a(href = "/contact", classes = "btn btn-primary btn-lg") { +"Запустить pilot" }
                    a(href = "/templates", classes = "btn btn-ghost btn-lg") { +"Выбрать шаблоны" }
                }
            }
        }

        script { unsafe { +showcaseScript() } }
    }
}

private fun FlowContent.demoChannelCard(
    id: String, name: String, description: String,
    category: String, overlays: List<String>, color: String
) {
    val isPortrait = id in setOf("roadside-portrait", "queue-hub", "hotel-tv", "business-center-flow")
    a(href = "/showcase/demo/$id", classes = "demo-channel-card${if (isPortrait) " demo-channel-card-portrait" else ""}") {
        div("demo-channel-preview") {
            style = "background: linear-gradient(135deg, ${color}11, ${color}22); position: relative"
            div("demo-channel-screen${if (isPortrait) " demo-channel-screen-portrait" else ""}") {
                style = "position: relative; overflow: hidden"
                div("demo-screen-bg") { style = "background: linear-gradient(160deg, #0f0f1a, #1a1a2e); width: 100%; height: 100%; position: absolute; inset: 0" }
                div("demo-screen-live-bar") {
                    style = "position: absolute; top: 8px; left: 8px; z-index: 2; display: flex; align-items: center; gap: 6px"
                    span("demo-screen-badge") { style = "background: $color"; +"LIVE" }
                    span("demo-screen-name") { +name }
                }
                unsafe { +demoChannelMiniOverlays(id, color) }
            }
        }
        div("demo-channel-info") {
            div("demo-channel-meta") {
                span("demo-channel-category") { style = "color: $color"; +(if (isPortrait) "$category • PORTRAIT" else category) }
                span("demo-channel-arrow") { +"→" }
            }
            h3("demo-channel-name") { +name }
            p("demo-channel-desc") { +description }
            div("demo-channel-overlays") { overlays.forEach { overlay -> span("badge badge-neutral") { +overlay } } }
        }
    }
}

private fun demoChannelMiniOverlays(id: String, color: String): String = when (id) {
    "roadside-portrait" -> """
        <div style="position:absolute;inset:12px auto 12px 12px;width:42%;background:rgba(0,0,0,.5);border-radius:8px;padding:8px;z-index:2;display:flex;flex-direction:column;justify-content:space-between">
            <div style="font-size:8px;color:rgba(255,255,255,.7)">ALMATY • +3°C</div>
            <div style="font-size:20px;font-weight:800;color:#fff">-35%</div>
            <div style="font-size:8px;color:#fff">QR купон</div>
        </div>
        <div style="position:absolute;bottom:0;left:0;right:0;height:20px;background:rgba(0,0,0,.85);display:flex;align-items:center;z-index:2"><span style="background:$color;color:#fff;font-size:7px;font-weight:700;padding:0 6px;height:100%;display:flex;align-items:center">PORTRAIT</span><span style="font-size:8px;color:#fff;padding-left:6px">Roadside offer stack</span></div>
    """
    "led-wall-prime" -> """
        <div style="position:absolute;top:34%;left:8px;right:8px;background:rgba(0,0,0,.55);border-radius:8px;padding:8px;text-align:center;z-index:2">
            <div style="font-size:8px;color:$color">LIVE FORUM</div><div style="font-size:14px;color:#fff;font-weight:700">Digital Signage Future 2026</div>
        </div>
    """
    "queue-hub" -> """
        <div style="position:absolute;top:34px;left:10px;right:10px;background:rgba(4,20,46,.82);border:1px solid rgba(96,165,250,.45);border-radius:8px;padding:6px;z-index:2">
            <div style="font-size:7px;opacity:.75;margin-bottom:4px">QUEUE PORTRAIT</div>
            <div style="font-size:15px;font-weight:800;line-height:1">A-204</div>
            <div style="font-size:8px;margin-top:2px">Window 7 • Now</div>
        </div>
        <div style="position:absolute;bottom:0;left:0;right:0;height:20px;background:rgba(0,0,0,.85);display:flex;align-items:center;z-index:2"><span style="background:$color;color:#fff;font-size:7px;font-weight:700;padding:0 6px;height:100%;display:flex;align-items:center">PORTRAIT</span><span style="font-size:8px;color:#fff;padding-left:6px">Queue stack</span></div>
    """
    "ad-live-hybrid" -> """
        <div style="position:absolute;bottom:24px;left:0;right:0;height:24px;background:rgba(0,0,0,.78);display:flex;align-items:center;z-index:2"><span style="background:$color;color:#fff;font-size:7px;font-weight:700;padding:0 6px;height:100%;display:flex;align-items:center">AD</span><span style="font-size:8px;color:#fff;padding-left:6px">Партнёр эфира: бонус 5 000 ₸ по QR</span></div>
    """
    "hotel-tv" -> """
        <div style="position:absolute;top:34px;left:8px;right:8px;background:linear-gradient(180deg, rgba(30,18,64,.88), rgba(12,10,25,.86));border:1px solid rgba(167,139,250,.38);border-radius:8px;padding:6px;z-index:2;color:#fff;font-size:8px"><div style="font-weight:700;margin-bottom:4px">HOTEL PORTRAIT</div><div>Sky Lounge -20%</div><div>Room Service 24/7</div></div>
        <div style="position:absolute;bottom:0;left:0;right:0;height:20px;background:rgba(0,0,0,.85);display:flex;align-items:center;z-index:2"><span style="background:$color;color:#fff;font-size:7px;font-weight:700;padding:0 6px;height:100%;display:flex;align-items:center">PORTRAIT</span><span style="font-size:8px;color:#fff;padding-left:6px">Hotel concierge</span></div>
    """
    "business-center-flow" -> """
        <div style="position:absolute;top:34px;left:8px;right:8px;background:rgba(15,23,42,.86);border:1px solid rgba(148,163,184,.35);border-radius:8px;padding:6px;color:#fff;font-size:8px;z-index:2"><div style="font-weight:700">BC PORTRAIT • Floor 12</div><div style="opacity:.8">11:00 Kaspi Team • Hall B</div></div>
        <div style="position:absolute;bottom:0;left:0;right:0;height:20px;background:rgba(0,0,0,.85);display:flex;align-items:center;z-index:2"><span style="background:$color;color:#fff;font-size:7px;font-weight:700;padding:0 6px;height:100%;display:flex;align-items:center">PORTRAIT</span><span style="font-size:8px;color:#fff;padding-left:6px">Elevator digest</span></div>
    """
    else -> when (id) {
        "news-24" -> """
            <div style="position:absolute;top:8px;right:8px;background:rgba(0,0,0,.6);border-radius:4px;padding:3px 8px;font-size:11px;color:#fff;font-weight:600;font-variant-numeric:tabular-nums;z-index:2">14:30</div>
            <div style="position:absolute;top:8px;right:60px;background:rgba(0,0,0,.6);border-radius:4px;padding:3px 8px;font-size:10px;color:#fff;display:flex;align-items:center;gap:4px;z-index:2"><span style="font-size:14px">☀️</span>+24°C</div>
            <div style="position:absolute;top:8px;left:54px;background:$color;border-radius:3px;padding:2px 6px;font-size:7px;font-weight:700;color:#fff;z-index:2">N24</div>
            <div style="position:absolute;bottom:0;left:0;right:0;height:24px;background:rgba(0,0,0,.85);display:flex;align-items:center;z-index:2"><span style="background:#e50914;color:#fff;font-size:7px;font-weight:700;padding:0 6px;height:100%;display:flex;align-items:center">BREAKING</span><span style="font-size:9px;color:#fff;white-space:nowrap;padding-left:8px">GDP growth exceeds expectations at 5.2% in Q4...</span></div>
        """
        "sports-live" -> """
            <div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);background:rgba(0,0,0,.7);border-radius:6px;padding:6px 12px;display:flex;align-items:center;gap:8px;z-index:2">
                <span style="font-size:10px;font-weight:600;color:#fff">AST</span>
                <span style="font-size:16px;font-weight:700;color:#fff">2 : 1</span>
                <span style="font-size:10px;font-weight:600;color:#fff">KRT</span>
            </div>
            <div style="position:absolute;top:8px;right:8px;font-size:9px;color:rgba(255,255,255,.5);z-index:2">67'</div>
            <div style="position:absolute;bottom:0;left:0;right:0;height:20px;background:rgba(0,0,0,.85);display:flex;align-items:center;z-index:2"><span style="background:$color;color:#fff;font-size:7px;font-weight:700;padding:0 6px;height:100%;display:flex;align-items:center">STATS</span><span style="font-size:8px;color:#fff;padding-left:6px">Possession: 58% - 42% | Shots: 12 - 8</span></div>
        """
        else -> """
            <div style="position:absolute;bottom:0;left:0;right:0;height:20px;background:rgba(0,0,0,.85);display:flex;align-items:center;z-index:2"><span style="background:$color;color:#fff;font-size:7px;font-weight:700;padding:0 6px;height:100%;display:flex;align-items:center">LIVE</span><span style="font-size:8px;color:#fff;padding-left:6px">Realtime overlays active</span></div>
        """
    }
}

private fun FlowContent.walkthroughStep(step: String, title: String, description: String, demoId: String) {
    div("walkthrough-step") {
        div("walkthrough-step-num") { +step }
        div("walkthrough-step-content") { h3 { +title }; p { +description } }
        div("walkthrough-demo") {
            id = "demo-$demoId"
            when (demoId) {
                "create-channel" -> {
                    div("mini-demo") {
                        div("mini-demo-toolbar") { span("mini-demo-dot red") {}; span("mini-demo-dot yellow") {}; span("mini-demo-dot green") {}; span("mini-demo-title") { +"New Channel" } }
                        div("mini-demo-body") {
                            div("mini-demo-field") { span("mini-demo-label") { +"Channel Name" }; div("mini-demo-input") { id = "demo-typing-channel"; +"News 24|" } }
                            div("mini-demo-field") { span("mini-demo-label") { +"Status" }; div("mini-demo-select") { +"Active ✓" } }
                            div("mini-demo-btn") { +"Create Channel →" }
                        }
                    }
                }
                "schedule-content" -> {
                    div("mini-demo") {
                        div("mini-demo-toolbar") { span("mini-demo-dot red") {}; span("mini-demo-dot yellow") {}; span("mini-demo-dot green") {}; span("mini-demo-title") { +"Schedule Editor" } }
                        div("mini-demo-body mini-demo-timeline") {
                            div("timeline-track") {
                                div("timeline-item ti-1") { style = "width: 30%; background: #3b82f6"; +"intro.mp4" }
                                div("timeline-item ti-2") { style = "width: 45%; background: #8b5cf6"; +"slides.jpg" }
                                div("timeline-item ti-3") { style = "width: 25%; background: #f59e0b"; +"promo.mp4" }
                            }
                            div("timeline-cursor") {}
                        }
                    }
                }
                "configure-overlays" -> {
                    div("mini-demo") {
                        div("mini-demo-toolbar") { span("mini-demo-dot red") {}; span("mini-demo-dot yellow") {}; span("mini-demo-dot green") {}; span("mini-demo-title") { +"Overlay Config" } }
                        div("mini-demo-body mini-demo-overlay-cfg") {
                            div("overlay-cfg-widget") { span("overlay-cfg-icon") { +"📰" }; span { +"Ticker" }; span("overlay-cfg-toggle on") { +"ON" } }
                            div("overlay-cfg-widget") { span("overlay-cfg-icon") { +"🧾" }; span { +"Queue" }; span("overlay-cfg-toggle on") { +"ON" } }
                            div("overlay-cfg-widget") { span("overlay-cfg-icon") { +"🏨" }; span { +"Hotel Info" }; span("overlay-cfg-toggle on") { +"ON" } }
                        }
                    }
                }
                "go-live" -> {
                    div("mini-demo") {
                        div("mini-demo-toolbar") { span("mini-demo-dot red") {}; span("mini-demo-dot yellow") {}; span("mini-demo-dot green") {}; span("mini-demo-title") { +"Live Monitor" } }
                        div("mini-demo-body mini-demo-monitor") {
                            div("monitor-device") { div("monitor-status online") {}; span { +"Lobby Screen #1" }; span("monitor-badge") { +"Online" } }
                            div("monitor-device") { div("monitor-status online") {}; span { +"Outdoor Portrait #3" }; span("monitor-badge") { +"Online" } }
                            div("monitor-device") { div("monitor-status offline") {}; span { +"Floor TV #12" }; span("monitor-badge warn") { +"Offline" } }
                        }
                    }
                }
            }
        }
    }
}

private fun FlowContent.useCaseCard(emoji: String, title: String, description: String) {
    div("usecase-card") { div("usecase-icon") { +emoji }; h3 { +title }; p { +description } }
}

private fun showcaseScript(): String = """
(function() {
    var el = document.getElementById('demo-typing-channel');
    if (!el) return;
    var texts = ['News 24', 'Queue Hub', 'Hotel TV', 'Roadside Portrait'];
    var ti = 0, ci = 0, deleting = false;
    function type() {
        var txt = texts[ti];
        if (!deleting) {
            ci++;
            el.textContent = txt.substring(0, ci) + '|';
            if (ci >= txt.length) { setTimeout(function(){ deleting = true; type(); }, 2000); return; }
        } else {
            ci--;
            el.textContent = txt.substring(0, ci) + '|';
            if (ci <= 0) { deleting = false; ti = (ti+1) % texts.length; }
        }
        setTimeout(type, deleting ? 50 : 100);
    }
    type();
})();

document.querySelectorAll('a[href^="#"]').forEach(function(a) {
    a.addEventListener('click', function(e) {
        var target = document.querySelector(this.getAttribute('href'));
        if (target) { e.preventDefault(); target.scrollIntoView({behavior:'smooth', block:'start'}); }
    });
});

(function() {
    var cursor = document.querySelector('.timeline-cursor');
    if (!cursor) return;
    var pos = 0;
    function move() {
        pos = (pos + 0.3) % 100;
        cursor.style.left = pos + '%';
        requestAnimationFrame(move);
    }
    move();
})();
"""