package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.ScheduleState
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.*
import com.playoutedge.persistence.repositories.CreateScheduleItemRequest
import com.playoutedge.persistence.repositories.ScheduleRepository
import com.playoutedge.persistence.tables.ScheduleItems
import com.playoutedge.persistence.tables.ScheduleVersions
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ScheduleRepositoryImpl : ScheduleRepository {

    override suspend fun findVersionById(tenantId: TenantId, versionId: UUID): ScheduleVersionEntity? =
        newSuspendedTransaction {
            ScheduleVersionEntity.find {
                (ScheduleVersions.id eq versionId) and (ScheduleVersions.tenantId eq tenantId.value)
            }.firstOrNull()
        }

    override suspend fun findVersionsByChannel(tenantId: TenantId, channelId: UUID): List<ScheduleVersionEntity> =
        newSuspendedTransaction {
            ScheduleVersionEntity.find {
                (ScheduleVersions.tenantId eq tenantId.value) and (ScheduleVersions.channelId eq channelId)
            }.toList()
        }

    override suspend fun findActiveVersion(tenantId: TenantId, channelId: UUID): ScheduleVersionEntity? =
        newSuspendedTransaction {
            ScheduleVersionEntity.find {
                (ScheduleVersions.tenantId eq tenantId.value) and
                (ScheduleVersions.channelId eq channelId) and
                (ScheduleVersions.state eq ScheduleState.PUBLISHED)
            }.firstOrNull()
        }

    override suspend fun findDraftVersion(tenantId: TenantId, channelId: UUID): ScheduleVersionEntity? =
        newSuspendedTransaction {
            ScheduleVersionEntity.find {
                (ScheduleVersions.tenantId eq tenantId.value) and
                (ScheduleVersions.channelId eq channelId) and
                (ScheduleVersions.state eq ScheduleState.DRAFT)
            }.firstOrNull()
        }

    override suspend fun createVersion(tenantId: TenantId, channelId: UUID, createdBy: UUID?): ScheduleVersionEntity =
        newSuspendedTransaction {
            val maxVersion = ScheduleVersionEntity.find {
                (ScheduleVersions.tenantId eq tenantId.value) and (ScheduleVersions.channelId eq channelId)
            }.maxOfOrNull { it.version } ?: 0

            ScheduleVersionEntity.new {
                tenant = TenantEntity[tenantId.value]
                channel = ChannelEntity[channelId]
                version = maxVersion + 1
                state = ScheduleState.DRAFT
                this.createdBy = createdBy?.let { UserEntity[it] }
                createdAt = Clock.System.now()
            }
        }

    override suspend fun updateVersionState(tenantId: TenantId, versionId: UUID, state: ScheduleState): ScheduleVersionEntity? =
        newSuspendedTransaction {
            val entity = ScheduleVersionEntity.find {
                (ScheduleVersions.id eq versionId) and (ScheduleVersions.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction null

            entity.state = state
            if (state == ScheduleState.PUBLISHED) {
                entity.publishedAt = Clock.System.now()
            }
            entity
        }

    override suspend fun findItemsByVersion(tenantId: TenantId, versionId: UUID): List<ScheduleItemEntity> =
        newSuspendedTransaction {
            ScheduleItemEntity.find {
                (ScheduleItems.tenantId eq tenantId.value) and (ScheduleItems.scheduleVersionId eq versionId)
            }.toList()
        }

    override suspend fun addItem(tenantId: TenantId, versionId: UUID, item: CreateScheduleItemRequest): ScheduleItemEntity =
        newSuspendedTransaction {
            ScheduleItemEntity.new {
                tenant = TenantEntity[tenantId.value]
                scheduleVersion = ScheduleVersionEntity[versionId]
                asset = AssetEntity[item.assetId]
                orderIndex = item.orderIndex
                validFrom = item.validFrom
                validTo = item.validTo
                daysOfWeek = item.daysOfWeek
                timeStart = item.timeStart
                timeEnd = item.timeEnd
                weight = item.weight
            }
        }

    override suspend fun removeItem(tenantId: TenantId, itemId: UUID): Boolean =
        newSuspendedTransaction {
            val entity = ScheduleItemEntity.find {
                (ScheduleItems.id eq itemId) and (ScheduleItems.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction false

            entity.delete()
            true
        }
}
