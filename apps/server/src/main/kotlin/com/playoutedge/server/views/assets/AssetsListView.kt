package com.playoutedge.server.views.assets

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*

/**
 * Assets list view.
 */
fun HTML.assetsListView(
    session: AdminClaims,
    assets: List<AssetViewItem>,
    quota: QuotaInfo,
    filters: AssetFilters
) {
    adminLayout(title = "Assets", userName = "Admin") {
        div("page-header") {
            h1("page-title") { +"Assets" }
            a(href = "/admin/assets/upload", classes = "btn btn-primary") {
                +"Upload Assets"
            }
        }

        // Quota bar
        div("card mb-4") {
            div("card-body") {
                div("quota-info") {
                    div("quota-label") {
                        +"Storage: ${quota.usedFormatted} / ${quota.limitFormatted}"
                    }
                    div("quota-bar") {
                        div("quota-fill") {
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
                        label { +"Type" }
                        select("form-control") {
                            name = "type"
                            option {
                                value = ""
                                if (filters.type == null) selected = true
                                +"All"
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
                        label { +"Status" }
                        select("form-control") {
                            name = "status"
                            option {
                                value = ""
                                if (filters.status == null) selected = true
                                +"All"
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
                        label { +"Search" }
                        input(type = InputType.text, classes = "form-control") {
                            name = "search"
                            placeholder = "Asset name..."
                            value = filters.search ?: ""
                        }
                    }
                    button(type = ButtonType.submit, classes = "btn btn-secondary") {
                        +"Filter"
                    }
                }
            }
        }

        // Assets table
        div("card") {
            if (assets.isEmpty()) {
                div("empty-state") {
                    p("empty-state-title") { +"No assets found" }
                    p("empty-state-text") { +"Upload your first video or image to get started." }
                    a(href = "/admin/assets/upload", classes = "btn btn-primary") {
                        +"Upload Assets"
                    }
                }
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
                        }
                    }
                    tbody {
                        assets.forEach { asset ->
                            tr {
                                td {
                                    a(href = "/admin/assets/${asset.id}") {
                                        +asset.name
                                    }
                                }
                                td {
                                    span("badge badge-gray") { +asset.type.name.lowercase() }
                                }
                                td {
                                    span("badge badge-${assetStatusBadge(asset.status)}") {
                                        +asset.status.name.lowercase()
                                    }
                                }
                                td { +asset.fileSizeFormatted }
                                td {
                                    asset.durationFormatted?.let { +it } ?: span("text-muted") { +"—" }
                                }
                                td { +asset.createdAt.toString() }
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
