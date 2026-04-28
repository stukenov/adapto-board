package com.playoutedge.persistence.entities

import com.playoutedge.persistence.tables.WidgetTemplates
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class WidgetTemplateEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<WidgetTemplateEntity>(WidgetTemplates)

    var tenant by TenantEntity optionalReferencedOn WidgetTemplates.tenantId
    var code by WidgetTemplates.code
    var name by WidgetTemplates.name
    var description by WidgetTemplates.description
    var category by WidgetTemplates.category
    var schemaJson by WidgetTemplates.schemaJson
    var htmlTemplate by WidgetTemplates.htmlTemplate
    var cssCode by WidgetTemplates.cssCode
    var jsCode by WidgetTemplates.jsCode
    var defaultDataJson by WidgetTemplates.defaultDataJson
    var previewHtml by WidgetTemplates.previewHtml
    var isActive by WidgetTemplates.isActive
    var version by WidgetTemplates.version
    var createdAt by WidgetTemplates.createdAt
    var updatedAt by WidgetTemplates.updatedAt
}
