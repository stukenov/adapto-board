package com.playoutedge.persistence.repositories

import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.ChannelEntity
import java.util.UUID

interface ChannelRepository {
    suspend fun findById(tenantId: TenantId, channelId: UUID): ChannelEntity?
    suspend fun findAll(tenantId: TenantId): List<ChannelEntity>
    suspend fun findByStatus(tenantId: TenantId, status: ChannelStatus): List<ChannelEntity>
    suspend fun create(tenantId: TenantId, channel: CreateChannelRequest): ChannelEntity
    suspend fun update(tenantId: TenantId, channelId: UUID, update: UpdateChannelRequest): ChannelEntity?
    suspend fun delete(tenantId: TenantId, channelId: UUID): Boolean
}

data class CreateChannelRequest(
    val name: String,
    val status: ChannelStatus = ChannelStatus.ACTIVE
)

data class UpdateChannelRequest(
    val name: String? = null,
    val status: ChannelStatus? = null,
    val defaultOverlayProfileId: UUID? = null
)
