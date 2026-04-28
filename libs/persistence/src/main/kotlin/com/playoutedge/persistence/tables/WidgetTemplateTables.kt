package com.playoutedge.persistence.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object WidgetTemplates : UUIDTable("widget_templates") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE).nullable()
    val code = varchar("code", 100)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val category = varchar("category", 50).default("general")
    val schemaJson = jsonbColumn("schema_json")
    val htmlTemplate = text("html_template")
    val cssCode = text("css_code").nullable()
    val jsCode = text("js_code").nullable()
    val defaultDataJson = jsonbColumn("default_data_json")
    val previewHtml = text("preview_html").nullable()
    val isActive = bool("is_active").default(true)
    val version = integer("version").default(1)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex("uq_widget_templates_tenant_code", tenantId, code)
    }
}
