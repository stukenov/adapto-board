package com.playoutedge.persistence.repositories

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.OverlayProfileEntity
import com.playoutedge.persistence.entities.OverlayStateEntity
import java.util.UUID

interface OverlayRepository {
    suspend fun findProfileById(tenantId: TenantId, profileId: UUID): OverlayProfileEntity?
    suspend fun findAllProfiles(tenantId: TenantId): List<OverlayProfileEntity>
    suspend fun createProfile(tenantId: TenantId, name: String, definitionJson: String): OverlayProfileEntity
    suspend fun deleteProfile(tenantId: TenantId, profileId: UUID): Boolean
    suspend fun getState(tenantId: TenantId, channelId: UUID): OverlayStateEntity?
    suspend fun setState(tenantId: TenantId, channelId: UUID, stateJson: String): OverlayStateEntity
    suspend fun updateState(tenantId: TenantId, channelId: UUID, stateJson: String, newVersion: Long): OverlayStateEntity?
}
