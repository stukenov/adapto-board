package com.playoutedge.server.views.channels

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*
import java.util.UUID

fun HTML.scheduleGridView(
    session: AdminClaims,
    channelId: UUID,
    channelName: String,
    items: List<ScheduleEditorItem>,
    activeVersionNumber: Int?
) {
    adminLayout(title = "Schedule Grid - $channelName", userName = session.displayName, currentPath = "/admin/channels") {
        pageHeader(
            title = "Schedule Grid",
            subtitle = channelName,
            backHref = "/admin/channels/$channelId",
            backLabel = "Back to Channel"
        ) {
            if (activeVersionNumber != null) {
                span("badge badge-success mr-2") { +"Published v$activeVersionNumber" }
            }
            a(href = "/admin/channels/$channelId/schedule", classes = "btn btn-secondary btn-sm") {
                +"List View"
            }
        }

        // View toggle
        div("schedule-grid-controls") {
            div("schedule-grid-toggle") {
                button(classes = "btn btn-sm btn-primary grid-view-btn") {
                    id = "btn-timeline"
                    +"Timeline"
                }
                button(classes = "btn btn-sm btn-secondary grid-view-btn") {
                    id = "btn-weekgrid"
                    +"Week Grid"
                }
            }
            div("schedule-grid-legend") {
                span("grid-legend-item") {
                    span("grid-legend-color grid-legend-video") {}
                    +"Video"
                }
                span("grid-legend-item") {
                    span("grid-legend-color grid-legend-image") {}
                    +"Image"
                }
            }
        }

        // Timeline view
        div("card mb-4") {
            id = "timeline-view"
            div("card-header") {
                h3 { +"24-Hour Timeline" }
            }
            div("card-body") {
                div("schedule-timeline") {
                    // Hour markers
                    div("timeline-hours") {
                        for (h in 0..23) {
                            div("timeline-hour-marker") {
                                +String.format("%02d:00", h)
                            }
                        }
                    }
                    // Items on timeline
                    div("timeline-track") {
                        id = "timeline-track"
                        items.forEach { item ->
                            val startMinute = parseTimeToMinutes(item.timeStart) ?: 0
                            val endMinute = parseTimeToMinutes(item.timeEnd) ?: 1440
                            val leftPct = (startMinute.toDouble() / 1440.0 * 100)
                            val widthPct = ((endMinute - startMinute).toDouble() / 1440.0 * 100).coerceAtLeast(1.0)
                            val colorClass = if (item.assetType == AssetType.VIDEO) "grid-item-video" else "grid-item-image"

                            div("timeline-item $colorClass") {
                                style = "left:${leftPct}%;width:${widthPct}%"
                                title = "${item.assetName} (${item.timeStart ?: "00:00"} - ${item.timeEnd ?: "23:59"})"
                                span("timeline-item-label") { +item.assetName }
                            }
                        }
                    }
                }
            }
        }

        // Week grid view (hidden by default)
        div("card mb-4") {
            id = "weekgrid-view"
            style = "display:none"
            div("card-header") {
                h3 { +"Week Grid" }
            }
            div("card-body") {
                table("table schedule-grid-table") {
                    thead {
                        tr {
                            th { +"Day" }
                            for (h in 0..23) {
                                th { classes = setOf("schedule-grid-hour-header"); +String.format("%02d", h) }
                            }
                        }
                    }
                    tbody {
                        val dayNames = listOf("Mon" to 1, "Tue" to 2, "Wed" to 4, "Thu" to 8, "Fri" to 16, "Sat" to 32, "Sun" to 64)
                        dayNames.forEach { (dayName, dayBit) ->
                            tr {
                                td("schedule-grid-day") { +dayName }
                                for (h in 0..23) {
                                    td("schedule-grid-cell") {
                                        attributes["data-day"] = dayBit.toString()
                                        attributes["data-hour"] = h.toString()
                                        // Find items that overlap this hour and day
                                        val overlapping = items.filter { item ->
                                            val days = item.daysOfWeek ?: 127
                                            val startH = parseTimeToHour(item.timeStart) ?: 0
                                            val endH = parseTimeToHour(item.timeEnd) ?: 24
                                            (days and dayBit != 0) && h in startH until endH
                                        }
                                        overlapping.firstOrNull()?.let { item ->
                                            val colorClass = if (item.assetType == AssetType.VIDEO) "grid-item-video" else "grid-item-image"
                                            div("schedule-grid-item $colorClass") {
                                                title = item.assetName
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

        script {
            unsafe {
                +scheduleGridScript()
            }
        }
    }
}

private fun parseTimeToMinutes(time: String?): Int? {
    if (time == null) return null
    val parts = time.split(":")
    if (parts.size < 2) return null
    return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
}

private fun parseTimeToHour(time: String?): Int? {
    if (time == null) return null
    return time.split(":").firstOrNull()?.toIntOrNull()
}

private fun scheduleGridScript(): String = """
(function() {
    const btnTimeline = document.getElementById('btn-timeline');
    const btnWeekgrid = document.getElementById('btn-weekgrid');
    const timelineView = document.getElementById('timeline-view');
    const weekgridView = document.getElementById('weekgrid-view');

    btnTimeline.addEventListener('click', function() {
        timelineView.style.display = '';
        weekgridView.style.display = 'none';
        btnTimeline.className = 'btn btn-sm btn-primary grid-view-btn';
        btnWeekgrid.className = 'btn btn-sm btn-secondary grid-view-btn';
    });

    btnWeekgrid.addEventListener('click', function() {
        timelineView.style.display = 'none';
        weekgridView.style.display = '';
        btnWeekgrid.className = 'btn btn-sm btn-primary grid-view-btn';
        btnTimeline.className = 'btn btn-sm btn-secondary grid-view-btn';
    });
})();
"""
