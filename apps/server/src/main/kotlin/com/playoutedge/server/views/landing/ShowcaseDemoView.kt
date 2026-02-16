package com.playoutedge.server.views.landing

import kotlinx.html.*

data class DemoChannel(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val color: String,
    val bgImages: List<String>,
    val overlayWidgets: List<DemoWidget>
)

data class DemoWidget(
    val type: String,
    val html: String,
    val css: String = ""
)

val DEMO_CHANNELS = mapOf(
    "news-24" to DemoChannel(
        id = "news-24",
        name = "News 24",
        category = "News & Media",
        description = "24/7 news broadcast with live ticker, weather updates, clock, and breaking news alerts",
        color = "#dc2626",
        bgImages = listOf(
            "https://images.unsplash.com/photo-1504711434969-e33886168d6c?w=1280&q=80",
            "https://images.unsplash.com/photo-1495020689067-958852a7765e?w=1280&q=80",
            "https://images.unsplash.com/photo-1586339949916-3e9457bef6d3?w=1280&q=80"
        ),
        overlayWidgets = listOf(
            DemoWidget("ticker", """<div class="ov-ticker"><div class="ov-ticker-label">BREAKING</div><div class="ov-ticker-track"><span class="ov-ticker-text">Kazakh GDP growth exceeds expectations at 5.2% in Q4 2025 • Oil prices stabilize at $78/barrel • Tech sector sees 12% increase in investment • Weather: sunny, 24°C in Almaty</span></div></div>"""),
            DemoWidget("weather", """<div class="ov-weather"><span class="ov-w-icon">☀️</span><div><div class="ov-w-temp">+24°C</div><div class="ov-w-city">Almaty</div></div></div>"""),
            DemoWidget("clock", """<div class="ov-clock" id="demo-clock-news"></div>"""),
            DemoWidget("logo", """<div class="ov-logo-bug" style="background:#dc2626">N24</div>""")
        )
    ),
    "sports-live" to DemoChannel(
        id = "sports-live",
        name = "Sports Live",
        category = "Sports",
        description = "Live sports coverage with dynamic scoreboard, audience reactions, and real-time statistics",
        color = "#059669",
        bgImages = listOf(
            "https://images.unsplash.com/photo-1461896836934-bd45ea8ba7e2?w=1280&q=80",
            "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=1280&q=80",
            "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=1280&q=80"
        ),
        overlayWidgets = listOf(
            DemoWidget("score", """<div class="ov-score"><div class="ov-score-team"><span class="ov-flag">🇰🇿</span>Astana FC</div><div class="ov-score-nums"><span id="demo-score-a">2</span>:<span id="demo-score-b">1</span></div><div class="ov-score-team">Kairat FC<span class="ov-flag">🇰🇿</span></div><div class="ov-score-period">2nd Half — <span id="demo-minute">67</span>'</div></div>"""),
            DemoWidget("reactions", """<div class="ov-reactions" id="demo-reactions"></div>"""),
            DemoWidget("ticker", """<div class="ov-ticker"><div class="ov-ticker-label" style="background:#059669">STATS</div><div class="ov-ticker-track"><span class="ov-ticker-text">Possession: Astana 58% — Kairat 42% • Shots on target: 7-4 • Corner kicks: 5-3 • Yellow cards: 2-1</span></div></div>""")
        )
    ),
    "corporate-lobby" to DemoChannel(
        id = "corporate-lobby",
        name = "Corporate Lobby",
        category = "Corporate",
        description = "Professional lobby display with KPI dashboard, visitor welcome, and branded content",
        color = "#2563eb",
        bgImages = listOf(
            "https://images.unsplash.com/photo-1497366216548-37526070297c?w=1280&q=80",
            "https://images.unsplash.com/photo-1497215842964-222b430dc094?w=1280&q=80",
            "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=1280&q=80"
        ),
        overlayWidgets = listOf(
            DemoWidget("kpi", """<div class="ov-kpi-grid"><div class="ov-kpi"><div class="ov-kpi-val" style="color:#3b82f6">${'$'}2.4M</div><div class="ov-kpi-lbl">Revenue</div></div><div class="ov-kpi"><div class="ov-kpi-val" style="color:#10b981">+18%</div><div class="ov-kpi-lbl">Growth</div></div><div class="ov-kpi"><div class="ov-kpi-val" style="color:#f59e0b">847</div><div class="ov-kpi-lbl">Clients</div></div><div class="ov-kpi"><div class="ov-kpi-val" style="color:#8b5cf6">99.9%</div><div class="ov-kpi-lbl">SLA</div></div></div>"""),
            DemoWidget("lower-third", """<div class="ov-lower-third"><div class="ov-lt-name">Welcome to Adapto HQ</div><div class="ov-lt-title">Innovation Center — Building the Future of Broadcast</div></div>"""),
            DemoWidget("clock", """<div class="ov-clock" id="demo-clock-corp"></div>"""),
            DemoWidget("qr", """<div class="ov-qr"><div class="ov-qr-grid"><div class="ov-qr-c"></div><div class="ov-qr-c"></div><div class="ov-qr-c"></div><div class="ov-qr-c"></div><div class="ov-qr-c ov-qr-w"></div><div class="ov-qr-c"></div><div class="ov-qr-c"></div><div class="ov-qr-c"></div><div class="ov-qr-c"></div></div><div class="ov-qr-txt">Wi-Fi: Connect</div></div>""")
        )
    ),
    "retail-signage" to DemoChannel(
        id = "retail-signage",
        name = "Retail Signage",
        category = "Retail",
        description = "Engaging retail displays with promotions, polls, QR engagement, and countdown deals",
        color = "#d97706",
        bgImages = listOf(
            "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1280&q=80",
            "https://images.unsplash.com/photo-1472851294608-062f824d29cc?w=1280&q=80",
            "https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?w=1280&q=80"
        ),
        overlayWidgets = listOf(
            DemoWidget("countdown", """<div class="ov-countdown"><div class="ov-cd-label">FLASH SALE ENDS IN</div><div class="ov-cd-nums"><span class="ov-cd-num" id="demo-cd-h">02</span><span class="ov-cd-sep">:</span><span class="ov-cd-num" id="demo-cd-m">45</span><span class="ov-cd-sep">:</span><span class="ov-cd-num" id="demo-cd-s">30</span></div></div>"""),
            DemoWidget("poll", """<div class="ov-poll"><div class="ov-poll-q">Which deal interests you?</div><div class="ov-poll-opt"><div class="ov-poll-bar" style="width:55%">Electronics — 55%</div></div><div class="ov-poll-opt"><div class="ov-poll-bar ov-poll-alt" style="width:30%">Fashion — 30%</div></div><div class="ov-poll-opt"><div class="ov-poll-bar ov-poll-alt2" style="width:15%">Home — 15%</div></div></div>"""),
            DemoWidget("ticker", """<div class="ov-ticker"><div class="ov-ticker-label" style="background:#d97706">DEALS</div><div class="ov-ticker-track"><span class="ov-ticker-text">🔥 50% OFF Electronics today only • Free shipping on orders over $50 • New arrivals in Fashion — check aisle 3 • Loyalty members get extra 10% OFF</span></div></div>""")
        )
    ),
    "event-stream" to DemoChannel(
        id = "event-stream",
        name = "Event Stream",
        category = "Events",
        description = "Live event coverage with audience engagement, speaker intros, and Q&A management",
        color = "#7c3aed",
        bgImages = listOf(
            "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=1280&q=80",
            "https://images.unsplash.com/photo-1505373877841-8d25f7d46678?w=1280&q=80",
            "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=1280&q=80"
        ),
        overlayWidgets = listOf(
            DemoWidget("lower-third", """<div class="ov-lower-third"><div class="ov-lt-name">Dr. Sarah Chen</div><div class="ov-lt-title">Keynote: The Future of AI in Broadcasting</div></div>"""),
            DemoWidget("reactions", """<div class="ov-reactions" id="demo-reactions-event"></div>"""),
            DemoWidget("countdown", """<div class="ov-countdown ov-cd-small"><div class="ov-cd-label">NEXT SESSION</div><div class="ov-cd-nums"><span class="ov-cd-num" id="demo-cd2-m">15</span><span class="ov-cd-sep">:</span><span class="ov-cd-num" id="demo-cd2-s">00</span></div></div>"""),
            DemoWidget("ticker", """<div class="ov-ticker"><div class="ov-ticker-label" style="background:#7c3aed">Q&A</div><div class="ov-ticker-track"><span class="ov-ticker-text">Submit questions via QR code on your badge • Next panel: "Scaling Digital Signage Networks" at 14:30 • WiFi: EventGuest / pass: innovate2026</span></div></div>""")
        )
    ),
    "education-tv" to DemoChannel(
        id = "education-tv",
        name = "Education TV",
        category = "Education",
        description = "Campus broadcasting with schedules, announcements, and educational content rotation",
        color = "#0891b2",
        bgImages = listOf(
            "https://images.unsplash.com/photo-1523050854058-8df90110c476?w=1280&q=80",
            "https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=1280&q=80",
            "https://images.unsplash.com/photo-1562774053-701939374585?w=1280&q=80"
        ),
        overlayWidgets = listOf(
            DemoWidget("table", """<div class="ov-table"><div class="ov-table-title">Today's Schedule</div><div class="ov-table-hdr"><span>Time</span><span>Subject</span><span>Room</span></div><div class="ov-table-row"><span>09:00</span><span>Mathematics</span><span>A-101</span></div><div class="ov-table-row ov-table-active"><span>10:30</span><span>Physics Lab</span><span>B-204</span></div><div class="ov-table-row"><span>12:00</span><span>Lunch Break</span><span>Cafeteria</span></div><div class="ov-table-row"><span>13:30</span><span>CS Workshop</span><span>C-301</span></div></div>"""),
            DemoWidget("ticker", """<div class="ov-ticker"><div class="ov-ticker-label" style="background:#0891b2">CAMPUS</div><div class="ov-ticker-track"><span class="ov-ticker-text">📚 Library hours extended to 22:00 this week • 🏆 Congratulations to the robotics team! • 📅 Registration for spring semester opens Monday • 🎭 Drama club performance Friday at 18:00</span></div></div>"""),
            DemoWidget("clock", """<div class="ov-clock" id="demo-clock-edu"></div>"""),
            DemoWidget("logo", """<div class="ov-logo-bug" style="background:#0891b2">EDU</div>""")
        )
    )
)

fun HTML.showcaseDemoView(demoId: String) {
    val demo = DEMO_CHANNELS[demoId]
    if (demo == null) {
        publicLayout("Demo Not Found", currentPath = "/showcase") {
            section("section") {
                div("section-inner") {
                    h1 { +"Demo not found" }
                    p { +"The requested demo channel does not exist." }
                    a(href = "/showcase", classes = "btn btn-primary") { +"Back to Showcase" }
                }
            }
        }
        return
    }

    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title { +"${demo.name} — Demo | Playout Edge" }
        link(rel = "stylesheet", href = "/admin/static/styles.css")
        style {
            unsafe { +demoCss(demo.color) }
        }
    }
    body("demo-page") {
        // Top bar
        div("demo-topbar") {
            a(href = "/showcase#demo-channels", classes = "demo-back") {
                unsafe { +"""<svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>""" }
                +"Back to Showcase"
            }
            div("demo-topbar-info") {
                span("demo-topbar-badge") {
                    style = "background: ${demo.color}"
                    +"LIVE DEMO"
                }
                span("demo-topbar-name") { +demo.name }
                span("demo-topbar-cat") { +demo.category }
            }
            div("demo-topbar-actions") {
                a(href = "/contact", classes = "btn btn-primary btn-sm") { +"Запросить pilot" }
            }
        }

        // Main demo area
        div("demo-container") {
            // TV Screen
            div("demo-screen") {
                id = "demo-screen"

                // Background images (will cycle)
                demo.bgImages.forEachIndexed { i, url ->
                    div("demo-bg${if (i == 0) " active" else ""}") {
                        style = "background-image: url('$url')"
                    }
                }

                // Overlay widgets
                div("demo-overlay") {
                    demo.overlayWidgets.forEach { widget ->
                        unsafe { +widget.html }
                    }
                }
            }

            // Info panel
            div("demo-info-panel") {
                h2 { +demo.name }
                p("demo-info-desc") { +demo.description }

                div("demo-info-section") {
                    h4 { +"Active Overlays" }
                    div("demo-widgets-list") {
                        demo.overlayWidgets.forEach { w ->
                            div("demo-widget-item") {
                                span("demo-widget-dot") {
                                    style = "background: ${demo.color}"
                                }
                                span { +w.type.replaceFirstChar { it.uppercase() } }
                            }
                        }
                    }
                }

                div("demo-info-section") {
                    h4 { +"Integration" }
                    div("demo-code-block") {
                        code {
                            +"""<iframe src="https://tv.adapto.kz/embed/{channelId}" width="100%" height="100%" frameborder="0"></iframe>"""
                        }
                    }
                }

                div("demo-info-section") {
                    h4 { +"Следующий шаг" }
                    p { +"Покажем как эти шаблоны адаптируются под ваш брендбук и текущую инфраструктуру." }
                    a(href = "/contact", classes = "btn btn-primary") { +"Обсудить внедрение" }
                }
            }
        }

        // Demo JS
        script {
            unsafe {
                +demoScript(demo)
            }
        }
    }
}

private fun demoCss(color: String): String = """
* { margin: 0; padding: 0; box-sizing: border-box; }
body.demo-page { background: var(--gray-900); color: #fff; font-family: var(--font-family); min-height: 100vh; }

.demo-topbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 24px; background: rgba(0,0,0,0.6); backdrop-filter: blur(12px); border-bottom: 1px solid rgba(255,255,255,0.1); position: sticky; top: 0; z-index: 50; }
.demo-back { display: flex; align-items: center; gap: 6px; color: var(--gray-400); font-size: 13px; text-decoration: none; transition: color 0.2s; }
.demo-back:hover { color: #fff; }
.demo-topbar-info { display: flex; align-items: center; gap: 12px; }
.demo-topbar-badge { font-size: 10px; font-weight: 700; padding: 3px 8px; border-radius: 4px; color: #fff; letter-spacing: 0.5px; animation: pulse-badge 2s infinite; }
@keyframes pulse-badge { 0%,100% { opacity: 1; } 50% { opacity: 0.7; } }
.demo-topbar-name { font-weight: 600; font-size: 15px; }
.demo-topbar-cat { color: var(--gray-500); font-size: 13px; }
.demo-topbar-actions .btn { font-size: 12px; }

.demo-container { display: grid; grid-template-columns: 1fr 340px; gap: 0; height: calc(100vh - 49px); }
@media (max-width: 900px) { .demo-container { grid-template-columns: 1fr; } .demo-info-panel { display: none; } }

.demo-screen { position: relative; overflow: hidden; background: #000; }
.demo-bg { position: absolute; inset: 0; background-size: cover; background-position: center; opacity: 0; transition: opacity 1.5s ease-in-out; }
.demo-bg.active { opacity: 1; }
.demo-overlay { position: absolute; inset: 0; z-index: 10; pointer-events: none; font-family: 'Segoe UI', Arial, sans-serif; }

.demo-info-panel { background: rgba(0,0,0,0.4); backdrop-filter: blur(12px); padding: 24px; overflow-y: auto; border-left: 1px solid rgba(255,255,255,0.08); }
.demo-info-panel h2 { font-size: 22px; margin-bottom: 8px; }
.demo-info-desc { color: var(--gray-400); font-size: 14px; line-height: 1.5; margin-bottom: 24px; }
.demo-info-section { margin-bottom: 24px; }
.demo-info-section h4 { font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; color: var(--gray-500); margin-bottom: 12px; }
.demo-widgets-list { display: flex; flex-direction: column; gap: 8px; }
.demo-widget-item { display: flex; align-items: center; gap: 8px; font-size: 13px; padding: 6px 10px; background: rgba(255,255,255,0.05); border-radius: 6px; }
.demo-widget-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.demo-code-block { background: rgba(0,0,0,0.4); border-radius: 8px; padding: 12px; overflow-x: auto; }
.demo-code-block code { font-size: 11px; color: var(--gray-400); font-family: var(--font-mono); white-space: pre-wrap; word-break: break-all; }

/* === OVERLAY WIDGET STYLES === */
.ov-ticker { position: absolute; bottom: 0; left: 0; width: 100%; height: 44px; background: rgba(0,0,0,0.85); display: flex; align-items: center; overflow: hidden; }
.ov-ticker-label { background: #e50914; color: #fff; font-weight: 700; font-size: 12px; padding: 0 14px; height: 100%; display: flex; align-items: center; white-space: nowrap; text-transform: uppercase; flex-shrink: 0; }
.ov-ticker-track { flex: 1; overflow: hidden; height: 100%; display: flex; align-items: center; }
.ov-ticker-text { white-space: nowrap; font-size: 14px; color: #fff; font-weight: 500; animation: ticker-scroll 25s linear infinite; padding-left: 100%; }
@keyframes ticker-scroll { 0% { transform: translateX(0); } 100% { transform: translateX(-100%); } }

.ov-weather { position: absolute; top: 16px; right: 16px; background: rgba(0,0,0,0.7); color: #fff; border-radius: 8px; padding: 10px 14px; backdrop-filter: blur(8px); display: flex; align-items: center; gap: 10px; }
.ov-w-icon { font-size: 28px; }
.ov-w-temp { font-size: 20px; font-weight: 700; }
.ov-w-city { font-size: 11px; opacity: 0.7; }

.ov-clock { position: absolute; top: 16px; right: 180px; background: rgba(0,0,0,0.7); color: #fff; border-radius: 8px; padding: 8px 14px; backdrop-filter: blur(8px); font-size: 20px; font-weight: 600; font-variant-numeric: tabular-nums; }

.ov-logo-bug { position: absolute; top: 16px; left: 16px; color: #fff; font-size: 14px; font-weight: 700; padding: 6px 12px; border-radius: 6px; letter-spacing: 1px; }

.ov-score { position: absolute; top: 16px; left: 50%; transform: translateX(-50%); background: rgba(0,0,0,0.85); color: #fff; border-radius: 10px; padding: 12px 20px; backdrop-filter: blur(8px); display: flex; align-items: center; gap: 16px; flex-wrap: wrap; justify-content: center; }
.ov-score-team { display: flex; align-items: center; gap: 6px; font-size: 14px; font-weight: 600; white-space: nowrap; }
.ov-flag { font-size: 18px; }
.ov-score-nums { font-size: 32px; font-weight: 700; display: flex; align-items: center; gap: 4px; font-variant-numeric: tabular-nums; }
.ov-score-period { width: 100%; text-align: center; font-size: 11px; opacity: 0.6; }

.ov-reactions { position: absolute; right: 20px; bottom: 60px; width: 60px; height: 200px; }
.ov-reaction { position: absolute; bottom: 0; font-size: 28px; animation: float-up 3s ease-out forwards; opacity: 0; }
@keyframes float-up { 0% { opacity: 1; transform: translateY(0) scale(1); } 100% { opacity: 0; transform: translateY(-180px) scale(0.4); } }

.ov-kpi-grid { position: absolute; top: 16px; right: 16px; display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.ov-kpi { background: rgba(0,0,0,0.8); border-radius: 8px; padding: 12px 16px; text-align: center; backdrop-filter: blur(8px); border: 1px solid rgba(255,255,255,0.1); }
.ov-kpi-val { font-size: 24px; font-weight: 700; }
.ov-kpi-lbl { font-size: 10px; text-transform: uppercase; letter-spacing: 0.5px; opacity: 0.6; margin-top: 2px; color: #fff; }

.ov-lower-third { position: absolute; bottom: 56px; left: 0; animation: slide-in 0.5s ease-out; }
.ov-lt-name { background: #fff; color: #000; font-weight: 700; font-size: 16px; padding: 8px 20px; display: inline-block; }
.ov-lt-title { background: rgba(0,0,0,0.8); color: #fff; font-size: 13px; padding: 6px 20px; display: inline-block; }
@keyframes slide-in { 0% { transform: translateX(-100%); opacity: 0; } 100% { transform: translateX(0); opacity: 1; } }

.ov-qr { position: absolute; bottom: 60px; right: 16px; background: #fff; border-radius: 10px; padding: 12px; text-align: center; }
.ov-qr-grid { display: grid; grid-template-columns: repeat(3,20px); gap: 3px; margin: 0 auto; width: fit-content; }
.ov-qr-c { width: 20px; height: 20px; background: #000; border-radius: 2px; }
.ov-qr-w { background: #fff; border: 1px solid #000; }
.ov-qr-txt { font-size: 10px; color: #333; font-weight: 600; margin-top: 6px; }

.ov-countdown { position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%); background: rgba(0,0,0,0.85); border-radius: 12px; padding: 16px 24px; text-align: center; backdrop-filter: blur(8px); }
.ov-countdown.ov-cd-small { top: 16px; left: auto; right: 16px; transform: none; padding: 10px 16px; }
.ov-cd-label { font-size: 11px; text-transform: uppercase; letter-spacing: 1px; opacity: 0.6; color: #fff; margin-bottom: 6px; }
.ov-cd-nums { display: flex; align-items: center; gap: 4px; justify-content: center; }
.ov-cd-num { font-size: 28px; font-weight: 700; color: #fff; font-variant-numeric: tabular-nums; background: rgba(255,255,255,0.1); border-radius: 6px; padding: 4px 8px; min-width: 44px; text-align: center; }
.ov-cd-small .ov-cd-num { font-size: 20px; min-width: 32px; padding: 2px 6px; }
.ov-cd-sep { font-size: 24px; font-weight: 700; color: rgba(255,255,255,0.4); }

.ov-poll { position: absolute; top: 50%; right: 16px; transform: translateY(-50%); background: rgba(0,0,0,0.85); border-radius: 10px; padding: 14px; backdrop-filter: blur(8px); min-width: 240px; color: #fff; }
.ov-poll-q { font-size: 14px; font-weight: 700; margin-bottom: 10px; }
.ov-poll-opt { margin-bottom: 6px; }
.ov-poll-bar { background: linear-gradient(90deg,#667eea,#764ba2); border-radius: 4px; padding: 5px 10px; font-size: 12px; font-weight: 500; color: #fff; white-space: nowrap; }
.ov-poll-alt { background: linear-gradient(90deg,#f59e0b,#ef4444); }
.ov-poll-alt2 { background: linear-gradient(90deg,#10b981,#3b82f6); }

.ov-table { position: absolute; top: 16px; right: 16px; background: rgba(0,0,0,0.85); border-radius: 10px; padding: 14px; backdrop-filter: blur(8px); color: #fff; min-width: 280px; }
.ov-table-title { font-size: 13px; font-weight: 700; margin-bottom: 10px; text-transform: uppercase; letter-spacing: 0.5px; }
.ov-table-hdr { display: grid; grid-template-columns: 60px 1fr 60px; gap: 8px; font-size: 10px; text-transform: uppercase; opacity: 0.5; padding-bottom: 6px; border-bottom: 1px solid rgba(255,255,255,0.15); margin-bottom: 6px; }
.ov-table-row { display: grid; grid-template-columns: 60px 1fr 60px; gap: 8px; font-size: 12px; padding: 5px 0; border-bottom: 1px solid rgba(255,255,255,0.05); }
.ov-table-active { background: rgba(255,255,255,0.08); border-radius: 4px; padding: 5px 4px; border-bottom: none; }
"""

private fun demoScript(demo: DemoChannel): String = """
(function() {
    // Background image cycling
    var bgs = document.querySelectorAll('.demo-bg');
    var currentBg = 0;
    if (bgs.length > 1) {
        setInterval(function() {
            bgs[currentBg].classList.remove('active');
            currentBg = (currentBg + 1) % bgs.length;
            bgs[currentBg].classList.add('active');
        }, 8000);
    }

    // Live clocks
    function tickClocks() {
        var now = new Date();
        var time = now.toLocaleTimeString('en-GB', {hour:'2-digit',minute:'2-digit',second:'2-digit',hour12:false});
        ['demo-clock-news','demo-clock-corp','demo-clock-edu'].forEach(function(id) {
            var el = document.getElementById(id);
            if (el) el.textContent = time;
        });
    }
    tickClocks();
    setInterval(tickClocks, 1000);

    // Sports minute counter
    var minEl = document.getElementById('demo-minute');
    if (minEl) {
        var m = 67;
        setInterval(function() {
            m++;
            if (m > 90) m = 1;
            minEl.textContent = m;
        }, 3000);
    }

    // Reactions animation
    var emojis = ['👍','❤️','😂','🎉','👏','🔥','⚽'];
    ['demo-reactions','demo-reactions-event'].forEach(function(id) {
        var container = document.getElementById(id);
        if (!container) return;
        setInterval(function() {
            var span = document.createElement('span');
            span.className = 'ov-reaction';
            span.textContent = emojis[Math.floor(Math.random()*emojis.length)];
            span.style.left = Math.random()*40 + 'px';
            span.style.animationDelay = Math.random()*0.5 + 's';
            container.appendChild(span);
            setTimeout(function(){ span.remove(); }, 3500);
        }, 800 + Math.random()*1200);
    });

    // Countdown timer
    var cdH = document.getElementById('demo-cd-h');
    var cdM = document.getElementById('demo-cd-m');
    var cdS = document.getElementById('demo-cd-s');
    if (cdH && cdM && cdS) {
        var total = 2*3600 + 45*60 + 30;
        setInterval(function() {
            total--;
            if (total < 0) total = 3*3600;
            var h = Math.floor(total/3600);
            var m = Math.floor((total%3600)/60);
            var s = total%60;
            cdH.textContent = String(h).padStart(2,'0');
            cdM.textContent = String(m).padStart(2,'0');
            cdS.textContent = String(s).padStart(2,'0');
        }, 1000);
    }

    // Small countdown
    var cd2M = document.getElementById('demo-cd2-m');
    var cd2S = document.getElementById('demo-cd2-s');
    if (cd2M && cd2S) {
        var t2 = 15*60;
        setInterval(function() {
            t2--;
            if (t2 < 0) t2 = 30*60;
            cd2M.textContent = String(Math.floor(t2/60)).padStart(2,'0');
            cd2S.textContent = String(t2%60).padStart(2,'0');
        }, 1000);
    }
})();
"""
