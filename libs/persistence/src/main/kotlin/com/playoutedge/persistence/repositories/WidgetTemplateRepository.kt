package com.playoutedge.persistence.repositories

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.WidgetTemplateEntity
import java.util.UUID

interface WidgetTemplateRepository {
    /**
     * Find all templates available to a tenant (global + tenant-specific).
     */
    suspend fun findAll(tenantId: TenantId): List<WidgetTemplateEntity>

    /**
     * Find template by code, checking tenant-specific first, then global.
     */
    suspend fun findByCode(tenantId: TenantId, code: String): WidgetTemplateEntity?

    /**
     * Find template by ID.
     */
    suspend fun findById(id: UUID): WidgetTemplateEntity?

    /**
     * Find all global templates (tenant_id IS NULL).
     */
    suspend fun findGlobal(): List<WidgetTemplateEntity>

    /**
     * Create a new widget template.
     */
    suspend fun create(
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
    ): WidgetTemplateEntity

    /**
     * Update an existing widget template.
     */
    suspend fun update(
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
    ): WidgetTemplateEntity?

    /**
     * Delete a widget template by ID.
     */
    suspend fun delete(id: UUID): Boolean

    /**
     * Duplicate a template for a specific tenant with a new code.
     */
    suspend fun duplicate(id: UUID, tenantId: TenantId, newCode: String): WidgetTemplateEntity?
}
