package com.playoutedge.server.views.overlay

import com.playoutedge.auth.AdminClaims
import com.playoutedge.server.services.WidgetTemplateDTO
import com.playoutedge.server.views.*
import kotlinx.html.*

/**
 * Template catalog page — read-only gallery of all templates.
 */
fun HTML.overlayTemplateCatalogView(session: AdminClaims, templates: List<WidgetTemplateDTO> = emptyList()) {
    adminLayout(title = "Template Catalog", userName = session.displayName, currentPath = "/admin/overlay") {
        pageHeader(
            title = "Template Catalog",
            subtitle = "Browse overlay templates with live previews",
            backHref = "/admin/overlay/profiles",
            backLabel = "Back to Profiles"
        )

        div("template-catalog") {
            if (templates.isNotEmpty()) {
                templates.forEach { template ->
                    div("catalog-card") {
                        div("catalog-preview") {
                            if (template.previewHtml != null) {
                                div("template-tv-preview template-preview-large") {
                                    unsafe { +template.previewHtml }
                                }
                            } else {
                                div("template-tv-preview template-preview-large") {
                                    div("tv-clock") { +template.code }
                                }
                            }
                        }
                        div("catalog-info") {
                            h3("catalog-title") { +template.name }
                            p("catalog-desc") { +(template.description ?: "") }
                            span("badge badge-gray badge-plain") { +template.category }
                            a(href = "/admin/overlay/profiles/new?template=${template.code}", classes = "btn btn-primary btn-sm") {
                                +"Use Template"
                            }
                        }
                    }
                }
            } else {
                div("card") {
                    div("card-body") {
                        p("text-muted") { +"No templates available. Add templates in the Widget Templates settings." }
                    }
                }
            }
        }
    }
}
