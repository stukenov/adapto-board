package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.WidgetTemplateEntity
import com.playoutedge.persistence.repositories.WidgetTemplateRepository
import com.playoutedge.persistence.tables.WidgetTemplates
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class WidgetTemplateRepositoryImpl : WidgetTemplateRepository {

    override suspend fun findAll(tenantId: TenantId): List<WidgetTemplateEntity> =
        newSuspendedTransaction {
            // Return both global (tenant_id IS NULL) AND tenant-specific templates
            WidgetTemplateEntity.find {
                (WidgetTemplates.tenantId.isNull()) or (WidgetTemplates.tenantId eq tenantId.value)
            }
                .orderBy(WidgetTemplates.category to SortOrder.ASC, WidgetTemplates.name to SortOrder.ASC)
                .toList()
        }

    override suspend fun findByCode(tenantId: TenantId, code: String): WidgetTemplateEntity? =
        newSuspendedTransaction {
            // First look for tenant-specific template
            val tenantTemplate = WidgetTemplateEntity.find {
                (WidgetTemplates.tenantId eq tenantId.value) and (WidgetTemplates.code eq code)
            }.firstOrNull()

            if (tenantTemplate != null) {
                return@newSuspendedTransaction tenantTemplate
            }

            // Fall back to global template
            WidgetTemplateEntity.find {
                (WidgetTemplates.tenantId.isNull()) and (WidgetTemplates.code eq code)
            }.firstOrNull()
        }

    override suspend fun findById(id: UUID): WidgetTemplateEntity? =
        newSuspendedTransaction {
            WidgetTemplateEntity.findById(id)
        }

    override suspend fun findGlobal(): List<WidgetTemplateEntity> =
        newSuspendedTransaction {
            WidgetTemplateEntity.find {
                WidgetTemplates.tenantId.isNull()
            }
                .orderBy(WidgetTemplates.category to SortOrder.ASC, WidgetTemplates.name to SortOrder.ASC)
                .toList()
        }

    override suspend fun create(
        tenantId: TenantId?,
        code: String,
        name: String,
        description: String?,
        category: String,
        schemaJson: String,
        htmlTemplate: String,
        cssCode: String?,
        jsCode: String?,
        defaultDataJson: String,
        previewHtml: String?
    ): WidgetTemplateEntity =
        newSuspendedTransaction {
            val now = Clock.System.now()
            WidgetTemplateEntity.new {
                tenant = tenantId?.let { TenantEntity[it.value] }
                this.code = code
                this.name = name
                this.description = description
                this.category = category
                this.schemaJson = Json.decodeFromString<JsonObject>(schemaJson)
                this.htmlTemplate = htmlTemplate
                this.cssCode = cssCode
                this.jsCode = jsCode
                this.defaultDataJson = Json.decodeFromString<JsonObject>(defaultDataJson)
                this.previewHtml = previewHtml
                this.isActive = true
                this.version = 1
                this.createdAt = now
                this.updatedAt = now
            }
        }

    override suspend fun update(
        id: UUID,
        name: String,
        description: String?,
        category: String,
        schemaJson: String,
        htmlTemplate: String,
        cssCode: String?,
        jsCode: String?,
        defaultDataJson: String,
        previewHtml: String?
    ): WidgetTemplateEntity? =
        newSuspendedTransaction {
            val entity = WidgetTemplateEntity.findById(id) ?: return@newSuspendedTransaction null

            entity.name = name
            entity.description = description
            entity.category = category
            entity.schemaJson = Json.decodeFromString<JsonObject>(schemaJson)
            entity.htmlTemplate = htmlTemplate
            entity.cssCode = cssCode
            entity.jsCode = jsCode
            entity.defaultDataJson = Json.decodeFromString<JsonObject>(defaultDataJson)
            entity.previewHtml = previewHtml
            entity.version = entity.version + 1
            entity.updatedAt = Clock.System.now()

            entity
        }

    override suspend fun delete(id: UUID): Boolean =
        newSuspendedTransaction {
            val entity = WidgetTemplateEntity.findById(id) ?: return@newSuspendedTransaction false
            entity.delete()
            true
        }

    override suspend fun duplicate(id: UUID, tenantId: TenantId, newCode: String): WidgetTemplateEntity? =
        newSuspendedTransaction {
            val source = WidgetTemplateEntity.findById(id) ?: return@newSuspendedTransaction null

            val now = Clock.System.now()
            WidgetTemplateEntity.new {
                tenant = TenantEntity[tenantId.value]
                this.code = newCode
                this.name = source.name + " (Copy)"
                this.description = source.description
                this.category = source.category
                this.schemaJson = source.schemaJson
                this.htmlTemplate = source.htmlTemplate
                this.cssCode = source.cssCode
                this.jsCode = source.jsCode
                this.defaultDataJson = source.defaultDataJson
                this.previewHtml = source.previewHtml
                this.isActive = true
                this.version = 1
                this.createdAt = now
                this.updatedAt = now
            }
        }
}
