package com.playoutedge.server.services

import kotlinx.serialization.json.*

/**
 * Simple Mustache-like template engine for rendering widget HTML templates.
 */
object TemplateEngine {

    fun render(template: String, data: JsonObject): String {
        var result = template
        // Process sections first ({{#section}}...{{/section}})
        result = processSections(result, data)
        // Process inverted sections ({{^section}}...{{/section}})
        result = processInvertedSections(result, data)
        // Process raw variables ({{{var}}})
        result = processRawVariables(result, data)
        // Process escaped variables ({{var}})
        result = processVariables(result, data)
        return result
    }

    private fun processSections(template: String, data: JsonObject): String {
        val regex = Regex("""\{\{#(\w[\w.]*)\}\}(.*?)\{\{/\1\}\}""", RegexOption.DOT_MATCHES_ALL)
        return regex.replace(template) { match ->
            val key = match.groupValues[1]
            val content = match.groupValues[2]
            val value = resolveValue(key, data)
            when {
                value is JsonArray -> value.joinToString("") { item ->
                    if (item is JsonObject) render(content, item)
                    else render(content, data) // fallback
                }
                value is JsonObject -> render(content, value)
                isTruthy(value) -> render(content, data)
                else -> ""
            }
        }
    }

    private fun processInvertedSections(template: String, data: JsonObject): String {
        val regex = Regex("""\{\{\^(\w[\w.]*)\}\}(.*?)\{\{/\1\}\}""", RegexOption.DOT_MATCHES_ALL)
        return regex.replace(template) { match ->
            val key = match.groupValues[1]
            val content = match.groupValues[2]
            val value = resolveValue(key, data)
            if (!isTruthy(value)) render(content, data) else ""
        }
    }

    private fun processRawVariables(template: String, data: JsonObject): String {
        val regex = Regex("""\{\{\{(\w[\w.]*)\}\}\}""")
        return regex.replace(template) { match ->
            val key = match.groupValues[1]
            resolveString(key, data)
        }
    }

    private fun processVariables(template: String, data: JsonObject): String {
        val regex = Regex("""\{\{(\w[\w.]*)\}\}""")
        return regex.replace(template) { match ->
            val key = match.groupValues[1]
            escapeHtml(resolveString(key, data))
        }
    }

    private fun resolveValue(key: String, data: JsonObject): JsonElement? {
        val parts = key.split(".")
        var current: JsonElement = data
        for (part in parts) {
            current = (current as? JsonObject)?.get(part) ?: return null
        }
        return current
    }

    private fun resolveString(key: String, data: JsonObject): String {
        val value = resolveValue(key, data) ?: return ""
        return when (value) {
            is JsonPrimitive -> value.content
            is JsonNull -> ""
            else -> value.toString()
        }
    }

    private fun isTruthy(value: JsonElement?): Boolean = when {
        value == null -> false
        value is JsonNull -> false
        value is JsonPrimitive && value.isString && value.content.isEmpty() -> false
        value is JsonPrimitive && value.booleanOrNull == false -> false
        value is JsonArray && value.isEmpty() -> false
        else -> true
    }

    private fun escapeHtml(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}
