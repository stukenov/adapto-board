package com.playoutedge.server.routes.admin

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.persistence.repositories.ChannelRepository
import com.playoutedge.persistence.repositories.ScheduleRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.adminLayout
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import java.util.UUID

/**
 * Admin schedule management routes.
 */
fun Route.adminScheduleRoutes(
    channelRepository: ChannelRepository,
    scheduleRepository: ScheduleRepository,
    assetRepository: AssetRepository
) {
    route("/admin/channels/{channelId}/schedule") {
        // GET /admin/channels/:channelId/schedule - Schedule editor
        get {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val channel = channelRepository.findById(tenantId, channelId)
            if (channel == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            // Get current schedule
            val scheduleVersion = scheduleRepository.findActiveVersion(tenantId, channelId)
            val items = scheduleVersion?.let { version ->
                scheduleRepository.findItemsByVersion(tenantId, version.id.value)
            } ?: emptyList()

            // Get available assets
            val assets = assetRepository.findAllActive(tenantId)

            call.respondHtml {
                adminLayout(title = "Edit Schedule - ${channel.name}", userName = "Admin") {
                    div("page-header") {
                        div {
                            a(href = "/admin/channels/$channelId", classes = "link") {
                                +"← Back to ${channel.name}"
                            }
                            h1("page-title") { +"Schedule Editor" }
                        }
                        div("header-actions") {
                            if (scheduleVersion != null) {
                                span("badge badge-success") { +"Published v${scheduleVersion.version}" }
                            }
                        }
                    }

                    div("card mb-4") {
                        div("card-header") {
                            h3 { +"Schedule Items" }
                        }
                        div("card-body") {
                            if (items.isEmpty()) {
                                div("empty-state") {
                                    p { +"No items in schedule" }
                                    p("text-muted") { +"Add assets from the library below to create a playlist." }
                                }
                            } else {
                                table("table") {
                                    thead {
                                        tr {
                                            th { +"#" }
                                            th { +"Asset" }
                                            th { +"Duration" }
                                            th { +"Time Window" }
                                        }
                                    }
                                    tbody {
                                        items.forEachIndexed { index, item ->
                                            tr {
                                                td { +"${index + 1}" }
                                                td { +item.asset.name }
                                                td {
                                                    item.asset.durationMs?.let {
                                                        +"${it / 1000}s"
                                                    } ?: span("text-muted") { +"—" }
                                                }
                                                td {
                                                    if (item.timeStart != null || item.timeEnd != null) {
                                                        +"${item.timeStart ?: "00:00"} - ${item.timeEnd ?: "23:59"}"
                                                    } else {
                                                        span("text-muted") { +"All day" }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    div("card") {
                        div("card-header") {
                            h3 { +"Available Assets" }
                        }
                        div("card-body") {
                            if (assets.isEmpty()) {
                                div("empty-state") {
                                    p { +"No assets available" }
                                    a(href = "/admin/assets", classes = "btn btn-primary") {
                                        +"Upload Assets"
                                    }
                                }
                            } else {
                                div("asset-grid") {
                                    assets.forEach { asset ->
                                        div("asset-card") {
                                            div("asset-info") {
                                                strong { +asset.name }
                                                span("text-muted") {
                                                    +" (${asset.type.name.lowercase()})"
                                                }
                                            }
                                            form(action = "/admin/channels/$channelId/schedule/add", method = FormMethod.post, classes = "inline") {
                                                input(type = InputType.hidden) {
                                                    name = "assetId"
                                                    value = asset.id.value.toString()
                                                }
                                                button(type = ButtonType.submit, classes = "btn btn-sm btn-secondary") {
                                                    +"Add to Schedule"
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
        }

        // POST /admin/channels/:channelId/schedule/add - Add item to schedule
        post("/add") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val channelId = call.parameters["channelId"]

            // TODO: Implement adding items to schedule
            // For now, just redirect back

            call.respondRedirect("/admin/channels/$channelId/schedule")
        }
    }
}
