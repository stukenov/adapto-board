package com.playoutedge.server.views.devices

import com.playoutedge.auth.AdminClaims
import com.playoutedge.persistence.repositories.DeviceLogData
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.breadcrumb
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*
import java.util.UUID

/**
 * Device logs view showing error logs and diagnostics.
 */
fun HTML.deviceLogsView(
    session: AdminClaims,
    deviceId: UUID,
    deviceName: String,
    logs: List<DeviceLogData>,
    currentPage: Int,
    totalLogs: Long,
    pageSize: Int
) {
    adminLayout(title = "Logs - $deviceName", userName = session.displayName, currentPath = "/admin/devices") {
        breadcrumb("Devices" to "/admin/devices", deviceName to "/admin/devices/$deviceId", "Logs" to null)
        pageHeader(
            title = "Device Logs",
            subtitle = "Logs for $deviceName",
            backHref = "/admin/devices/$deviceId",
            backLabel = "Back to Device"
        )

        if (logs.isEmpty()) {
            emptyState(
                icon = "\uD83D\uDCCB",
                title = "No logs found",
                description = "This device has not reported any logs yet."
            )
        } else {
            div("card") {
                div("card-header") {
                    h3 { +"Logs" }
                    span("badge badge-gray badge-plain") { +"$totalLogs total" }
                }
                table("table") {
                    thead {
                        tr {
                            th { +"Time" }
                            th { +"Level" }
                            th { +"Message" }
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
                                    val badgeClass = when (log.level.uppercase()) {
                                        "ERROR" -> "badge-danger"
                                        "WARN", "WARNING" -> "badge-warning"
                                        "INFO" -> "badge-success"
                                        else -> "badge-gray"
                                    }
                                    span("badge $badgeClass") { +log.level.uppercase() }
                                }
                                td {
                                    +log.message
                                    val st = log.stackTrace
                                    if (st != null) {
                                        details {
                                            summary { +"Stack trace" }
                                            pre { code { +st } }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Simple pagination
            val totalPages = ((totalLogs + pageSize - 1) / pageSize).toInt().coerceAtLeast(1)
            if (totalPages > 1) {
                nav("pagination mt-3") {
                    if (currentPage > 1) {
                        a(href = "/admin/devices/$deviceId/logs?page=${currentPage - 1}", classes = "btn btn-secondary btn-sm") {
                            +"Previous"
                        }
                    }
                    span("text-muted") {
                        style = "padding: 0.5rem 1rem;"
                        +"Page $currentPage of $totalPages"
                    }
                    if (currentPage < totalPages) {
                        a(href = "/admin/devices/$deviceId/logs?page=${currentPage + 1}", classes = "btn btn-secondary btn-sm") {
                            +"Next"
                        }
                    }
                }
            }
        }
    }
}
