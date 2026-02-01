package com.playoutedge.server.views.assets

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.alertBox
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import kotlinx.html.*

/**
 * Asset upload form view.
 */
fun HTML.assetUploadView(
    session: AdminClaims,
    quota: QuotaInfo,
    error: String? = null,
    success: String? = null
) {
    adminLayout(title = "Upload Asset", userName = session.displayName, currentPath = "/admin/assets") {
        pageHeader(
            title = "Upload Asset",
            subtitle = "Add new media to your library",
            backHref = "/admin/assets",
            backLabel = "Back to Assets"
        )

        // Storage quota info
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

        div("card") {
            div("card-body") {
                if (error != null) {
                    alertBox(error, "error")
                }
                if (success != null) {
                    alertBox(success, "success")
                }

                form(
                    action = "/admin/assets/upload",
                    method = FormMethod.post,
                    encType = FormEncType.multipartFormData
                ) {
                    div("form-group") {
                        label {
                            htmlFor = "file"
                            +"Select File"
                        }
                        input(type = InputType.file, classes = "form-control") {
                            id = "file"
                            name = "file"
                            accept = "image/png,image/jpeg,video/mp4,video/x-m4v"
                            required = true
                        }
                        small("form-helper") {
                            +"Supported formats: PNG, JPEG images (max 20MB), MP4 videos (max 2GB). Maximum resolution: 1920x1080 for videos, 3840x2160 for images."
                        }
                    }

                    div("form-actions") {
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Upload"
                        }
                        a(href = "/admin/assets", classes = "btn btn-secondary") {
                            +"Cancel"
                        }
                    }
                }
            }
        }
    }
}
