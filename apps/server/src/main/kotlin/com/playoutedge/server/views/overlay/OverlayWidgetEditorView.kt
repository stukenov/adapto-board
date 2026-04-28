package com.playoutedge.server.views.overlay

import kotlinx.html.*
import kotlinx.serialization.json.*

/**
 * Data class representing a widget template with its JSON Schema definition.
 * TODO: Move to com.playoutedge.server.services once WidgetTemplateDTO is defined there.
 */
data class WidgetTemplateDTO(
    val id: String,
    val type: String,
    val name: String,
    val schemaJson: JsonObject
)

/**
 * Visual widget editor — renders form fields driven by JSON Schema
 * from widget templates, pre-filled with current state data.
 */
fun FlowContent.widgetEditorCard(
    binding: OverlayBindingDetail,
    templates: Map<String, WidgetTemplateDTO> = emptyMap()
) {
    val widgets = parseWidgets(binding.profileDefinitionJson)
    val stateWidgets = parseWidgets(binding.currentStateJson)

    div("card mb-4") {
        div("card-header") {
            id = "widget-editor-header"
            h3 { +"Visual Editor" }
            button(type = ButtonType.button, classes = "btn btn-secondary btn-sm") {
                id = "toggle-json-editor"
                attributes["onclick"] = "toggleOverlayEditorMode()"
                +"Switch to JSON"
            }
        }
        div("card-body") {
            form(action = "/admin/overlay/bindings/${binding.id}/state", method = FormMethod.post) {
                id = "overlay-visual-form"
                // Hidden textarea for assembled JSON
                textArea {
                    id = "stateJson"
                    name = "stateJson"
                    style = "display:none"
                    +binding.currentStateJson
                }

                // Visual editor section
                div {
                    id = "visual-editor-section"
                    if (widgets.isEmpty()) {
                        p("text-muted") {
                            +"No widgets defined in the profile. Edit the profile definition to add widgets."
                        }
                    } else {
                        widgets.forEachIndexed { idx, widget ->
                            val type = widget.str("type") ?: "unknown"
                            val stateWidget = stateWidgets.firstOrNull { it.str("type") == type }
                                ?: if (idx < stateWidgets.size) stateWidgets[idx] else null
                            val template = templates[type.lowercase()]
                            widgetForm(idx, type, widget, stateWidget, template)
                        }
                    }
                }

                // JSON editor section (hidden by default)
                div {
                    id = "json-editor-section"
                    style = "display:none"
                    div("form-group") {
                        label {
                            htmlFor = "jsonEditorTextarea"
                            +"Overlay State JSON"
                        }
                        textArea(classes = "form-control code-editor json-editor") {
                            id = "jsonEditorTextarea"
                            rows = "14"
                            +binding.currentStateJson
                        }
                    }
                }

                div("form-actions") {
                    button(type = ButtonType.submit, classes = "btn btn-primary") {
                        id = "overlay-submit-btn"
                        +"Send to Devices"
                    }
                }
            }
        }
    }
}

private fun FlowContent.widgetForm(
    idx: Int,
    type: String,
    definition: JsonObject,
    stateWidget: JsonObject?,
    template: WidgetTemplateDTO? = null
) {
    val prefix = "widget_${idx}_"
    val widgetData = stateWidget ?: definition

    div("widget-editor-card mb-3") {
        attributes["data-widget-index"] = idx.toString()
        attributes["data-widget-type"] = type
        attributes["data-schema"] = template?.schemaJson?.toString() ?: "{}"

        div("widget-editor-title") {
            span("badge badge-info badge-plain") { +type }
            val position = stateWidget?.str("position") ?: definition.str("position") ?: ""
            if (position.isNotEmpty()) {
                span("text-muted ml-2") { +" Position: $position" }
            }
        }

        // Position selector (common for all)
        div("form-group") {
            label {
                htmlFor = "${prefix}position"
                +"Position"
            }
            select("form-control form-control-sm") {
                id = "${prefix}position"
                name = "${prefix}position"
                val current = stateWidget?.str("position") ?: definition.str("position") ?: "bottom"
                listOf("top-left", "top-right", "bottom-left", "bottom-right", "center", "bottom", "top").forEach { pos ->
                    option {
                        value = pos
                        if (pos == current) selected = true
                        +pos
                    }
                }
            }
        }

        // Schema-driven fields
        if (template != null) {
            schemaFields(prefix, template.schemaJson, widgetData)
        } else {
            p("text-muted") { +"No schema available for widget type \"$type\". Use JSON editor or assign a template." }
        }
    }
}

/**
 * Schema-driven form generator. Reads a JSON Schema object and generates
 * appropriate form inputs for each property, pre-filled from [data].
 */
private fun FlowContent.schemaFields(prefix: String, schema: JsonObject, data: JsonObject) {
    val properties = (schema["properties"] as? JsonObject) ?: return
    properties.forEach { (key, propDef) ->
        val prop = propDef as? JsonObject ?: return@forEach
        val type = (prop["type"] as? JsonPrimitive)?.content ?: "string"
        val title = (prop["title"] as? JsonPrimitive)?.content
            ?: key.replace("_", " ").replaceFirstChar { it.uppercase() }
        val format = (prop["format"] as? JsonPrimitive)?.content
        val currentValue = (data[key] as? JsonPrimitive)?.content
            ?: (prop["default"] as? JsonPrimitive)?.content
            ?: ""

        // Skip array types for now (complex nested editors)
        if (type == "array") return@forEach

        div("form-group") {
            label {
                htmlFor = "$prefix$key"
                +title
            }
            when {
                prop.containsKey("enum") -> {
                    select("form-control") {
                        id = "$prefix$key"
                        name = "$prefix$key"
                        val options = (prop["enum"] as? JsonArray) ?: return@select
                        options.forEach { opt ->
                            val optVal = (opt as? JsonPrimitive)?.content ?: ""
                            option {
                                value = optVal
                                if (optVal == currentValue) selected = true
                                +optVal
                            }
                        }
                    }
                }
                type == "boolean" -> {
                    val checked = (data[key] as? JsonPrimitive)?.content?.toBooleanStrictOrNull()
                        ?: (prop["default"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull()
                        ?: false
                    input(type = InputType.checkBox, name = "$prefix$key", classes = "form-check-input") {
                        id = "$prefix$key"
                        this.checked = checked
                        this.value = "true"
                    }
                }
                type == "number" || type == "integer" -> {
                    input(type = InputType.number, name = "$prefix$key", classes = "form-control") {
                        id = "$prefix$key"
                        value = currentValue
                        (prop["minimum"] as? JsonPrimitive)?.content?.let { min = it }
                        (prop["maximum"] as? JsonPrimitive)?.content?.let { max = it }
                    }
                }
                format == "color" -> {
                    input(type = InputType.color, name = "$prefix$key", classes = "form-control form-control-color") {
                        id = "$prefix$key"
                        value = currentValue.ifEmpty { "#000000" }
                    }
                }
                format == "uri" || format == "url" -> {
                    input(type = InputType.url, name = "$prefix$key", classes = "form-control") {
                        id = "$prefix$key"
                        value = currentValue
                        placeholder = "https://..."
                    }
                }
                format == "textarea" -> {
                    textArea(classes = "form-control") {
                        id = "$prefix$key"
                        name = "$prefix$key"
                        rows = "3"
                        +currentValue
                    }
                }
                else -> {
                    input(type = InputType.text, name = "$prefix$key", classes = "form-control") {
                        id = "$prefix$key"
                        value = currentValue
                    }
                }
            }
            // Show description if present
            val desc = (prop["description"] as? JsonPrimitive)?.content
            if (desc != null) {
                small("text-muted") { +desc }
            }
        }
    }
}

// --- Helpers ---

private fun parseWidgets(json: String): List<JsonObject> {
    return try {
        val obj = Json.decodeFromString<JsonObject>(json)
        val arr = obj["widgets"] as? JsonArray ?: return emptyList()
        arr.filterIsInstance<JsonObject>()
    } catch (_: Exception) {
        emptyList()
    }
}

private fun JsonObject.str(key: String): String? =
    (this[key] as? JsonPrimitive)?.content
