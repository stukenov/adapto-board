package com.playoutedge.persistence.repositories

import com.playoutedge.domain.enums.BindingStatus
import com.playoutedge.domain.enums.OverlaySourceType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.OverlayBindingEntity
import com.playoutedge.persistence.entities.OverlayProfileEntity
import com.playoutedge.persistence.entities.OverlayStateEntity
import java.util.UUID

interface OverlayRepository {
    // Profile operations
    suspend fun findProfileById(tenantId: TenantId, profileId: UUID): OverlayProfileEntity?
    suspend fun findAllProfiles(tenantId: TenantId): List<OverlayProfileEntity>
    suspend fun createProfile(tenantId: TenantId, name: String, definitionJson: String): OverlayProfileEntity
    suspend fun updateProfile(tenantId: TenantId, profileId: UUID, name: String, definitionJson: String): OverlayProfileEntity?
    suspend fun deleteProfile(tenantId: TenantId, profileId: UUID): Boolean

    // Binding operations
    suspend fun findBindingById(tenantId: TenantId, bindingId: UUID): OverlayBindingEntity?
    suspend fun findAllBindings(tenantId: TenantId): List<OverlayBindingEntity>
    suspend fun findBindingsByProfile(tenantId: TenantId, profileId: UUID): List<OverlayBindingEntity>
    suspend fun findBindingsByChannel(tenantId: TenantId, channelId: UUID): List<OverlayBindingEntity>
    suspend fun createBinding(
        tenantId: TenantId,
        channelId: UUID,
        profileId: UUID,
        sourceType: OverlaySourceType,
        sourceConfigJson: String,
        webhookSecret: String?
    ): OverlayBindingEntity
    suspend fun updateBindingStatus(tenantId: TenantId, bindingId: UUID, status: BindingStatus): OverlayBindingEntity?
    suspend fun updateBindingConfig(tenantId: TenantId, bindingId: UUID, sourceConfigJson: String): OverlayBindingEntity?
    suspend fun deleteBinding(tenantId: TenantId, bindingId: UUID): Boolean
    suspend fun countBindingsByProfile(tenantId: TenantId, profileId: UUID): Long

    // State operations
    suspend fun getState(tenantId: TenantId, channelId: UUID): OverlayStateEntity?
    suspend fun setState(tenantId: TenantId, channelId: UUID, stateJson: String): OverlayStateEntity
    suspend fun updateState(tenantId: TenantId, channelId: UUID, stateJson: String, newVersion: Long): OverlayStateEntity?
}
