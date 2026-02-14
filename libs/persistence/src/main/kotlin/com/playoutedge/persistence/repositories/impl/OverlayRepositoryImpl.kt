package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.BindingStatus
import com.playoutedge.domain.enums.OverlaySourceType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.ChannelEntity
import com.playoutedge.persistence.entities.OverlayBindingEntity
import com.playoutedge.persistence.entities.OverlayProfileEntity
import com.playoutedge.persistence.entities.OverlayStateEntity
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.repositories.OverlayRepository
import com.playoutedge.persistence.tables.OverlayBindings
import com.playoutedge.persistence.tables.OverlayProfiles
import com.playoutedge.persistence.tables.OverlayStates
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class OverlayRepositoryImpl : OverlayRepository {

    // Profile operations

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

    override suspend fun updateProfile(
        tenantId: TenantId,
        profileId: UUID,
        name: String,
        definitionJson: String
    ): OverlayProfileEntity? =
        newSuspendedTransaction {
            val entity = OverlayProfileEntity.find {
                (OverlayProfiles.id eq profileId) and (OverlayProfiles.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction null

            entity.name = name
            entity.definitionJson = Json.decodeFromString<JsonObject>(definitionJson)
            entity
        }

    override suspend fun deleteProfile(tenantId: TenantId, profileId: UUID): Boolean =
        newSuspendedTransaction {
            val entity = OverlayProfileEntity.find {
                (OverlayProfiles.id eq profileId) and (OverlayProfiles.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction false

            entity.delete()
            true
        }

    // Binding operations

    override suspend fun findBindingById(tenantId: TenantId, bindingId: UUID): OverlayBindingEntity? =
        newSuspendedTransaction {
            OverlayBindingEntity.find {
                (OverlayBindings.id eq bindingId) and (OverlayBindings.tenantId eq tenantId.value)
            }.firstOrNull()
        }

    override suspend fun findAllBindings(tenantId: TenantId): List<OverlayBindingEntity> =
        newSuspendedTransaction {
            OverlayBindingEntity.find { OverlayBindings.tenantId eq tenantId.value }.toList()
        }

    override suspend fun findBindingsByProfile(tenantId: TenantId, profileId: UUID): List<OverlayBindingEntity> =
        newSuspendedTransaction {
            OverlayBindingEntity.find {
                (OverlayBindings.tenantId eq tenantId.value) and (OverlayBindings.overlayProfileId eq profileId)
            }.toList()
        }

    override suspend fun findBindingsByChannel(tenantId: TenantId, channelId: UUID): List<OverlayBindingEntity> =
        newSuspendedTransaction {
            OverlayBindingEntity.find {
                (OverlayBindings.tenantId eq tenantId.value) and (OverlayBindings.channelId eq channelId)
            }.toList()
        }

    override suspend fun createBinding(
        tenantId: TenantId,
        channelId: UUID,
        profileId: UUID,
        sourceType: OverlaySourceType,
        sourceConfigJson: String,
        webhookSecret: String?
    ): OverlayBindingEntity =
        newSuspendedTransaction {
            OverlayBindingEntity.new {
                tenant = TenantEntity[tenantId.value]
                channel = ChannelEntity[channelId]
                overlayProfile = OverlayProfileEntity[profileId]
                this.sourceType = sourceType
                this.sourceConfigJson = Json.decodeFromString<JsonObject>(sourceConfigJson)
                this.status = BindingStatus.ACTIVE
                this.webhookSecret = webhookSecret
                createdAt = Clock.System.now()
            }
        }

    override suspend fun updateBindingStatus(
        tenantId: TenantId,
        bindingId: UUID,
        status: BindingStatus
    ): OverlayBindingEntity? =
        newSuspendedTransaction {
            val entity = OverlayBindingEntity.find {
                (OverlayBindings.id eq bindingId) and (OverlayBindings.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction null

            entity.status = status
            entity
        }

    override suspend fun updateBindingConfig(
        tenantId: TenantId,
        bindingId: UUID,
        sourceConfigJson: String
    ): OverlayBindingEntity? =
        newSuspendedTransaction {
            val entity = OverlayBindingEntity.find {
                (OverlayBindings.id eq bindingId) and (OverlayBindings.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction null

            entity.sourceConfigJson = Json.decodeFromString<JsonObject>(sourceConfigJson)
            entity
        }

    override suspend fun deleteBinding(tenantId: TenantId, bindingId: UUID): Boolean =
        newSuspendedTransaction {
            val entity = OverlayBindingEntity.find {
                (OverlayBindings.id eq bindingId) and (OverlayBindings.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction false

            entity.delete()
            true
        }

    override suspend fun countBindingsByProfile(tenantId: TenantId, profileId: UUID): Long =
        newSuspendedTransaction {
            OverlayBindingEntity.find {
                (OverlayBindings.tenantId eq tenantId.value) and (OverlayBindings.overlayProfileId eq profileId)
            }.count()
        }

    // State operations

    override suspend fun findAllRestPullBindings(): List<OverlayBindingEntity> =
        newSuspendedTransaction {
            OverlayBindingEntity.find {
                (OverlayBindings.sourceType eq OverlaySourceType.REST_PULL) and
                    (OverlayBindings.status eq BindingStatus.ACTIVE)
            }.toList()
        }

    override suspend fun getStateByChannel(channelId: UUID): OverlayStateEntity? =
        newSuspendedTransaction {
            OverlayStateEntity.find {
                OverlayStates.channelId eq channelId
            }.firstOrNull()
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
