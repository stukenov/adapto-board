package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AssetEntity
import com.playoutedge.persistence.entities.TenantEntity
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
                name = asset.filename
                type = AssetType.VIDEO // Determine from mimeType in real impl
                status = AssetStatus.PROCESSING
                mimeType = asset.mimeType
                fileSizeBytes = asset.sizeBytes
                storageKey = asset.storageUrl
                createdAt = Clock.System.now()
            }
        }

    override suspend fun update(tenantId: TenantId, assetId: UUID, update: UpdateAssetRequest): AssetEntity? =
        newSuspendedTransaction {
            val entity = AssetEntity.find {
                (Assets.id eq assetId) and (Assets.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction null

            update.filename?.let { entity.name = it }
            update.status?.let { entity.status = it }
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
            AssetEntity.find { Assets.tenantId eq tenantId.value }
                .sumOf { it.fileSizeBytes ?: 0L }
        }
}
