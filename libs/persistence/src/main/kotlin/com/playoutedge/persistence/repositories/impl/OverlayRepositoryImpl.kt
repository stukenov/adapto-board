package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.ChannelEntity
import com.playoutedge.persistence.entities.OverlayProfileEntity
import com.playoutedge.persistence.entities.OverlayStateEntity
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.repositories.OverlayRepository
import com.playoutedge.persistence.tables.OverlayProfiles
import com.playoutedge.persistence.tables.OverlayStates
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class OverlayRepositoryImpl : OverlayRepository {

    override suspend fun findProfileById(tenantId: TenantId, profileId: UUID): OverlayProfileEntity? =
        newSuspendedTransaction {
            OverlayProfileEntity.find {
                (OverlayProfiles.id eq profileId) and (OverlayProfiles.tenantId eq tenantId.value)
            }.firstOrNull()
        }

    override suspend fun findAllProfiles(tenantId: TenantId): List<OverlayProfileEntity> =
        newSuspendedTransaction {
            OverlayProfileEntity.find { OverlayProfiles.tenantId eq tenantId.value }.toList()
        }

    override suspend fun createProfile(
        tenantId: TenantId,
        name: String,
        definitionJson: String
    ): OverlayProfileEntity =
        newSuspendedTransaction {
            OverlayProfileEntity.new {
                tenant = TenantEntity[tenantId.value]
                this.name = name
                this.definitionJson = Json.decodeFromString<JsonObject>(definitionJson)
                createdAt = Clock.System.now()
            }
        }

    override suspend fun deleteProfile(tenantId: TenantId, profileId: UUID): Boolean =
        newSuspendedTransaction {
            val entity = OverlayProfileEntity.find {
                (OverlayProfiles.id eq profileId) and (OverlayProfiles.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction false

            entity.delete()
            true
        }

    override suspend fun getState(tenantId: TenantId, channelId: UUID): OverlayStateEntity? =
        newSuspendedTransaction {
            OverlayStateEntity.find {
                (OverlayStates.tenantId eq tenantId.value) and (OverlayStates.channelId eq channelId)
            }.firstOrNull()
        }

    override suspend fun setState(tenantId: TenantId, channelId: UUID, stateJson: String): OverlayStateEntity =
        newSuspendedTransaction {
            val existing = OverlayStateEntity.find {
                (OverlayStates.tenantId eq tenantId.value) and (OverlayStates.channelId eq channelId)
            }.firstOrNull()

            if (existing != null) {
                existing.stateJson = Json.decodeFromString<JsonObject>(stateJson)
                existing.version = existing.version + 1
                existing.updatedAt = Clock.System.now()
                existing
            } else {
                OverlayStateEntity.new {
                    tenant = TenantEntity[tenantId.value]
                    channel = ChannelEntity[channelId]
                    this.stateJson = Json.decodeFromString<JsonObject>(stateJson)
                    version = 1
                    updatedAt = Clock.System.now()
                }
            }
        }

    override suspend fun updateState(
        tenantId: TenantId,
        channelId: UUID,
        stateJson: String,
        newVersion: Long
    ): OverlayStateEntity? =
        newSuspendedTransaction {
            val entity = OverlayStateEntity.find {
                (OverlayStates.tenantId eq tenantId.value) and (OverlayStates.channelId eq channelId)
            }.firstOrNull() ?: return@newSuspendedTransaction null

            entity.stateJson = Json.decodeFromString<JsonObject>(stateJson)
            entity.version = newVersion
            entity.updatedAt = Clock.System.now()
            entity
        }
}
