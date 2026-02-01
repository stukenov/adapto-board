package com.playoutedge.server.views.home

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*

/**
 * Main dashboard view.
 */
fun HTML.homeView(
    session: AdminClaims,
    fleetHealth: FleetHealth,
    alerts: List<AlertSummary>
) {
    adminLayout(title = "Dashboard", userName = "Admin") {
        div("dashboard-grid") {
            // Fleet Health Widget
            fleetHealthWidget(fleetHealth)

            // Alerts Widget
            alertsWidget(alerts)

            // Quick Actions Widget
            quickActionsWidget()
        }
    }
}

/**
 * Fleet health widget showing online/offline devices.
 */
fun FlowContent.fleetHealthWidget(health: FleetHealth) {
    div("card") {
        div("card-header") {
            h3 { +"Fleet Health" }
        }
        div("card-body") {
            div("stat-row") {
                div("stat") {
                    span("stat-value") { +"${health.onlineCount}" }
                    span("stat-label") { +"Online" }
                }
                div("stat") {
                    span("stat-value") { +"${health.totalCount}" }
                    span("stat-label") { +"Total" }
                }
                div("stat") {
                    span("stat-value ${if (health.onlineRate >= 80) "text-success" else "text-warning"}") {
                        +"${health.onlineRate}%"
                    }
                    span("stat-label") { +"Online Rate" }
                }
            }

            if (health.offlineDevices.isNotEmpty()) {
                div("offline-list") {
                    h4 { +"Offline Devices" }
                    ul {
                        health.offlineDevices.take(5).forEach { device ->
                            li {
                                a(href = "/admin/devices/${device.id}") { +device.name }
                                device.lastSeen?.let {
                                    span("text-muted") { +" (last seen: ${it})" }
                                }
                            }
                        }
                        if (health.offlineDevices.size > 5) {
                            li {
                                a(href = "/admin/devices?status=offline") {
                                    +"View all ${health.offlineDevices.size} offline devices"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Alerts widget showing open alerts.
 */
fun FlowContent.alertsWidget(alerts: List<AlertSummary>) {
    div("card") {
        div("card-header") {
            h3 { +"Alerts" }
            if (alerts.isNotEmpty()) {
                span("badge badge-warning") { +"${alerts.size}" }
            }
        }
        div("card-body") {
            if (alerts.isEmpty()) {
                div("empty-state") {
                    p { +"No open alerts" }
                }
            } else {
                ul("alert-list") {
                    alerts.take(5).forEach { alert ->
                        li("alert-item alert-${alert.type.lowercase()}") {
                            span("alert-type") { +alert.type }
                            span("alert-message") { +alert.message }
                        }
                    }
                }
                if (alerts.size > 5) {
                    a(href = "/admin/alerts", classes = "link") {
                        +"View all ${alerts.size} alerts"
                    }
                }
            }
        }
    }
}

/**
 * Quick actions widget.
 */
fun FlowContent.quickActionsWidget() {
    div("card") {
        div("card-header") {
            h3 { +"Quick Actions" }
        }
        div("card-body") {
            div("quick-actions") {
                a(href = "/admin/devices/enroll", classes = "btn btn-primary") {
                    +"Generate Enroll Code"
                }
                a(href = "/admin/channels/new", classes = "btn btn-secondary") {
                    +"Create Channel"
                }
                a(href = "/admin/devices", classes = "btn btn-secondary") {
                    +"View All Devices"
                }
            }
        }
    }
}
