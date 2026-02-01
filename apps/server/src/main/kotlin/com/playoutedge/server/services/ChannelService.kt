package com.playoutedge.server.services

import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.ChannelEntity
import com.playoutedge.persistence.repositories.ChannelRepository
import com.playoutedge.persistence.repositories.CreateChannelRequest
import com.playoutedge.persistence.repositories.UpdateChannelRequest
import java.util.UUID

class ChannelService(
    private val channelRepo: ChannelRepository
) {
    suspend fun create(tenantId: TenantId, name: String): ChannelEntity {
        return channelRepo.create(tenantId, CreateChannelRequest(name = name))
    }

    suspend fun findAll(tenantId: TenantId): List<ChannelEntity> {
        return channelRepo.findAll(tenantId)
    }

    suspend fun findById(tenantId: TenantId, channelId: UUID): ChannelEntity? {
        return channelRepo.findById(tenantId, channelId)
    }

    suspend fun update(
        tenantId: TenantId,
        channelId: UUID,
        name: String? = null,
        status: ChannelStatus? = null,
        defaultOverlayProfileId: UUID? = null
    ): ChannelEntity? {
        return channelRepo.update(
            tenantId,
            channelId,
            UpdateChannelRequest(
                name = name,
                status = status,
                defaultOverlayProfileId = defaultOverlayProfileId
            )
        )
    }

    suspend fun archive(tenantId: TenantId, channelId: UUID): Boolean {
        val updated = channelRepo.update(
            tenantId,
            channelId,
            UpdateChannelRequest(status = ChannelStatus.PAUSED)
        )
        return updated != null
    }
}
