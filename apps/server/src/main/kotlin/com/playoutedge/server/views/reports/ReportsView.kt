package com.playoutedge.server.views.reports

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.AsrunEventType
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import com.playoutedge.server.views.statCard
import com.playoutedge.server.views.icon
import kotlinx.html.*

/**
 * Reports main page with tabs and improved UX.
 */
fun HTML.reportsMainView(
    session: AdminClaims,
    activeTab: String = "asrun"
) {
    adminLayout(title = "Reports", userName = session.displayName, currentPath = "/admin/reports") {
        pageHeader(
            title = "Reports",
            subtitle = "Analyze playback data and system activity"
        )

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
 * As-run reports view with improved UX.
 */
fun HTML.asrunReportsView(
    session: AdminClaims,
    events: List<AsrunEventItem>,
    summary: AsrunSummaryView,
    filters: AsrunFiltersView,
    devices: List<DeviceFilterOption>,
    channels: List<ChannelFilterOption>,
    playbackByDay: List<ChartDataPoint> = emptyList(),
    topAssets: List<ChartDataPoint> = emptyList(),
    activePreset: String? = null
) {
    adminLayout(title = "As-Run Reports", userName = session.displayName, currentPath = "/admin/reports") {
        pageHeader(
            title = "As-Run Reports",
            subtitle = "Track what content played and when"
        ) {
            a(href = "/admin/reports/asrun/export?${buildFilterParams(filters)}", classes = "btn btn-secondary") {
                +"Export CSV"
            }
        }

        // Tabs
        div("tabs mb-4") {
            a(href = "/admin/reports/asrun", classes = "tab active") { +"As-Run Reports" }
            a(href = "/admin/reports/audit", classes = "tab") { +"Audit Log" }
        }

        // Date preset buttons
        div("date-presets") {
            a(href = "/admin/reports/asrun?preset=last7days", classes = if (activePreset == "last7days") "active" else "") {
                +"Last 7 days"
            }
            a(href = "/admin/reports/asrun?preset=last30days", classes = if (activePreset == "last30days") "active" else "") {
                +"Last 30 days"
            }
            a(href = "/admin/reports/asrun?preset=thisMonth", classes = if (activePreset == "thisMonth") "active" else "") {
                +"This month"
            }
            a(href = "/admin/reports/asrun?preset=lastMonth", classes = if (activePreset == "lastMonth") "active" else "") {
                +"Last month"
            }
        }

        // CSS bar charts
        if (playbackByDay.isNotEmpty() || topAssets.isNotEmpty()) {
            div("card mb-4") {
                div("card-body") {
                    if (playbackByDay.isNotEmpty()) {
                        div("chart-container") {
                            h4 { +"Playback by Day" }
                            playbackByDay.forEach { point ->
                                div("chart-bar-row") {
                                    span("chart-bar-label") { +point.label.takeLast(5) }
                                    div("chart-bar-track") {
                                        div("chart-bar-fill") {
                                            style = "width: ${point.percentage}%"
                                        }
                                    }
                                    span("chart-bar-value") { +"${point.value}" }
                                }
                            }
                        }
                    }
                    if (topAssets.isNotEmpty()) {
                        div("chart-container") {
                            h4 { +"Top Assets" }
                            topAssets.forEach { point ->
                                div("chart-bar-row") {
                                    span("chart-bar-label") {
                                        title = point.label
                                        +(if (point.label.length > 15) point.label.take(15) + "..." else point.label)
                                    }
                                    div("chart-bar-track") {
                                        div("chart-bar-fill") {
                                            style = "width: ${point.percentage}%"
                                        }
                                    }
                                    span("chart-bar-value") { +"${point.value}" }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Summary cards
        div("stats-grid mb-4") {
            div("stat-card") {
                div("stat-icon") { icon("chart-bar") }
                div("stat-content") {
                    div("stat-value") { +"${summary.totalEvents}" }
                    div("stat-label") { +"Total Events" }
                }
            }
            div("stat-card") {
                div("stat-icon") { icon("film") }
                div("stat-content") {
                    div("stat-value") { +"${summary.uniqueAssets}" }
                    div("stat-label") { +"Unique Assets" }
                }
            }
            div("stat-card") {
                div("stat-icon") { icon("clock") }
                div("stat-content") {
                    div("stat-value") { +summary.totalPlayTime }
                    div("stat-label") { +"Total Play Time" }
                }
            }
        }

        // Filters
        div("card mb-4") {
            form(action = "/admin/reports/asrun", method = FormMethod.get, classes = "filter-form") {
                div("filter-row") {
                    div("form-group") {
                        label {
                            htmlFor = "filter-device"
                            +"Device"
                        }
                        select("form-control") {
                            id = "filter-device"
                            name = "deviceId"
                            option {
                                value = ""
                                if (filters.deviceId == null) selected = true
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
                        label {
                            htmlFor = "filter-channel"
                            +"Channel"
                        }
                        select("form-control") {
                            id = "filter-channel"
                            name = "channelId"
                            option {
                                value = ""
                                if (filters.channelId == null) selected = true
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
                        label {
                            htmlFor = "filter-event-type"
                            +"Event Type"
                        }
                        select("form-control") {
                            id = "filter-event-type"
                            name = "eventType"
                            option {
                                value = ""
                                if (filters.eventType == null) selected = true
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
                        label {
                            htmlFor = "filter-from-date"
                            +"From"
                        }
                        input(type = InputType.date, classes = "form-control") {
                            id = "filter-from-date"
                            name = "fromDate"
                            value = filters.fromDate ?: ""
                        }
                    }
                    div("form-group") {
                        label {
                            htmlFor = "filter-to-date"
                            +"To"
                        }
                        input(type = InputType.date, classes = "form-control") {
                            id = "filter-to-date"
                            name = "toDate"
                            value = filters.toDate ?: ""
                        }
                    }
                    div("filter-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-secondary") {
                            +"Apply"
                        }
                        if (filters.hasActiveFilters()) {
                            a(href = "/admin/reports/asrun", classes = "btn btn-ghost") {
                                +"Clear"
                            }
                        }
                    }
                }
            }
        }

        // Events table
        div("card") {
            if (events.isEmpty()) {
                emptyState(
                    icon = "chart-bar",
                    title = "No events found",
                    description = if (filters.hasActiveFilters())
                        "Try adjusting your filters to see more results."
                    else
                        "Playback events will appear here once devices start playing content."
                )
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
                                td {
                                    span("text-muted") {
                                        +event.at.toString().replace("T", " ").substringBeforeLast(":")
                                    }
                                }
                                td {
                                    a(href = "/admin/reports/asrun?deviceId=${event.deviceId}", classes = "font-medium") {
                                        title = "Filter by this device"
                                        +event.deviceName
                                    }
                                }
                                td {
                                    event.channelName?.let {
                                        a(href = "/admin/reports/asrun?channelId=${event.channelId}") {
                                            title = "Filter by this channel"
                                            +it
                                        }
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
                div("card-header") {
                    h3 { +"Playback by Asset" }
                    span("badge badge-gray badge-plain") { +"${summary.byAsset.size}" }
                }
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
                                    a(href = "/admin/assets/${asset.assetId}", classes = "font-medium") {
                                        +asset.assetName
                                    }
                                }
                                td {
                                    span("badge badge-info badge-plain") { +"${asset.playCount}" }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun AsrunFiltersView.hasActiveFilters(): Boolean =
    deviceId != null || channelId != null || eventType != null || !fromDate.isNullOrBlank() || !toDate.isNullOrBlank()

/**
 * Audit log view with improved UX.
 */
fun HTML.auditLogView(
    session: AdminClaims,
    logs: List<AuditLogItem>,
    filters: AuditLogFilters
) {
    adminLayout(title = "Audit Log", userName = session.displayName, currentPath = "/admin/reports") {
        pageHeader(
            title = "Audit Log",
            subtitle = "Track all changes and system activity"
        ) {
            a(href = "/admin/reports/audit/export?${buildAuditFilterParams(filters)}", classes = "btn btn-secondary") {
                +"Export CSV"
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
                        label {
                            htmlFor = "audit-filter-entity"
                            +"Entity Type"
                        }
                        select("form-control") {
                            id = "audit-filter-entity"
                            name = "entityType"
                            option {
                                value = ""
                                if (filters.entityType == null) selected = true
                                +"All Types"
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
                        label {
                            htmlFor = "audit-filter-action"
                            +"Action"
                        }
                        select("form-control") {
                            id = "audit-filter-action"
                            name = "action"
                            option {
                                value = ""
                                if (filters.action == null) selected = true
                                +"All Actions"
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
                        label {
                            htmlFor = "audit-filter-from"
                            +"From"
                        }
                        input(type = InputType.date, classes = "form-control") {
                            id = "audit-filter-from"
                            name = "fromDate"
                            value = filters.fromDate ?: ""
                        }
                    }
                    div("form-group") {
                        label {
                            htmlFor = "audit-filter-to"
                            +"To"
                        }
                        input(type = InputType.date, classes = "form-control") {
                            id = "audit-filter-to"
                            name = "toDate"
                            value = filters.toDate ?: ""
                        }
                    }
                    div("filter-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-secondary") {
                            +"Apply"
                        }
                        if (filters.hasActiveFilters()) {
                            a(href = "/admin/reports/audit", classes = "btn btn-ghost") {
                                +"Clear"
                            }
                        }
                    }
                }
            }
        }

        // Audit log table
        div("card") {
            if (logs.isEmpty()) {
                emptyState(
                    icon = "pencil",
                    title = "No audit entries",
                    description = if (filters.hasActiveFilters())
                        "Try adjusting your filters to see more results."
                    else
                        "System activity will be logged here as changes are made."
                )
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
                                td {
                                    span("text-muted") {
                                        +log.createdAt.toString().replace("T", " ").substringBeforeLast(":")
                                    }
                                }
                                td {
                                    log.actorName?.let {
                                        span("font-medium") { +it }
                                    } ?: span("text-muted") { +"System" }
                                    small("text-muted ml-1") { +"(${log.actorType})" }
                                }
                                td {
                                    span("badge badge-${actionBadge(log.action)}") {
                                        +log.action
                                    }
                                }
                                td {
                                    span("badge badge-gray badge-plain") { +log.entityType }
                                    code("text-muted ml-1") { +log.entityId.toString().take(8) }
                                }
                                td {
                                    if (log.hasDiff) {
                                        span("diff-toggle") {
                                            attributes["onclick"] = "var el=this.nextElementSibling;el.classList.toggle('open');this.textContent=el.classList.contains('open')?'Hide changes':'View changes'"
                                            +"View changes"
                                        }
                                        div("diff-content") {
                                            id = "diff-${log.id}"
                                            +"Loading..."
                                            attributes["data-diff-url"] = "/admin/reports/audit/${log.id}/diff"
                                            attributes["onclick"] = ""
                                        }
                                        script {
                                            unsafe {
                                                +"document.querySelector('#diff-${log.id}').parentElement.querySelector('.diff-toggle').addEventListener('click',function(){var d=document.getElementById('diff-${log.id}');if(d.dataset.loaded)return;fetch(d.dataset.diffUrl).then(r=>r.text()).then(t=>{d.innerHTML=t;d.dataset.loaded='1'}).catch(()=>{d.textContent='Could not load diff'})},{ once: false })"
                                            }
                                        }
                                    } else {
                                        span("text-muted") { +"--" }
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

fun AuditLogFilters.hasActiveFilters(): Boolean =
    entityType != null || action != null || !fromDate.isNullOrBlank() || !toDate.isNullOrBlank()

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
