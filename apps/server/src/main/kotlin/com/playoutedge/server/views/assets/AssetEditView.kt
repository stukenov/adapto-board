package com.playoutedge.server.views.assets

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.*
import kotlinx.html.*

data class AssetEditModel(
    val id: java.util.UUID,
    val name: String,
    val description: String?,
    val tags: String?,
    val approvalStatus: String = "APPROVED",
    val expiresAt: String? = null,
    val allTags: List<String> = emptyList()
)

fun HTML.assetEditView(
    session: AdminClaims,
    asset: AssetEditModel,
    error: String? = null,
    success: String? = null
) {
    adminLayout(
        title = "Edit Asset",
        userName = session.displayName,
        userRole = session.roleLabel,
        currentPath = "/admin/assets"
    ) {
        breadcrumb("Assets" to "/admin/assets", asset.name to "/admin/assets/${asset.id}", "Edit" to null)
        pageHeader(title = "Edit Asset", subtitle = asset.name, backHref = "/admin/assets/${asset.id}", backLabel = "Back to Asset")

        if (error != null) alertBox(error, "error")
        if (success != null) alertBox(success, "success")

        div("card") {
            div("card-body") {
                form(action = "/admin/assets/${asset.id}/edit", method = FormMethod.post) {
                    div("form-group") {
                        label { htmlFor = "name"; +"Name" }
                        input(type = InputType.text, name = "name", classes = "form-control") {
                            id = "name"; value = asset.name; required = true
                        }
                    }
                    div("form-group") {
                        label { htmlFor = "description"; +"Description" }
                        textArea(classes = "form-control") {
                            id = "description"; name = "description"; rows = "3"
                            +(asset.description ?: "")
                        }
                    }
                    div("form-group") {
                        label { htmlFor = "tags"; +"Tags" }
                        input(type = InputType.text, name = "tags", classes = "form-control") {
                            id = "tags"; value = asset.tags ?: ""
                            placeholder = "Comma-separated tags"
                            list = "tags-datalist"
                        }
                        dataList {
                            id = "tags-datalist"
                            asset.allTags.forEach { tag ->
                                option { value = tag }
                            }
                        }
                        small("form-help") { +"Separate tags with commas" }
                    }
                    div("form-group") {
                        label { htmlFor = "approval_status"; +"Content Approval Status" }
                        select("form-control") {
                            id = "approval_status"; name = "approval_status"
                            listOf("PENDING_REVIEW", "APPROVED", "REJECTED").forEach { opt ->
                                option {
                                    value = opt
                                    if (asset.approvalStatus == opt) selected = true
                                    +opt.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
                                }
                            }
                        }
                    }
                    div("form-group") {
                        label { htmlFor = "expires_at"; +"Content Expiration" }
                        input(type = InputType.date, name = "expires_at", classes = "form-control") {
                            id = "expires_at"
                            value = asset.expiresAt ?: ""
                        }
                        small("form-help") { +"Leave blank for no expiration" }
                    }
                    button(type = ButtonType.submit, classes = "btn btn-primary") { +"Save Changes" }
                }
            }
        }
    }
}
