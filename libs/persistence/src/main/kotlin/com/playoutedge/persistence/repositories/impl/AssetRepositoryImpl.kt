package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AssetEntity
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.UserEntity
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.persistence.repositories.CreateAssetRequest
import com.playoutedge.persistence.repositories.UpdateAssetRequest
import com.playoutedge.persistence.tables.Assets
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AssetRepositoryImpl : AssetRepository {

    override suspend fun findById(tenantId: TenantId, assetId: UUID): AssetEntity? =
        newSuspendedTransaction {
            AssetEntity.find {
                (Assets.id eq assetId) and (Assets.tenantId eq tenantId.value)
            }.firstOrNull()
        }

    override suspend fun findAll(tenantId: TenantId): List<AssetEntity> =
        newSuspendedTransaction {
            AssetEntity.find { Assets.tenantId eq tenantId.value }.toList()
        }

    override suspend fun findAllActive(tenantId: TenantId): List<AssetEntity> =
        newSuspendedTransaction {
            AssetEntity.find {
                (Assets.tenantId eq tenantId.value) and
                (Assets.status neq AssetStatus.ARCHIVED) and
                (Assets.archivedAt.isNull())
            }.toList()
        }

    override suspend fun findByStatus(tenantId: TenantId, status: AssetStatus): List<AssetEntity> =
        newSuspendedTransaction {
            AssetEntity.find {
                (Assets.tenantId eq tenantId.value) and (Assets.status eq status)
            }.toList()
        }

    override suspend fun create(tenantId: TenantId, asset: CreateAssetRequest): AssetEntity =
        newSuspendedTransaction {
            AssetEntity.new {
                tenant = TenantEntity[tenantId.value]
                type = asset.type
                name = asset.name
                status = AssetStatus.UPLOADING
                mimeType = asset.mimeType
                storageKey = asset.storageKey
                fileSizeBytes = asset.fileSizeBytes
                checksumSha256 = asset.checksumSha256
                durationMs = asset.durationMs
                width = asset.width
                height = asset.height
                createdBy = asset.createdBy?.let { UserEntity[it] }
                createdAt = Clock.System.now()
            }
        }

    override suspend fun update(tenantId: TenantId, assetId: UUID, update: UpdateAssetRequest): AssetEntity? =
        newSuspendedTransaction {
            val entity = AssetEntity.find {
                (Assets.id eq assetId) and (Assets.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction null

            update.name?.let { entity.name = it }
            update.status?.let { entity.status = it }
            update.checksumSha256?.let { entity.checksumSha256 = it }
            update.fileSizeBytes?.let { entity.fileSizeBytes = it }
            update.durationMs?.let { entity.durationMs = it }
            update.width?.let { entity.width = it }
            update.height?.let { entity.height = it }
            update.rejectionReason?.let { entity.rejectionReason = it }
            entity
        }

    override suspend fun archive(tenantId: TenantId, assetId: UUID): AssetEntity? =
        newSuspendedTransaction {
            val entity = AssetEntity.find {
                (Assets.id eq assetId) and (Assets.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction null

            entity.status = AssetStatus.ARCHIVED
            entity.archivedAt = Clock.System.now()
            entity
        }

    override suspend fun delete(tenantId: TenantId, assetId: UUID): Boolean =
        newSuspendedTransaction {
            val entity = AssetEntity.find {
                (Assets.id eq assetId) and (Assets.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction false

            entity.delete()
            true
        }

    override suspend fun getTotalStorageBytes(tenantId: TenantId): Long =
        newSuspendedTransaction {
            AssetEntity.find {
                (Assets.tenantId eq tenantId.value) and
                (Assets.status neq AssetStatus.ARCHIVED)
            }.sumOf { it.fileSizeBytes ?: 0L }
        }
}
