package com.playoutedge.server.views.assets

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.server.views.adminLayout
import kotlinx.html.*

/**
 * Asset detail view.
 */
fun HTML.assetDetailView(
    session: AdminClaims,
    asset: AssetDetailModel
) {
    adminLayout(title = asset.name, userName = "Admin") {
        // Header
        div("page-header") {
            div {
                a(href = "/admin/assets", classes = "link") { +"← Back to Assets" }
                h1("page-title") { +asset.name }
            }
            div("header-actions") {
                span("badge badge-${assetStatusBadge(asset.status)}") {
                    +asset.status.name.lowercase()
                }
            }
        }

        // Rejection reason
        if (asset.status == AssetStatus.REJECTED && asset.rejectionReason != null) {
            div("alert alert-error mb-4") {
                strong { +"Rejected: " }
                +asset.rejectionReason
            }
        }

        // Info cards
        div("dashboard-grid mb-4") {
            // Basic info
            div("card") {
                div("card-header") {
                    h3 { +"Information" }
                }
                div("card-body") {
                    dl("info-list") {
                        dt { +"Type" }
                        dd { +asset.type.name.lowercase() }

                        dt { +"MIME Type" }
                        dd { +(asset.mimeType ?: "—") }

                        dt { +"File Size" }
                        dd { +asset.fileSizeFormatted }

                        dt { +"Created" }
                        dd { +asset.createdAt.toString() }
                    }
                }
            }

            // Media info
            div("card") {
                div("card-header") {
                    h3 { +"Media Info" }
                }
                div("card-body") {
                    dl("info-list") {
                        dt { +"Resolution" }
                        dd { +(asset.resolution ?: "—") }

                        dt { +"Duration" }
                        dd { +(asset.durationFormatted ?: "—") }
                    }
                }
            }
        }

        // Actions
        div("card mb-4") {
            div("card-header") {
                h3 { +"Actions" }
            }
            div("card-body") {
                div("quick-actions") {
                    form(action = "/admin/assets/${asset.id}/archive", method = FormMethod.post, classes = "inline") {
                        button(type = ButtonType.submit, classes = "btn btn-danger") {
                            onClick = "return confirm('Are you sure you want to archive this asset?')"
                            +"Archive Asset"
                        }
                    }
                }
            }
        }

        // Asset ID
        div("card") {
            div("card-header") {
                h3 { +"Details" }
            }
            div("card-body") {
                dl("info-list") {
                    dt { +"Asset ID" }
                    dd {
                        code { +asset.id.toString() }
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
