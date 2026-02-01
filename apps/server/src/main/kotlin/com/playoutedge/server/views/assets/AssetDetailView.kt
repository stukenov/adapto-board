package com.playoutedge.server.views.assets

import com.playoutedge.auth.AdminClaims
import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.alertBox
import com.playoutedge.server.views.dangerItem
import com.playoutedge.server.views.dangerZone
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*

/**
 * Asset detail view with improved layout.
 */
fun HTML.assetDetailView(
    session: AdminClaims,
    asset: AssetDetailModel
) {
    adminLayout(title = asset.name, userName = session.displayName, currentPath = "/admin/assets") {
        pageHeader(
            title = asset.name,
            backHref = "/admin/assets",
            backLabel = "Back to Assets"
        ) {
            span("badge badge-${assetStatusBadge(asset.status)} badge-lg") {
                +asset.status.name.lowercase()
            }
        }

        // Rejection reason alert
        if (asset.status == AssetStatus.REJECTED && asset.rejectionReason != null) {
            alertBox("Rejected: ${asset.rejectionReason}", "error")
        }

        // Info cards
        div("stats-grid mb-4") {
            // Basic info card
            div("card") {
                div("card-header") {
                    h3 { +"Information" }
                }
                div("card-body") {
                    dl("info-list") {
                        dt { +"Type" }
                        dd {
                            val typeIcon = when (asset.type) {
                                AssetType.VIDEO -> "🎬"
                                AssetType.IMAGE -> "🖼"
                                else -> "📄"
                            }
                            span("badge badge-gray badge-plain") {
                                +"$typeIcon ${asset.type.name.lowercase()}"
                            }
                        }

                        dt { +"MIME Type" }
                        dd {
                            asset.mimeType?.let {
                                code { +it }
                            } ?: span("text-muted") { +"—" }
                        }

                        dt { +"File Size" }
                        dd { +asset.fileSizeFormatted }

                        dt { +"Created" }
                        dd {
                            +asset.createdAt.toString().replace("T", " ").substringBeforeLast(":")
                        }
                    }
                }
            }

            // Media info card
            div("card") {
                div("card-header") {
                    h3 { +"Media Details" }
                }
                div("card-body") {
                    dl("info-list") {
                        dt { +"Resolution" }
                        dd {
                            asset.resolution?.let { +it } ?: span("text-muted") { +"—" }
                        }

                        dt { +"Duration" }
                        dd {
                            asset.durationFormatted?.let { +it } ?: span("text-muted") { +"—" }
                        }

                        // Additional video info would go here if available in the model
                    }
                }
            }

            // Usage card
            div("card") {
                div("card-header") {
                    h3 { +"Usage" }
                }
                div("card-body") {
                    p("text-muted") { +"View channels to see where this asset is used." }
                }
            }
        }

        // Asset ID
        div("card mb-4") {
            div("card-header") {
                h3 { +"Technical Details" }
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

        // Actions
        if (asset.status != AssetStatus.ARCHIVED) {
            div("card") {
                div("card-header") {
                    h3 { +"Danger Zone" }
                }
                div("card-body") {
                    dangerZone {
                        dangerItem(
                            title = "Archive Asset",
                            description = "Remove this asset from active use. It will no longer appear in channel schedules."
                        ) {
                            form(action = "/admin/assets/${asset.id}/archive", method = FormMethod.post) {
                                button(type = ButtonType.submit, classes = "btn btn-danger") {
                                    attributes["onclick"] = "return confirm('Are you sure you want to archive this asset?')"
                                    +"Archive"
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
