package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.showcaseView() {
    publicLayout("Product Showcase", currentPath = "/showcase") {
        // Hero
        section("showcase-hero") {
            // Floating widget ghosts in background
            div("showcase-hero-widgets") {
                unsafe {
                    +"""
                    <div class="hero-float-widget hfw-1" style="top:15%;left:8%"><div style="background:rgba(0,0,0,.4);border-radius:6px;padding:6px 10px;font-size:11px;color:rgba(255,255,255,.6);backdrop-filter:blur(8px)">📰 Breaking News Ticker</div></div>
                    <div class="hero-float-widget hfw-2" style="top:25%;right:10%"><div style="background:rgba(0,0,0,.4);border-radius:6px;padding:6px 10px;font-size:11px;color:rgba(255,255,255,.6);backdrop-filter:blur(8px)">🏆 2 : 1 &nbsp;67'</div></div>
                    <div class="hero-float-widget hfw-3" style="bottom:20%;left:12%"><div style="background:rgba(0,0,0,.4);border-radius:6px;padding:6px 10px;font-size:11px;color:rgba(255,255,255,.6);backdrop-filter:blur(8px)">📊 KPI: $2.4M</div></div>
                    <div class="hero-float-widget hfw-4" style="bottom:30%;right:8%"><div style="background:rgba(0,0,0,.4);border-radius:6px;padding:6px 10px;font-size:11px;color:rgba(255,255,255,.6);backdrop-filter:blur(8px)">⏱ 02:45:30</div></div>
                    """
                }
            }
            div("showcase-hero-content") {
                div("showcase-hero-badge") { +"Product Showcase" }
                h1 { +"See Playout Edge in Action" }
                p("showcase-hero-subtitle") {
                    +"Explore live demo channels, overlay templates, and interactive walkthroughs. No signup required."
                }
                div("showcase-hero-stats") {
                    div("hero-stat") {
                        span("hero-stat-num") { +"6" }
                        span("hero-stat-label") { +"Demo Channels" }
                    }
                    div("hero-stat") {
                        span("hero-stat-num") { +"15+" }
                        span("hero-stat-label") { +"Templates" }
                    }
                    div("hero-stat") {
                        span("hero-stat-num") { +"24/7" }
                        span("hero-stat-label") { +"Live Overlays" }
                    }
                }
                div("showcase-hero-actions") {
                    a(href = "#demo-channels", classes = "btn btn-primary btn-lg") { +"View Demo Channels" }
                    a(href = "#templates", classes = "btn btn-ghost btn-lg") { +"Browse Templates" }
                }
            }
        }

        // Demo Channels Section
        section("section showcase-section") {
            id = "demo-channels"
            div("section-inner") {
                div("section-header") {
                    h2 { +"Live Demo Channels" }
                    p { +"Click any channel to see a live preview with overlays, scheduling, and real-time updates." }
                }
                div("demo-channels-grid") {
                    demoChannelCard(
                        id = "news-24",
                        name = "News 24",
                        description = "Breaking news with live ticker, weather widget, and clock overlay",
                        category = "News & Media",
                        overlays = listOf("Ticker", "Weather", "Clock", "Breaking News"),
                        color = "#dc2626"
                    )
                    demoChannelCard(
                        id = "sports-live",
                        name = "Sports Live",
                        description = "Live sports broadcast with scoreboard, reactions, and KPI stats",
                        category = "Sports",
                        overlays = listOf("Score", "Reactions", "KPI Tiles", "Ticker"),
                        color = "#059669"
                    )
                    demoChannelCard(
                        id = "corporate-lobby",
                        name = "Corporate Lobby",
                        description = "Corporate display with KPI dashboard, QR code, and branded lower thirds",
                        category = "Corporate",
                        overlays = listOf("KPI Tiles", "QR Card", "Lower Third", "Clock"),
                        color = "#2563eb"
                    )
                    demoChannelCard(
                        id = "retail-signage",
                        name = "Retail Signage",
                        description = "Retail digital signage with polls, countdown timers, and promotional tickers",
                        category = "Retail",
                        overlays = listOf("Poll", "Countdown", "Ticker", "QR Card"),
                        color = "#d97706"
                    )
                    demoChannelCard(
                        id = "event-stream",
                        name = "Event Stream",
                        description = "Live event broadcasting with audience reactions, Q&A queue, and schedule",
                        category = "Events",
                        overlays = listOf("Reactions", "Queue Table", "Lower Third", "Countdown"),
                        color = "#7c3aed"
                    )
                    demoChannelCard(
                        id = "education-tv",
                        name = "Education TV",
                        description = "Educational broadcast with timetable, news ticker, and animated branding",
                        category = "Education",
                        overlays = listOf("Queue Table", "News Ticker", "Animated Logo", "Clock"),
                        color = "#0891b2"
                    )
                }
            }
        }

        // Widget Templates Gallery
        section("section showcase-section showcase-templates-section") {
            id = "templates"
            div("section-inner") {
                div("section-header") {
                    h2 { +"Overlay Template Gallery" }
                    p { +"15 production-ready templates. Customize via API or admin panel — no code required." }
                }
                div("template-categories") {
                    templateCategoryPill("all", "All", true)
                    templateCategoryPill("text", "Text")
                    templateCategoryPill("data", "Data")
                    templateCategoryPill("media", "Media")
                    templateCategoryPill("info", "Info")
                    templateCategoryPill("time", "Time")
                    templateCategoryPill("interactive", "Interactive")
                    templateCategoryPill("sports", "Sports")
                }
                div("templates-grid") {
                    id = "templates-grid"
                    templateCard("ticker", "Ticker", "text",
                        "Scrolling text ticker at bottom of screen",
                        """<div class="tp-ticker"><div class="tp-ticker-label">BREAKING</div><div class="tp-ticker-track"><span class="tp-ticker-text">Breaking news: live updates scrolling across the screen in real-time...</span></div></div>""")
                    templateCard("news-ticker", "News Ticker", "text",
                        "Scrolling news feed from RSS/URL",
                        """<div class="tp-ticker"><div class="tp-ticker-label">NEWS</div><div class="tp-ticker-track"><span class="tp-ticker-text">Top stories: Technology, Finance, Sports — updated every 5 minutes</span></div></div>""")
                    templateCard("breaking-news", "Breaking News", "text",
                        "Full-width breaking news alert bar",
                        """<div class="tp-breaking"><span class="tp-breaking-flash">●</span> BREAKING NEWS: Major announcement expected at 15:00 UTC</div>""")
                    templateCard("kpi-tiles", "KPI Tiles", "data",
                        "Grid of key performance indicators",
                        """<div class="tp-kpi-grid"><div class="tp-kpi"><span class="tp-kpi-val">1,247</span><span class="tp-kpi-lbl">Viewers</span></div><div class="tp-kpi"><span class="tp-kpi-val">89%</span><span class="tp-kpi-lbl">Uptime</span></div><div class="tp-kpi"><span class="tp-kpi-val">42</span><span class="tp-kpi-lbl">Active</span></div><div class="tp-kpi"><span class="tp-kpi-val">3.2k</span><span class="tp-kpi-lbl">Total</span></div></div>""")
                    templateCard("queue-table", "Queue Table", "data",
                        "Dynamic table showing queue data",
                        """<div class="tp-table"><div class="tp-table-hdr"><span>Name</span><span>Status</span><span>Wait</span></div><div class="tp-table-row"><span>Alice K.</span><span class="tp-dot tp-green"></span><span>2m</span></div><div class="tp-table-row"><span>Bob M.</span><span class="tp-dot tp-yellow"></span><span>5m</span></div><div class="tp-table-row"><span>Carol D.</span><span class="tp-dot tp-red"></span><span>12m</span></div></div>""")
                    templateCard("poll", "Poll", "data",
                        "Interactive poll with voting",
                        """<div class="tp-poll"><div class="tp-poll-q">What do you prefer?</div><div class="tp-poll-opt"><div class="tp-poll-bar" style="width:65%">Option A — 65%</div></div><div class="tp-poll-opt"><div class="tp-poll-bar tp-poll-bar-alt" style="width:35%">Option B — 35%</div></div></div>""")
                    templateCard("qr-card", "QR Card", "media",
                        "Card with QR code and call to action",
                        """<div class="tp-qr"><div class="tp-qr-grid"><div class="tp-qr-c"></div><div class="tp-qr-c"></div><div class="tp-qr-c"></div><div class="tp-qr-c"></div><div class="tp-qr-c tp-qr-w"></div><div class="tp-qr-c"></div><div class="tp-qr-c"></div><div class="tp-qr-c"></div><div class="tp-qr-c"></div></div><div class="tp-qr-txt">Scan to join</div></div>""")
                    templateCard("logo", "Logo", "media",
                        "Channel logo watermark (bug)",
                        """<div class="tp-logo">P<span>EDGE</span></div>""")
                    templateCard("animated-logo", "Animated Logo", "media",
                        "Logo with CSS animation (pulse, bounce, spin)",
                        """<div class="tp-logo tp-logo-pulse">P<span>EDGE</span></div>""")
                    templateCard("weather", "Weather", "info",
                        "Live weather widget",
                        """<div class="tp-weather"><span class="tp-weather-icon">☀️</span><div><div class="tp-weather-temp">+24°C</div><div class="tp-weather-city">Almaty</div></div></div>""")
                    templateCard("lower-third", "Lower Third", "info",
                        "Name + title bar with slide-in animation",
                        """<div class="tp-lower-third"><div class="tp-lt-name">John Smith</div><div class="tp-lt-title">Chief Technology Officer</div></div>""")
                    templateCard("clock", "Clock", "time",
                        "Current time display",
                        """<div class="tp-clock" id="showcase-clock">14:30:05</div>""")
                    templateCard("countdown", "Countdown", "time",
                        "Countdown timer to a target time",
                        """<div class="tp-countdown"><span class="tp-cd-num">02</span><span class="tp-cd-sep">:</span><span class="tp-cd-num">45</span><span class="tp-cd-sep">:</span><span class="tp-cd-num">30</span></div>""")
                    templateCard("score", "Score", "sports",
                        "Sports scoreboard with teams, score and period",
                        """<div class="tp-score"><div class="tp-score-team"><span class="tp-team-flag">🔵</span>Team A</div><div class="tp-score-nums"><span>2</span><span class="tp-score-sep">:</span><span>1</span></div><div class="tp-score-team">Team B<span class="tp-team-flag">🔴</span></div><div class="tp-score-period">2nd Half — 67'</div></div>""")
                    templateCard("reactions", "Reactions", "interactive",
                        "Emoji reaction overlay",
                        """<div class="tp-reactions"><span class="tp-react">👍</span><span class="tp-react tp-react-2">❤️</span><span class="tp-react tp-react-3">😂</span><span class="tp-react tp-react-4">🎉</span><span class="tp-react tp-react-5">👏</span></div>""")
                }
            }
        }

        // Interactive Demos / Product Walkthrough
        section("section showcase-section") {
            id = "demos"
            div("section-inner") {
                div("section-header") {
                    h2 { +"How It Works" }
                    p { +"See the complete workflow from channel creation to live broadcast." }
                }
                div("walkthrough-grid") {
                    walkthroughStep(
                        step = "1",
                        title = "Create a Channel",
                        description = "Set up a broadcast channel with a name, schedule, and default overlay profile. One click to go live.",
                        demoId = "create-channel"
                    )
                    walkthroughStep(
                        step = "2",
                        title = "Upload & Schedule Content",
                        description = "Drag & drop images and videos. Build playlists with drag-and-drop timeline editor. Set start/end times.",
                        demoId = "schedule-content"
                    )
                    walkthroughStep(
                        step = "3",
                        title = "Configure Overlays",
                        description = "Pick from 15+ overlay templates. Connect data via webhooks, REST polling, or manual control panel.",
                        demoId = "configure-overlays"
                    )
                    walkthroughStep(
                        step = "4",
                        title = "Go Live & Monitor",
                        description = "Embed on any screen or website. Monitor device fleet in real-time. Get alerts and audit logs.",
                        demoId = "go-live"
                    )
                }
            }
        }

        // Use Cases
        section("section showcase-section showcase-usecases") {
            div("section-inner") {
                div("section-header") {
                    h2 { +"Built For Every Industry" }
                    p { +"From breaking news to corporate lobbies — one platform, endless possibilities." }
                }
                div("usecases-grid") {
                    useCaseCard("📺", "Broadcast & TV",
                        "Run 24/7 TV channels with automated scheduling, live overlays, and as-run logging for compliance.")
                    useCaseCard("🏢", "Corporate Displays",
                        "KPI dashboards, meeting room signage, and branded welcome screens with real-time data feeds.")
                    useCaseCard("🏟", "Sports & Events",
                        "Live scoreboards, audience reactions, countdown timers, and dynamic sponsor rotations.")
                    useCaseCard("🏥", "Healthcare",
                        "Waiting room queues, appointment displays, and wayfinding with live data integration.")
                    useCaseCard("🎓", "Education",
                        "Campus announcements, class schedules, event promotion, and emergency alerts.")
                    useCaseCard("🛍", "Retail",
                        "Promotional content, QR engagement, poll-driven campaigns, and menu boards.")
                }
            }
        }

        // CTA
        section("cta-section showcase-cta") {
            div("section-inner") {
                div("section-header") {
                    h2 { +"Ready to Transform Your Screens?" }
                    p { +"Start free. Deploy in minutes. Scale to thousands of screens." }
                }
                div("showcase-cta-actions") {
                    a(href = "/signup", classes = "btn btn-primary btn-lg") { +"Start Free Trial" }
                    a(href = "/contact", classes = "btn btn-ghost btn-lg") { +"Talk to Sales" }
                }
            }
        }

        // Showcase JS
        script {
            unsafe {
                +showcaseScript()
            }
        }
    }
}

private fun FlowContent.demoChannelCard(
    id: String, name: String, description: String,
    category: String, overlays: List<String>, color: String
) {
    a(href = "/showcase/demo/$id", classes = "demo-channel-card") {
        div("demo-channel-preview") {
            style = "background: linear-gradient(135deg, ${color}11, ${color}22); position: relative"
            div("demo-channel-screen") {
                style = "position: relative; overflow: hidden"
                // Mini TV with actual overlay widgets
                div("demo-screen-bg") {
                    style = "background: linear-gradient(160deg, #0f0f1a, #1a1a2e); width: 100%; height: 100%; position: absolute; inset: 0"
                }
                // Live badge
                div("demo-screen-live-bar") {
                    style = "position: absolute; top: 8px; left: 8px; z-index: 2; display: flex; align-items: center; gap: 6px"
                    span("demo-screen-badge") {
                        style = "background: $color"
                        +"LIVE"
                    }
                    span("demo-screen-name") { +name }
                }
                // Mini overlays based on channel type
                unsafe {
                    +demoChannelMiniOverlays(id, color)
                }
            }
        }
        div("demo-channel-info") {
            div("demo-channel-meta") {
                span("demo-channel-category") {
                    style = "color: $color"
                    +category
                }
                span("demo-channel-arrow") { +"\u2192" }
            }
            h3("demo-channel-name") { +name }
            p("demo-channel-desc") { +description }
            div("demo-channel-overlays") {
                overlays.forEach { overlay ->
                    span("badge badge-neutral") { +overlay }
                }
            }
        }
    }
}

private fun demoChannelMiniOverlays(id: String, color: String): String = when (id) {
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
    "corporate-lobby" -> """
        <div style="position:absolute;top:10px;right:8px;display:grid;grid-template-columns:1fr 1fr;gap:3px;z-index:2">
            <div style="background:rgba(0,0,0,.6);border-radius:3px;padding:4px 8px;text-align:center"><div style="font-size:12px;font-weight:700;color:#3b82f6">$2.4M</div><div style="font-size:6px;color:rgba(255,255,255,.4);text-transform:uppercase">Revenue</div></div>
            <div style="background:rgba(0,0,0,.6);border-radius:3px;padding:4px 8px;text-align:center"><div style="font-size:12px;font-weight:700;color:#22c55e">+18%</div><div style="font-size:6px;color:rgba(255,255,255,.4);text-transform:uppercase">Growth</div></div>
        </div>
        <div style="position:absolute;bottom:20px;left:0;z-index:2"><div style="background:#fff;color:#000;font-weight:700;font-size:9px;padding:3px 10px">Sarah Chen</div><div style="background:rgba(0,0,0,.8);color:#fff;font-size:7px;padding:2px 10px">VP Operations</div></div>
        <div style="position:absolute;top:10px;left:8px;background:rgba(0,0,0,.6);border-radius:3px;padding:3px 6px;font-size:10px;font-weight:600;color:#fff;font-variant-numeric:tabular-nums;z-index:2">09:45</div>
    """
    "retail-signage" -> """
        <div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);background:rgba(0,0,0,.7);border-radius:6px;padding:8px 14px;text-align:center;z-index:2">
            <div style="font-size:7px;text-transform:uppercase;color:$color;font-weight:700;letter-spacing:1px">Flash Sale Ends In</div>
            <div style="font-size:18px;font-weight:700;color:#fff;font-variant-numeric:tabular-nums;margin-top:2px">02:45:30</div>
        </div>
        <div style="position:absolute;bottom:0;left:0;right:0;height:20px;background:rgba(0,0,0,.85);display:flex;align-items:center;z-index:2"><span style="background:$color;color:#fff;font-size:7px;font-weight:700;padding:0 6px;height:100%;display:flex;align-items:center">DEALS</span><span style="font-size:8px;color:#fff;padding-left:6px">Electronics 40% OFF | Fashion BOGO | Home 25% OFF</span></div>
    """
    "event-stream" -> """
        <div style="position:absolute;bottom:24px;left:0;z-index:2"><div style="background:#fff;color:#000;font-weight:700;font-size:9px;padding:3px 10px">Dr. Sarah Chen</div><div style="background:rgba(0,0,0,.8);color:#fff;font-size:7px;padding:2px 10px">Keynote Speaker</div></div>
        <div style="position:absolute;top:8px;right:8px;background:rgba(0,0,0,.6);border-radius:4px;padding:4px 8px;text-align:center;z-index:2"><div style="font-size:6px;color:$color;text-transform:uppercase;font-weight:600">Next Session</div><div style="font-size:12px;font-weight:700;color:#fff">15:00</div></div>
        <div style="position:absolute;bottom:0;left:0;right:0;height:20px;background:rgba(0,0,0,.85);display:flex;align-items:center;z-index:2"><span style="background:$color;color:#fff;font-size:7px;font-weight:700;padding:0 6px;height:100%;display:flex;align-items:center">Q&A</span><span style="font-size:8px;color:#fff;padding-left:6px">Submit questions via app or scan QR code</span></div>
    """
    "education-tv" -> """
        <div style="position:absolute;top:8px;right:8px;display:flex;flex-direction:column;gap:2px;z-index:2">
            <div style="background:rgba(0,0,0,.6);border-radius:3px;padding:2px 6px;font-size:8px;color:#fff;display:flex;align-items:center;gap:4px"><span style="width:5px;height:5px;border-radius:50%;background:#22c55e;display:inline-block"></span>Math 101 — Now</div>
            <div style="background:rgba(0,0,0,.4);border-radius:3px;padding:2px 6px;font-size:8px;color:rgba(255,255,255,.5)">Physics — 10:30</div>
            <div style="background:rgba(0,0,0,.4);border-radius:3px;padding:2px 6px;font-size:8px;color:rgba(255,255,255,.5)">History — 11:15</div>
        </div>
        <div style="position:absolute;top:8px;left:8px;background:$color;border-radius:3px;padding:2px 6px;font-size:7px;font-weight:700;color:#fff;z-index:2">EDU</div>
        <div style="position:absolute;bottom:0;left:0;right:0;height:20px;background:rgba(0,0,0,.85);display:flex;align-items:center;z-index:2"><span style="background:$color;color:#fff;font-size:7px;font-weight:700;padding:0 6px;height:100%;display:flex;align-items:center">NEWS</span><span style="font-size:8px;color:#fff;padding-left:6px">Campus library closes early at 5PM today</span></div>
    """
    else -> ""
}

private fun FlowContent.templateCategoryPill(category: String, label: String, active: Boolean = false) {
    button(classes = "template-pill${if (active) " active" else ""}") {
        attributes["data-category"] = category
        attributes["onclick"] = "filterTemplates('$category')"
        +label
    }
}

private fun FlowContent.templateCard(code: String, name: String, category: String, description: String, previewHtml: String) {
    div("template-card") {
        attributes["data-category"] = category
        div("template-preview") {
            div("template-preview-inner") {
                unsafe { +previewHtml }
            }
        }
        div("template-info") {
            div("template-meta") {
                span("badge badge-neutral") { +category }
                span("template-code") { +code }
            }
            h4("template-name") { +name }
            p("template-desc") { +description }
        }
    }
}

private fun FlowContent.walkthroughStep(step: String, title: String, description: String, demoId: String) {
    div("walkthrough-step") {
        div("walkthrough-step-num") { +step }
        div("walkthrough-step-content") {
            h3 { +title }
            p { +description }
        }
        div("walkthrough-demo") {
            id = "demo-$demoId"
            // Interactive demo placeholder with animated simulation
            when (demoId) {
                "create-channel" -> {
                    div("mini-demo") {
                        div("mini-demo-toolbar") {
                            span("mini-demo-dot red") {}
                            span("mini-demo-dot yellow") {}
                            span("mini-demo-dot green") {}
                            span("mini-demo-title") { +"New Channel" }
                        }
                        div("mini-demo-body") {
                            div("mini-demo-field") {
                                span("mini-demo-label") { +"Channel Name" }
                                div("mini-demo-input") {
                                    id = "demo-typing-channel"
                                    +"News 24|"
                                }
                            }
                            div("mini-demo-field") {
                                span("mini-demo-label") { +"Status" }
                                div("mini-demo-select") { +"Active ✓" }
                            }
                            div("mini-demo-btn") { +"Create Channel →" }
                        }
                    }
                }
                "schedule-content" -> {
                    div("mini-demo") {
                        div("mini-demo-toolbar") {
                            span("mini-demo-dot red") {}
                            span("mini-demo-dot yellow") {}
                            span("mini-demo-dot green") {}
                            span("mini-demo-title") { +"Schedule Editor" }
                        }
                        div("mini-demo-body mini-demo-timeline") {
                            div("timeline-track") {
                                div("timeline-item ti-1") {
                                    style = "width: 30%; background: #3b82f6"
                                    +"intro.mp4"
                                }
                                div("timeline-item ti-2") {
                                    style = "width: 45%; background: #8b5cf6"
                                    +"slides.jpg"
                                }
                                div("timeline-item ti-3") {
                                    style = "width: 25%; background: #f59e0b"
                                    +"promo.mp4"
                                }
                            }
                            div("timeline-cursor") {}
                        }
                    }
                }
                "configure-overlays" -> {
                    div("mini-demo") {
                        div("mini-demo-toolbar") {
                            span("mini-demo-dot red") {}
                            span("mini-demo-dot yellow") {}
                            span("mini-demo-dot green") {}
                            span("mini-demo-title") { +"Overlay Config" }
                        }
                        div("mini-demo-body mini-demo-overlay-cfg") {
                            div("overlay-cfg-widget") {
                                span("overlay-cfg-icon") { +"📰" }
                                span { +"Ticker" }
                                span("overlay-cfg-toggle on") { +"ON" }
                            }
                            div("overlay-cfg-widget") {
                                span("overlay-cfg-icon") { +"🌤" }
                                span { +"Weather" }
                                span("overlay-cfg-toggle on") { +"ON" }
                            }
                            div("overlay-cfg-widget") {
                                span("overlay-cfg-icon") { +"🕐" }
                                span { +"Clock" }
                                span("overlay-cfg-toggle") { +"OFF" }
                            }
                        }
                    }
                }
                "go-live" -> {
                    div("mini-demo") {
                        div("mini-demo-toolbar") {
                            span("mini-demo-dot red") {}
                            span("mini-demo-dot yellow") {}
                            span("mini-demo-dot green") {}
                            span("mini-demo-title") { +"Live Monitor" }
                        }
                        div("mini-demo-body mini-demo-monitor") {
                            div("monitor-device") {
                                div("monitor-status online") {}
                                span { +"Lobby Screen #1" }
                                span("monitor-badge") { +"Online" }
                            }
                            div("monitor-device") {
                                div("monitor-status online") {}
                                span { +"Conference Room" }
                                span("monitor-badge") { +"Online" }
                            }
                            div("monitor-device") {
                                div("monitor-status offline") {}
                                span { +"Reception TV" }
                                span("monitor-badge warn") { +"Offline" }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun FlowContent.useCaseCard(emoji: String, title: String, description: String) {
    div("usecase-card") {
        div("usecase-icon") { +emoji }
        h3 { +title }
        p { +description }
    }
}

private fun showcaseScript(): String = """
// Template category filter
function filterTemplates(category) {
    document.querySelectorAll('.template-pill').forEach(function(p) {
        p.classList.toggle('active', p.dataset.category === category);
    });
    document.querySelectorAll('.template-card').forEach(function(c) {
        if (category === 'all' || c.dataset.category === category) {
            c.style.display = '';
        } else {
            c.style.display = 'none';
        }
    });
}

// Live clock in showcase
(function() {
    var el = document.getElementById('showcase-clock');
    if (!el) return;
    function tick() {
        var now = new Date();
        el.textContent = now.toLocaleTimeString('en-GB', {hour:'2-digit',minute:'2-digit',second:'2-digit',hour12:false});
    }
    tick();
    setInterval(tick, 1000);
})();

// Typing animation for demo
(function() {
    var el = document.getElementById('demo-typing-channel');
    if (!el) return;
    var texts = ['News 24', 'Sports Live', 'Corporate TV', 'Retail Display'];
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

// Smooth scroll for anchor links
document.querySelectorAll('a[href^="#"]').forEach(function(a) {
    a.addEventListener('click', function(e) {
        var target = document.querySelector(this.getAttribute('href'));
        if (target) { e.preventDefault(); target.scrollIntoView({behavior:'smooth', block:'start'}); }
    });
});

// Timeline cursor animation
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
