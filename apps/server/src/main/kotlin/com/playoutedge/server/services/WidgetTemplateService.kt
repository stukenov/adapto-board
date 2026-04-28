package com.playoutedge.server.services

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.WidgetTemplateEntity
import com.playoutedge.persistence.repositories.WidgetTemplateRepository
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class WidgetTemplateDTO(
    val id: UUID,
    val tenantId: UUID?,
    val code: String,
    val name: String,
    val description: String?,
    val category: String,
    val schemaJson: JsonObject,
    val htmlTemplate: String,
    val cssCode: String?,
    val jsCode: String?,
    val defaultDataJson: JsonObject,
    val previewHtml: String?,
    val isActive: Boolean,
    val isGlobal: Boolean
)

class WidgetTemplateService(
    private val repository: WidgetTemplateRepository
) {
    // Simple cache: tenantId -> templates (null key = global)
    private val cache = ConcurrentHashMap<UUID?, List<WidgetTemplateDTO>>()

    suspend fun getTemplates(tenantId: TenantId): List<WidgetTemplateDTO> {
        return cache.getOrPut(tenantId.value) {
            loadTemplates(tenantId)
        }
    }

    suspend fun getTemplateByCode(tenantId: TenantId, code: String): WidgetTemplateDTO? {
        return getTemplates(tenantId).firstOrNull { it.code == code }
    }

    suspend fun getTemplateById(id: UUID): WidgetTemplateDTO? {
        return newSuspendedTransaction {
            val entity = com.playoutedge.persistence.entities.WidgetTemplateEntity.findById(id)
                ?: return@newSuspendedTransaction null
            entity.tenant // eagerly load lazy ref
            entityToDTO(entity)
        }
    }

    /**
     * Get aggregated CSS for all active templates available to a tenant.
     */
    suspend fun getAggregatedCss(tenantId: TenantId): String {
        return getTemplates(tenantId)
            .filter { it.isActive }
            .mapNotNull { it.cssCode }
            .joinToString("\n\n")
    }

    /**
     * Get aggregated JS for all active templates available to a tenant.
     * Returns a JS object: WIDGET_TEMPLATES = { "code": { html, js }, ... }
     */
    suspend fun getTemplatesJs(tenantId: TenantId): String {
        val templates = getTemplates(tenantId).filter { it.isActive }
        val entries = templates.joinToString(",\n") { t ->
            val htmlEscaped = t.htmlTemplate.replace("\\", "\\\\").replace("`", "\\`").replace("\$", "\\\$")
            val jsEscaped = t.jsCode?.replace("\\", "\\\\")?.replace("`", "\\`")?.replace("\$", "\\\$")
            """  "${t.code}": { html: `${htmlEscaped}`, js: ${if (jsEscaped != null) "`$jsEscaped`" else "null"}, schema: ${t.schemaJson} }"""
        }
        return "var WIDGET_TEMPLATES = {\n$entries\n};"
    }

    /**
     * Build profile definition JSON for a template code.
     */
    suspend fun getTemplateDefinition(tenantId: TenantId, code: String): String {
        val template = getTemplateByCode(tenantId, code) ?: return """{"widgets":[]}"""
        val defaultData = template.defaultDataJson
        val widget = buildJsonObject {
            put("type", code)
            defaultData.forEach { (k, v) -> put(k, v) }
        }
        return buildJsonObject {
            put("widgets", buildJsonArray { add(widget) })
        }.toString()
    }

    /**
     * Build initial overlay state with defaults from template.
     */
    suspend fun buildInitialState(tenantId: TenantId, definitionJson: String): String {
        return try {
            val definition = Json.decodeFromString<JsonObject>(definitionJson)
            val widgets = definition["widgets"] as? JsonArray ?: return definitionJson

            val populatedWidgets = buildJsonArray {
                widgets.forEach { widget ->
                    val w = widget as? JsonObject ?: return@forEach
                    val type = (w["type"] as? JsonPrimitive)?.content ?: ""
                    val template = getTemplateByCode(tenantId, type)
                    add(populateFromTemplate(w, template))
                }
            }

            buildJsonObject {
                put("widgets", populatedWidgets)
                definition.forEach { (key, value) ->
                    if (key != "widgets") put(key, value)
                }
            }.toString()
        } catch (_: Exception) {
            definitionJson
        }
    }

    private fun populateFromTemplate(widget: JsonObject, template: WidgetTemplateDTO?): JsonObject {
        return buildJsonObject {
            // Start with template defaults
            template?.defaultDataJson?.forEach { (k, v) -> put(k, v) }
            // Override with widget's own properties
            widget.forEach { (k, v) -> put(k, v) }
        }
    }

    fun invalidateCache(tenantId: UUID? = null) {
        if (tenantId != null) {
            cache.remove(tenantId)
        } else {
            cache.clear()
        }
    }

    private suspend fun loadTemplates(tenantId: TenantId): List<WidgetTemplateDTO> {
        return newSuspendedTransaction {
            val entities = repository.findAll(tenantId)
            // Eagerly load lazy references before mapping
            entities.onEach { it.tenant }
            entities.map { entityToDTO(it) }
        }
    }

    private fun entityToDTO(entity: WidgetTemplateEntity): WidgetTemplateDTO {
        return WidgetTemplateDTO(
            id = entity.id.value,
            tenantId = entity.tenant?.id?.value,
            code = entity.code,
            name = entity.name,
            description = entity.description,
            category = entity.category,
            schemaJson = entity.schemaJson,
            htmlTemplate = entity.htmlTemplate,
            cssCode = entity.cssCode,
            jsCode = entity.jsCode,
            defaultDataJson = entity.defaultDataJson,
            previewHtml = entity.previewHtml,
            isActive = entity.isActive,
            isGlobal = entity.tenant == null
        )
    }
}
