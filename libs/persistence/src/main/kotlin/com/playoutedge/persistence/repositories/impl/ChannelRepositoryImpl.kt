package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.ChannelEntity
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.repositories.ChannelRepository
import com.playoutedge.persistence.repositories.CreateChannelRequest
import com.playoutedge.persistence.repositories.UpdateChannelRequest
import com.playoutedge.persistence.tables.Channels
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ChannelRepositoryImpl : ChannelRepository {

    override suspend fun findByIdAnyTenant(channelId: UUID): ChannelEntity? =
        newSuspendedTransaction {
            ChannelEntity.findById(channelId)
        }

    override suspend fun findById(tenantId: TenantId, channelId: UUID): ChannelEntity? =
        newSuspendedTransaction {
            ChannelEntity.find {
                (Channels.id eq channelId) and (Channels.tenantId eq tenantId.value)
            }.firstOrNull()
        }

    override suspend fun findAll(tenantId: TenantId): List<ChannelEntity> =
        newSuspendedTransaction {
            ChannelEntity.find { Channels.tenantId eq tenantId.value }.toList()
        }

    override suspend fun findAllPaged(tenantId: TenantId, limit: Int, offset: Int): Pair<List<ChannelEntity>, Long> =
        newSuspendedTransaction {
            val query = ChannelEntity.find { Channels.tenantId eq tenantId.value }
            val total = query.count()
            val items = query.limit(limit).offset(offset.toLong()).toList()
            Pair(items, total)
        }

    override suspend fun findByStatus(tenantId: TenantId, status: ChannelStatus): List<ChannelEntity> =
        newSuspendedTransaction {
            ChannelEntity.find {
                (Channels.tenantId eq tenantId.value) and (Channels.status eq status)
            }.toList()
        }

    override suspend fun create(tenantId: TenantId, channel: CreateChannelRequest): ChannelEntity =
        newSuspendedTransaction {
            ChannelEntity.new {
                tenant = TenantEntity[tenantId.value]
                name = channel.name
                status = channel.status
                createdAt = Clock.System.now()
            }
        }

    override suspend fun update(tenantId: TenantId, channelId: UUID, update: UpdateChannelRequest): ChannelEntity? =
        newSuspendedTransaction {
            val entity = ChannelEntity.find {
                (Channels.id eq channelId) and (Channels.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction null

            update.name?.let { entity.name = it }
            update.status?.let { entity.status = it }
            update.defaultOverlayProfileId?.let { entity.defaultOverlayProfileId = it }
            entity
        }

    override suspend fun delete(tenantId: TenantId, channelId: UUID): Boolean =
        newSuspendedTransaction {
            val entity = ChannelEntity.find {
                (Channels.id eq channelId) and (Channels.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction false

            entity.delete()
            true
        }
}
