package com.playoutedge.server.views.assets

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.emptyState
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*

/**
 * Assets list view with improved UX.
 */
fun HTML.assetsListView(
    session: AdminClaims,
    assets: List<AssetViewItem>,
    quota: QuotaInfo,
    filters: AssetFilters
) {
    adminLayout(title = "Assets", userName = session.displayName, currentPath = "/admin/assets") {
        pageHeader(
            title = "Assets",
            subtitle = "Manage your media library"
        ) {
            a(href = "/admin/assets/upload", classes = "btn btn-primary") {
                +"+ Upload"
            }
        }

        // Storage quota
        div("card mb-4") {
            div("card-body") {
                div("quota-info") {
                    div("quota-label") {
                        span { +"Storage Usage" }
                        span { +"${quota.usedFormatted} / ${quota.limitFormatted}" }
                    }
                    div("quota-bar") {
                        val barClass = when {
                            quota.usedPercent >= 90 -> "danger"
                            quota.usedPercent >= 70 -> "warning"
                            else -> ""
                        }
                        div("quota-fill $barClass") {
                            style = "width: ${quota.usedPercent}%"
                        }
                    }
                }
            }
        }

        // Filters
        div("card mb-4") {
            form(action = "/admin/assets", method = FormMethod.get, classes = "filter-form") {
                div("filter-row") {
                    div("form-group") {
                        label {
                            htmlFor = "filter-type"
                            +"Type"
                        }
                        select("form-control") {
                            id = "filter-type"
                            name = "type"
                            option {
                                value = ""
                                if (filters.type == null) selected = true
                                +"All Types"
                            }
                            AssetType.entries.forEach { type ->
                                option {
                                    value = type.name
                                    if (filters.type == type) selected = true
                                    +type.name.lowercase().replaceFirstChar { it.uppercase() }
                                }
                            }
                        }
                    }
                    div("form-group") {
                        label {
                            htmlFor = "filter-status"
                            +"Status"
                        }
                        select("form-control") {
                            id = "filter-status"
                            name = "status"
                            option {
                                value = ""
                                if (filters.status == null) selected = true
                                +"All Statuses"
                            }
                            AssetStatus.entries.forEach { status ->
                                option {
                                    value = status.name
                                    if (filters.status == status) selected = true
                                    +status.name.lowercase().replaceFirstChar { it.uppercase() }
                                }
                            }
                        }
                    }
                    div("form-group") {
                        label {
                            htmlFor = "filter-search"
                            +"Search"
                        }
                        input(type = InputType.text, classes = "form-control") {
                            id = "filter-search"
                            name = "search"
                            placeholder = "Search assets..."
                            value = filters.search ?: ""
                        }
                    }
                    div("filter-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-secondary") {
                            +"Apply"
                        }
                        if (filters.hasActiveFilters()) {
                            a(href = "/admin/assets", classes = "btn btn-ghost") {
                                +"Clear"
                            }
                        }
                    }
                }
            }
        }

        // Assets table
        div("card") {
            if (assets.isEmpty()) {
                emptyState(
                    icon = "🎬",
                    title = "No assets found",
                    description = if (filters.hasActiveFilters())
                        "Try adjusting your filters or upload new content."
                    else
                        "Upload your first video or image to get started.",
                    actionHref = "/admin/assets/upload",
                    actionLabel = "Upload Assets"
                )
            } else {
                table("table") {
                    thead {
                        tr {
                            th { +"Name" }
                            th { +"Type" }
                            th { +"Status" }
                            th { +"Size" }
                            th { +"Duration" }
                            th { +"Created" }
                            th { }
                        }
                    }
                    tbody {
                        assets.forEach { asset ->
                            tr {
                                td {
                                    a(href = "/admin/assets/${asset.id}", classes = "font-medium") {
                                        +asset.name
                                    }
                                }
                                td {
                                    val typeIcon = when (asset.type) {
                                        AssetType.VIDEO -> "🎬"
                                        AssetType.IMAGE -> "🖼"
                                        else -> "📄"
                                    }
                                    span("badge badge-gray badge-plain") {
                                        +"$typeIcon ${asset.type.name.lowercase()}"
                                    }
                                }
                                td {
                                    span("badge badge-${assetStatusBadge(asset.status)}") {
                                        +asset.status.name.lowercase()
                                    }
                                }
                                td {
                                    span("text-muted") { +asset.fileSizeFormatted }
                                }
                                td {
                                    asset.durationFormatted?.let { +it } ?: span("text-muted") { +"—" }
                                }
                                td {
                                    span("text-muted") {
                                        +asset.createdAt.toString().substringBefore("T")
                                    }
                                }
                                td {
                                    div("table-actions") {
                                        a(href = "/admin/assets/${asset.id}", classes = "btn btn-secondary btn-sm") {
                                            +"View"
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

private fun assetStatusBadge(status: AssetStatus): String = when (status) {
    AssetStatus.UPLOADING -> "warning"
    AssetStatus.PROCESSING -> "warning"
    AssetStatus.READY -> "success"
    AssetStatus.REJECTED -> "danger"
    AssetStatus.ARCHIVED -> "gray"
}

fun AssetFilters.hasActiveFilters(): Boolean =
    type != null || status != null || !search.isNullOrBlank()
