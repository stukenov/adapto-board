package com.playoutedge.server.views.home

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.detailLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import com.playoutedge.server.views.icon
import com.playoutedge.server.views.components.hxPolling
import kotlinx.html.*

/**
 * Main dashboard view with improved widgets.
 */
fun HTML.homeView(
    session: AdminClaims,
    fleetHealth: FleetHealth,
    alerts: List<AlertSummary>,
    contentStats: ContentStats? = null,
    recentActivity: List<RecentActivity> = emptyList()
) {
    adminLayout(title = "Dashboard", userName = session.displayName, currentPath = "/admin") {
        // Page header
        pageHeader(
            title = "Dashboard",
            subtitle = "Monitor your fleet and manage content"
        )

        // Stats cards row
        div("stats-grid mb-4") {
            // Total devices
            div("stat-card") {
                div("stat-card-header") {
                    div("stat-icon icon-primary") { icon("monitor") }
                }
                span("stat-label") { +"Total Devices" }
                span("stat-value") { +"${fleetHealth.totalCount}" }
            }

            // Online devices
            div("stat-card") {
                div("stat-card-header") {
                    div("stat-icon icon-success") { icon("check-circle") }
                }
                span("stat-label") { +"Online" }
                span("stat-value text-success") { +"${fleetHealth.onlineCount}" }
            }

            // Channels
            div("stat-card") {
                div("stat-card-header") {
                    div("stat-icon icon-info") { icon("hash") }
                }
                span("stat-label") { +"Channels" }
                span("stat-value") { +"${contentStats?.channelCount ?: 0}" }
            }

            // Assets
            div("stat-card") {
                div("stat-card-header") {
                    div("stat-icon icon-warning") { icon("folder") }
                }
                span("stat-label") { +"Assets" }
                span("stat-value") { +"${contentStats?.assetCount ?: 0}" }
            }

            // Storage
            div("stat-card") {
                div("stat-card-header") {
                    div("stat-icon icon-secondary") { icon("database") }
                }
                span("stat-label") { +"Storage Used" }
                span("stat-value") { +(contentStats?.storageDisplay ?: "0 MB") }
            }

            // Online rate
            div("stat-card") {
                div("stat-card-header") {
                    div("stat-icon icon-info") { icon("trending-up") }
                }
                span("stat-label") { +"Uptime" }
                span("stat-value ${if (fleetHealth.onlineRate >= 80) "text-success" else "text-warning"}") {
                    +"${fleetHealth.onlineRate}%"
                }
            }
        }

        // Main content: 2-column layout
        detailLayout(
            main = {
                hxPolling(url = "/admin/fragments/fleet-health", id = "fleet-health", intervalSeconds = 30) {
                    fleetHealthWidget(fleetHealth)
                }
                recentActivityWidget(recentActivity)
            },
            sidebar = {
                hxPolling(url = "/admin/fragments/alerts", id = "alerts-widget", intervalSeconds = 30) {
                    alertsWidget(alerts)
                }
                quickActionsWidget()
            }
        )
    }
}

/**
 * Recent activity widget showing latest audit entries.
 */
fun FlowContent.recentActivityWidget(activity: List<RecentActivity>) {
    div("card") {
        div("card-header") {
            h3 { +"Recent Activity" }
            a(href = "/admin/reports/audit", classes = "btn btn-sm btn-secondary") { +"View All" }
        }
        div("card-body") {
            if (activity.isEmpty()) {
                div("text-center p-4") {
                    p("text-muted") { +"No recent activity" }
                }
            } else {
                ul("alert-list") {
                    activity.forEach { item ->
                        li("alert-item") {
                            div("alert-item-content") {
                                span("alert-type") { +"${item.action} ${item.entityType}" }
                                p("alert-message") {
                                    +(item.actorName ?: "System")
                                    +" · "
                                    +item.createdAt.toString().take(16).replace("T", " ")
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
 * Fleet health widget showing online/offline devices with visual indicator.
 */
fun FlowContent.fleetHealthWidget(health: FleetHealth) {
    div("card") {
        div("card-header") {
            h3 { +"Fleet Health" }
            a(href = "/admin/devices", classes = "btn btn-sm btn-secondary") { +"View All" }
        }
        div("card-body") {
            // Progress bar showing online rate
            div("quota-info mb-4") {
                div("quota-label") {
                    span { +"${health.onlineCount} of ${health.totalCount} devices online" }
                    span { +"${health.onlineRate}%" }
                }
                div("quota-bar") {
                    val barClass = when {
                        health.onlineRate >= 80 -> ""
                        health.onlineRate >= 50 -> "warning"
                        else -> "danger"
                    }
                    div("quota-fill $barClass") {
                        style = "width: ${health.onlineRate}%"
                    }
                }
            }

            // Offline devices list
            if (health.offlineDevices.isNotEmpty()) {
                div("offline-list") {
                    h4 { +"Needs Attention" }
                    ul {
                        health.offlineDevices.take(5).forEach { device ->
                            li {
                                a(href = "/admin/devices/${device.id}") { +device.name }
                                device.lastSeen?.let {
                                    span("text-muted text-sm") { +" · last seen $it" }
                                }
                            }
                        }
                        if (health.offlineDevices.size > 5) {
                            li {
                                a(href = "/admin/devices?status=offline", classes = "link") {
                                    +"View all ${health.offlineDevices.size} offline devices →"
                                }
                            }
                        }
                    }
                }
            } else if (health.totalCount > 0) {
                div("text-center p-4") {
                    div("text-success text-2xl mb-2") { icon("check-circle") }
                    p("text-muted") { +"All devices are online" }
                }
            } else {
                div("text-center p-4") {
                    p("text-muted") { +"No devices enrolled yet" }
                    a(href = "/admin/devices/enroll", classes = "btn btn-primary btn-sm mt-2") {
                        +"Add Device"
                    }
                }
            }
        }
    }
}

/**
 * Alerts widget showing open alerts with icons.
 */
fun FlowContent.alertsWidget(alerts: List<AlertSummary>) {
    div("card") {
        div("card-header") {
            h3 {
                +"Alerts"
                if (alerts.isNotEmpty()) {
                    span("badge badge-warning badge-plain ml-2") { +"${alerts.size}" }
                }
            }
            if (alerts.isNotEmpty()) {
                a(href = "/admin/alerts", classes = "btn btn-sm btn-secondary") { +"View All" }
            }
        }
        div("card-body") {
            if (alerts.isEmpty()) {
                div("text-center p-4") {
                    div("text-success text-2xl mb-2") { icon("check-circle") }
                    p("text-muted") { +"No open alerts" }
                }
            } else {
                ul("alert-list") {
                    alerts.take(5).forEach { alert ->
                        val iconName = when (alert.type.lowercase()) {
                            "device_offline" -> "exclamation"
                            "publish_failed" -> "x-circle"
                            "storage_warning" -> "database"
                            else -> "exclamation"
                        }
                        li("alert-item alert-${alert.type.lowercase()}") {
                            span("alert-item-icon") { icon(iconName) }
                            div("alert-item-content") {
                                span("alert-type") { +alert.type.replace("_", " ") }
                                p("alert-message") { +alert.message }
                            }
                        }
                    }
                }
                if (alerts.size > 5) {
                    div("text-center mt-3") {
                        a(href = "/admin/alerts", classes = "link") {
                            +"View all ${alerts.size} alerts →"
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quick actions widget with prominent primary action.
 */
fun FlowContent.quickActionsWidget() {
    div("card") {
        div("card-header") {
            h3 { +"Quick Actions" }
        }
        div("card-body") {
            div("quick-actions-grid") {
                // Action cards
                a(href = "/admin/assets/upload", classes = "action-card") {
                    div("action-card-icon icon-primary") { +"+" }
                    div("action-card-content") {
                        span("action-card-title") { +"Upload Asset" }
                        span("action-card-desc") { +"Add new media content" }
                    }
                }
                a(href = "/admin/channels/new", classes = "action-card") {
                    div("action-card-icon icon-info") { icon("hash") }
                    div("action-card-content") {
                        span("action-card-title") { +"Create Channel" }
                        span("action-card-desc") { +"Set up a new channel" }
                    }
                }
                a(href = "/admin/devices/enroll", classes = "action-card action-card-primary") {
                    div("action-card-icon icon-success") { +"+" }
                    div("action-card-content") {
                        span("action-card-title") { +"Enroll Device" }
                        span("action-card-desc") { +"Add a new device to fleet" }
                    }
                }
                a(href = "/admin/overlay/profiles/new", classes = "action-card") {
                    div("action-card-icon icon-warning") { +"L" }
                    div("action-card-content") {
                        span("action-card-title") { +"New Overlay" }
                        span("action-card-desc") { +"Create overlay profile" }
                    }
                }
            }
        }
    }
}
