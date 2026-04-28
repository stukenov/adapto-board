package com.playoutedge.server.routes.admin

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.WidgetTemplateRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.services.WidgetTemplateDTO
import com.playoutedge.server.services.WidgetTemplateService
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.pageHeader
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.UUID

/**
 * Admin widget template management routes — list, create, edit, duplicate.
 */
fun Route.adminWidgetTemplateRoutes(
    widgetTemplateRepository: WidgetTemplateRepository,
    widgetTemplateService: WidgetTemplateService
) {
    route("/admin/templates") {
        // GET /admin/templates — list all templates
        get {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@get }
            val tenantId = TenantId(session.tenantId)
            val templates = widgetTemplateService.getTemplates(tenantId)
            call.respondHtml {
                widgetTemplateListView(session, templates)
            }
        }

        // GET /admin/templates/new — new template form
        get("/new") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@get }
            call.respondHtml {
                widgetTemplateEditorView(session, template = null)
            }
        }

        // POST /admin/templates — create template
        post {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
            val tenantId = TenantId(session.tenantId)
            val params = call.receiveParameters()
            val code = params["code"]?.trim() ?: ""
            val name = params["name"]?.trim() ?: ""
            val category = params["category"]?.trim() ?: "general"
            val description = params["description"]?.trim()?.takeIf { it.isNotEmpty() }
            val schemaJson = params["schema_json"]?.trim() ?: "{}"
            val htmlTemplate = params["html_template"]?.trim() ?: ""
            val cssCode = params["css_code"]?.trim()?.takeIf { it.isNotEmpty() }
            val jsCode = params["js_code"]?.trim()?.takeIf { it.isNotEmpty() }
            val defaultDataJson = params["default_data_json"]?.trim() ?: "{}"
            val previewHtml = params["preview_html"]?.trim()?.takeIf { it.isNotEmpty() }

            if (code.isBlank() || name.isBlank() || htmlTemplate.isBlank()) {
                call.respondHtml {
                    widgetTemplateEditorView(session, template = null, error = "Code, name, and HTML template are required")
                }
                return@post
            }

            // Validate JSON fields
            try {
                Json.decodeFromString<JsonObject>(schemaJson)
                Json.decodeFromString<JsonObject>(defaultDataJson)
            } catch (e: Exception) {
                call.respondHtml {
                    widgetTemplateEditorView(session, template = null, error = "Invalid JSON: ${e.message}")
                }
                return@post
            }

            widgetTemplateRepository.create(
                tenantId = tenantId,
                code = code,
                name = name,
                description = description,
                category = category,
                schemaJson = schemaJson,
                htmlTemplate = htmlTemplate,
                cssCode = cssCode,
                jsCode = jsCode,
                defaultDataJson = defaultDataJson,
                previewHtml = previewHtml
            )
            widgetTemplateService.invalidateCache(session.tenantId)
            call.respondRedirect("/admin/templates")
        }

        // GET /admin/templates/{id}/edit — edit form
        get("/{id}/edit") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@get }
            val id = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            if (id == null) { call.respondRedirect("/admin/templates"); return@get }
            val template = widgetTemplateService.getTemplateById(id)
            if (template == null) { call.respondRedirect("/admin/templates"); return@get }
            // Only allow editing tenant-owned templates
            if (template.isGlobal) {
                call.respondRedirect("/admin/templates")
                return@get
            }
            call.respondHtml {
                widgetTemplateEditorView(session, template = template)
            }
        }

        // POST /admin/templates/{id} — update template
        post("/{id}") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
            val id = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            if (id == null) { call.respondRedirect("/admin/templates"); return@post }
            val params = call.receiveParameters()

            // Validate JSON fields
            try {
                Json.decodeFromString<JsonObject>(params["schema_json"]?.trim() ?: "{}")
                Json.decodeFromString<JsonObject>(params["default_data_json"]?.trim() ?: "{}")
            } catch (e: Exception) {
                val template = widgetTemplateService.getTemplateById(id)
                call.respondHtml {
                    widgetTemplateEditorView(session, template = template, error = "Invalid JSON: ${e.message}")
                }
                return@post
            }

            widgetTemplateRepository.update(
                id = id,
                name = params["name"]?.trim() ?: "",
                description = params["description"]?.trim()?.takeIf { it.isNotEmpty() },
                category = params["category"]?.trim() ?: "general",
                schemaJson = params["schema_json"]?.trim() ?: "{}",
                htmlTemplate = params["html_template"]?.trim() ?: "",
                cssCode = params["css_code"]?.trim()?.takeIf { it.isNotEmpty() },
                jsCode = params["js_code"]?.trim()?.takeIf { it.isNotEmpty() },
                defaultDataJson = params["default_data_json"]?.trim() ?: "{}",
                previewHtml = params["preview_html"]?.trim()?.takeIf { it.isNotEmpty() }
            )
            widgetTemplateService.invalidateCache(session.tenantId)
            call.respondRedirect("/admin/templates")
        }

        // POST /admin/templates/{id}/duplicate — duplicate a template for this tenant
        post("/{id}/duplicate") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
            val id = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            if (id == null) { call.respondRedirect("/admin/templates"); return@post }
            val tenantId = TenantId(session.tenantId)
            val source = widgetTemplateService.getTemplateById(id)
            if (source == null) { call.respondRedirect("/admin/templates"); return@post }
            val newCode = source.code + "-custom-${System.currentTimeMillis() % 10000}"
            widgetTemplateRepository.duplicate(id, tenantId, newCode)
            widgetTemplateService.invalidateCache(session.tenantId)
            call.respondRedirect("/admin/templates")
        }

        // POST /admin/templates/{id}/delete — delete a tenant template
        post("/{id}/delete") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
            val id = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            if (id == null) { call.respondRedirect("/admin/templates"); return@post }
            val template = widgetTemplateService.getTemplateById(id)
            if (template == null || template.isGlobal) {
                call.respondRedirect("/admin/templates")
                return@post
            }
            widgetTemplateRepository.delete(id)
            widgetTemplateService.invalidateCache(session.tenantId)
            call.respondRedirect("/admin/templates")
        }
    }
}

/**
 * Template list view.
 */
private fun HTML.widgetTemplateListView(session: com.playoutedge.auth.AdminClaims, templates: List<WidgetTemplateDTO>) {
    adminLayout(title = "Widget Templates", userName = session.displayName, currentPath = "/admin/templates") {
        pageHeader(
            title = "Widget Templates",
            subtitle = "${templates.size} templates available"
        )

        div("mb-4") {
            a(href = "/admin/templates/new", classes = "btn btn-primary") {
                +"+ New Template"
            }
        }

        div("template-catalog") {
            templates.forEach { t ->
                div("catalog-card") {
                    div("catalog-info") {
                        h3("catalog-title") {
                            +t.name
                            if (t.isGlobal) {
                                span("badge badge-gray badge-plain ml-2") { +"System" }
                            }
                        }
                        p("catalog-desc") {
                            +(t.description ?: "")
                        }
                        span("badge badge-gray badge-plain") { +t.category }
                        span("text-muted ml-2") { +"Code: ${t.code}" }
                    }
                    div("catalog-actions") {
                        if (!t.isGlobal) {
                            a(href = "/admin/templates/${t.id}/edit", classes = "btn btn-sm btn-secondary") { +"Edit" }
                        }
                        form(action = "/admin/templates/${t.id}/duplicate", method = FormMethod.post) {
                            style = "display:inline"
                            button(type = ButtonType.submit, classes = "btn btn-sm btn-secondary") { +"Duplicate" }
                        }
                        if (!t.isGlobal) {
                            form(action = "/admin/templates/${t.id}/delete", method = FormMethod.post) {
                                style = "display:inline"
                                attributes["onsubmit"] = "return confirm('Delete this template?')"
                                button(type = ButtonType.submit, classes = "btn btn-sm btn-danger") { +"Delete" }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Template editor view (create / edit).
 */
private fun HTML.widgetTemplateEditorView(
    session: com.playoutedge.auth.AdminClaims,
    template: WidgetTemplateDTO?,
    error: String? = null
) {
    val isEdit = template != null
    val title = if (isEdit) "Edit Template: ${template!!.name}" else "New Template"
    val action = if (isEdit) "/admin/templates/${template!!.id}" else "/admin/templates"

    adminLayout(title = title, userName = session.displayName, currentPath = "/admin/templates") {
        pageHeader(
            title = title,
            backHref = "/admin/templates",
            backLabel = "Back to Templates"
        )

        if (error != null) {
            div("alert alert-danger") { +error }
        }

        form(action = action, method = FormMethod.post) {
            div("form-grid") {
                div("form-group") {
                    label { +"Code" }
                    input(type = InputType.text, name = "code", classes = "form-control") {
                        value = template?.code ?: ""
                        if (isEdit) readonly = true
                    }
                }
                div("form-group") {
                    label { +"Name" }
                    input(type = InputType.text, name = "name", classes = "form-control") {
                        value = template?.name ?: ""
                    }
                }
                div("form-group") {
                    label { +"Category" }
                    input(type = InputType.text, name = "category", classes = "form-control") {
                        value = template?.category ?: "general"
                    }
                }
                div("form-group") {
                    label { +"Description" }
                    textArea(classes = "form-control") {
                        name = "description"
                        rows = "2"
                        +(template?.description ?: "")
                    }
                }
            }

            div("form-group") {
                label { +"HTML Template" }
                textArea(classes = "form-control code-editor") {
                    name = "html_template"
                    rows = "10"
                    +(template?.htmlTemplate ?: "")
                }
            }

            div("form-group") {
                label { +"CSS Code" }
                textArea(classes = "form-control code-editor") {
                    name = "css_code"
                    rows = "8"
                    +(template?.cssCode ?: "")
                }
            }

            div("form-group") {
                label { +"JS Code (optional)" }
                textArea(classes = "form-control code-editor") {
                    name = "js_code"
                    rows = "6"
                    +(template?.jsCode ?: "")
                }
            }

            div("form-group") {
                label { +"Schema JSON" }
                textArea(classes = "form-control code-editor") {
                    name = "schema_json"
                    rows = "8"
                    +(template?.schemaJson?.toString() ?: "{}")
                }
            }

            div("form-group") {
                label { +"Default Data JSON" }
                textArea(classes = "form-control code-editor") {
                    name = "default_data_json"
                    rows = "4"
                    +(template?.defaultDataJson?.toString() ?: "{}")
                }
            }

            div("form-group") {
                label { +"Preview HTML (optional, CSS-only miniature)" }
                textArea(classes = "form-control code-editor") {
                    name = "preview_html"
                    rows = "4"
                    +(template?.previewHtml ?: "")
                }
            }

            div("form-actions") {
                button(type = ButtonType.submit, classes = "btn btn-primary") {
                    +(if (isEdit) "Save Changes" else "Create Template")
                }
                a(href = "/admin/templates", classes = "btn btn-secondary") { +"Cancel" }
            }
        }
    }
}
