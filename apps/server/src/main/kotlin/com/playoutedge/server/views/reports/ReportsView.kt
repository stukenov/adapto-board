package com.playoutedge.server.views.reports

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.AsrunEventType
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*

/**
 * Reports main page with tabs.
 */
fun HTML.reportsMainView(
    session: AdminClaims,
    activeTab: String = "asrun"
) {
    adminLayout(title = "Reports", userName = "Admin") {
        div("page-header") {
            h1("page-title") { +"Reports" }
        }

        // Tabs
        div("tabs mb-4") {
            a(href = "/admin/reports/asrun", classes = "tab${if (activeTab == "asrun") " active" else ""}") {
                +"As-Run Reports"
            }
            a(href = "/admin/reports/audit", classes = "tab${if (activeTab == "audit") " active" else ""}") {
                +"Audit Log"
            }
        }
    }
}

/**
 * As-run reports view.
 */
fun HTML.asrunReportsView(
    session: AdminClaims,
    events: List<AsrunEventItem>,
    summary: AsrunSummaryView,
    filters: AsrunFiltersView,
    devices: List<DeviceFilterOption>,
    channels: List<ChannelFilterOption>
) {
    adminLayout(title = "As-Run Reports", userName = "Admin") {
        div("page-header") {
            h1("page-title") { +"As-Run Reports" }
            div("btn-group") {
                a(href = "/admin/reports/asrun/export?${buildFilterParams(filters)}", classes = "btn btn-secondary") {
                    +"Export CSV"
                }
            }
        }

        // Tabs
        div("tabs mb-4") {
            a(href = "/admin/reports/asrun", classes = "tab active") { +"As-Run Reports" }
            a(href = "/admin/reports/audit", classes = "tab") { +"Audit Log" }
        }

        // Summary cards
        div("stats-grid mb-4") {
            div("stat-card") {
                div("stat-value") { +"${summary.totalEvents}" }
                div("stat-label") { +"Total Events" }
            }
            div("stat-card") {
                div("stat-value") { +"${summary.uniqueAssets}" }
                div("stat-label") { +"Unique Assets" }
            }
            div("stat-card") {
                div("stat-value") { +summary.totalPlayTime }
                div("stat-label") { +"Total Play Time" }
            }
        }

        // Filters
        div("card mb-4") {
            form(action = "/admin/reports/asrun", method = FormMethod.get, classes = "filter-form") {
                div("filter-row") {
                    div("form-group") {
                        label { +"Device" }
                        select("form-control") {
                            name = "deviceId"
                            option {
                                value = ""
                                +"All Devices"
                            }
                            devices.forEach { device ->
                                option {
                                    value = device.id.toString()
                                    if (filters.deviceId == device.id) selected = true
                                    +device.name
                                }
                            }
                        }
                    }
                    div("form-group") {
                        label { +"Channel" }
                        select("form-control") {
                            name = "channelId"
                            option {
                                value = ""
                                +"All Channels"
                            }
                            channels.forEach { channel ->
                                option {
                                    value = channel.id.toString()
                                    if (filters.channelId == channel.id) selected = true
                                    +channel.name
                                }
                            }
                        }
                    }
                    div("form-group") {
                        label { +"Event Type" }
                        select("form-control") {
                            name = "eventType"
                            option {
                                value = ""
                                +"All Events"
                            }
                            AsrunEventType.entries.forEach { type ->
                                option {
                                    value = type.name
                                    if (filters.eventType == type) selected = true
                                    +type.name.replace("_", " ")
                                }
                            }
                        }
                    }
                    div("form-group") {
                        label { +"From" }
                        input(type = InputType.date, classes = "form-control") {
                            name = "fromDate"
                            value = filters.fromDate ?: ""
                        }
                    }
                    div("form-group") {
                        label { +"To" }
                        input(type = InputType.date, classes = "form-control") {
                            name = "toDate"
                            value = filters.toDate ?: ""
                        }
                    }
                    button(type = ButtonType.submit, classes = "btn btn-secondary") {
                        +"Filter"
                    }
                }
            }
        }

        // Events table
        div("card") {
            if (events.isEmpty()) {
                div("empty-state") {
                    div("empty-state-icon") { +"📊" }
                    p("empty-state-title") { +"No events found" }
                    p("empty-state-text") { +"Playback events will appear here once devices start playing content." }
                }
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Time" }
                            th { +"Device" }
                            th { +"Channel" }
                            th { +"Asset" }
                            th { +"Event" }
                        }
                    }
                    tbody {
                        events.forEach { event ->
                            tr {
                                td { +event.at.toString() }
                                td {
                                    a(href = "/admin/devices/${event.deviceId}") {
                                        +event.deviceName
                                    }
                                }
                                td {
                                    event.channelName?.let {
                                        a(href = "/admin/channels/${event.channelId}") { +it }
                                    } ?: span("text-muted") { +"—" }
                                }
                                td {
                                    event.assetName?.let {
                                        a(href = "/admin/assets/${event.assetId}") { +it }
                                    } ?: span("text-muted") { +"—" }
                                }
                                td {
                                    span("badge badge-${eventTypeBadge(event.eventType)}") {
                                        +event.eventType.name.replace("_", " ")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Asset breakdown
        if (summary.byAsset.isNotEmpty()) {
            div("card mt-4") {
                div("card-header") { +"Playback by Asset" }
                table("table") {
                    thead {
                        tr {
                            th { +"Asset" }
                            th { +"Play Count" }
                        }
                    }
                    tbody {
                        summary.byAsset.forEach { asset ->
                            tr {
                                td {
                                    a(href = "/admin/assets/${asset.assetId}") {
                                        +asset.assetName
                                    }
                                }
                                td { +"${asset.playCount}" }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Audit log view.
 */
fun HTML.auditLogView(
    session: AdminClaims,
    logs: List<AuditLogItem>,
    filters: AuditLogFilters
) {
    adminLayout(title = "Audit Log", userName = "Admin") {
        div("page-header") {
            h1("page-title") { +"Audit Log" }
            div("btn-group") {
                a(href = "/admin/reports/audit/export?${buildAuditFilterParams(filters)}", classes = "btn btn-secondary") {
                    +"Export CSV"
                }
            }
        }

        // Tabs
        div("tabs mb-4") {
            a(href = "/admin/reports/asrun", classes = "tab") { +"As-Run Reports" }
            a(href = "/admin/reports/audit", classes = "tab active") { +"Audit Log" }
        }

        // Filters
        div("card mb-4") {
            form(action = "/admin/reports/audit", method = FormMethod.get, classes = "filter-form") {
                div("filter-row") {
                    div("form-group") {
                        label { +"Entity Type" }
                        select("form-control") {
                            name = "entityType"
                            option {
                                value = ""
                                +"All"
                            }
                            ENTITY_TYPES.forEach { type ->
                                option {
                                    value = type
                                    if (filters.entityType == type) selected = true
                                    +type.replaceFirstChar { it.uppercase() }
                                }
                            }
                        }
                    }
                    div("form-group") {
                        label { +"Action" }
                        select("form-control") {
                            name = "action"
                            option {
                                value = ""
                                +"All"
                            }
                            ACTION_TYPES.forEach { action ->
                                option {
                                    value = action
                                    if (filters.action == action) selected = true
                                    +action.replaceFirstChar { it.uppercase() }
                                }
                            }
                        }
                    }
                    div("form-group") {
                        label { +"From" }
                        input(type = InputType.date, classes = "form-control") {
                            name = "fromDate"
                            value = filters.fromDate ?: ""
                        }
                    }
                    div("form-group") {
                        label { +"To" }
                        input(type = InputType.date, classes = "form-control") {
                            name = "toDate"
                            value = filters.toDate ?: ""
                        }
                    }
                    button(type = ButtonType.submit, classes = "btn btn-secondary") {
                        +"Filter"
                    }
                }
            }
        }

        // Audit log table
        div("card") {
            if (logs.isEmpty()) {
                div("empty-state") {
                    div("empty-state-icon") { +"📝" }
                    p("empty-state-title") { +"No audit entries" }
                    p("empty-state-text") { +"System activity will be logged here." }
                }
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Time" }
                            th { +"Actor" }
                            th { +"Action" }
                            th { +"Entity" }
                            th { +"Details" }
                        }
                    }
                    tbody {
                        logs.forEach { log ->
                            tr {
                                td { +log.createdAt.toString() }
                                td {
                                    log.actorName?.let { +it } ?: span("text-muted") { +"System" }
                                    small("text-muted") { +" (${log.actorType})" }
                                }
                                td {
                                    span("badge badge-${actionBadge(log.action)}") {
                                        +log.action
                                    }
                                }
                                td {
                                    +"${log.entityType} "
                                    small("text-muted") { +log.entityId.toString().take(8) }
                                }
                                td {
                                    if (log.hasDiff) {
                                        a(href = "/admin/reports/audit/${log.id}/diff", classes = "link") {
                                            +"View changes"
                                        }
                                    } else {
                                        span("text-muted") { +"—" }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun eventTypeBadge(type: AsrunEventType): String = when (type) {
    AsrunEventType.PLAY_START -> "success"
    AsrunEventType.PLAY_END -> "info"
    AsrunEventType.SKIP -> "warning"
    AsrunEventType.ERROR -> "danger"
    AsrunEventType.HEARTBEAT -> "gray"
}

private fun actionBadge(action: String): String = when (action.lowercase()) {
    "create" -> "success"
    "update" -> "info"
    "delete" -> "danger"
    "archive" -> "warning"
    else -> "gray"
}

private fun buildFilterParams(filters: AsrunFiltersView): String {
    val params = mutableListOf<String>()
    filters.deviceId?.let { params.add("deviceId=$it") }
    filters.channelId?.let { params.add("channelId=$it") }
    filters.eventType?.let { params.add("eventType=${it.name}") }
    filters.fromDate?.let { params.add("fromDate=$it") }
    filters.toDate?.let { params.add("toDate=$it") }
    return params.joinToString("&")
}

private fun buildAuditFilterParams(filters: AuditLogFilters): String {
    val params = mutableListOf<String>()
    filters.entityType?.let { params.add("entityType=$it") }
    filters.action?.let { params.add("action=$it") }
    filters.fromDate?.let { params.add("fromDate=$it") }
    filters.toDate?.let { params.add("toDate=$it") }
    return params.joinToString("&")
}
