package com.playoutedge.server.views.landing

import kotlinx.html.*

fun HTML.featuresView() {
    publicLayout("Features", currentPath = "/features") {
        section("section") {
            div("section-inner") {
                div("section-header") {
                    h1 { +"Features" }
                    p { +"Everything you need to run a professional digital signage network." }
                }
                div("feature-grid feature-grid-full") {
                    featureDetailCard(
                        "M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z",
                        "Channel Management",
                        "Create and manage content channels. Assign channels to devices and update content in real-time across your entire network."
                    )
                    featureDetailCard(
                        "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z",
                        "Smart Scheduling",
                        "Schedule content by time, date, or recurring patterns. Set up playlists that run automatically without manual intervention."
                    )
                    featureDetailCard(
                        "M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z",
                        "Device Fleet Management",
                        "Monitor device health, push updates remotely, and manage your entire fleet of screens from one dashboard."
                    )
                    featureDetailCard(
                        "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z",
                        "Asset Library",
                        "Upload images, videos, and HTML content. Organize assets with tags and folders for easy access."
                    )
                    featureDetailCard(
                        "M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01",
                        "Overlay System",
                        "Add dynamic overlays like tickers, weather, and alerts on top of your scheduled content via webhooks."
                    )
                    featureDetailCard(
                        "M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z",
                        "Reports & Analytics",
                        "Track playback logs, audit trails, and device uptime. Export reports for compliance and performance review."
                    )
                }
            }
        }
    }
}

private fun FlowContent.featureDetailCard(icon: String, title: String, description: String) {
    div("feature-card") {
        div("feature-icon") {
            unsafe {
                +"""<svg fill="none" stroke="currentColor" viewBox="0 0 24 24" width="32" height="32"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="$icon"></path></svg>"""
            }
        }
        h3 { +title }
        p { +description }
    }
}
