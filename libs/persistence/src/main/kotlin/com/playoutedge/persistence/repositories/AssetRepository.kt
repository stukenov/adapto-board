package com.playoutedge.persistence.repositories

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AssetEntity
import java.util.UUID

interface AssetRepository {
    suspend fun findById(tenantId: TenantId, assetId: UUID): AssetEntity?
    suspend fun findAll(tenantId: TenantId): List<AssetEntity>
    suspend fun findByStatus(tenantId: TenantId, status: com.playoutedge.domain.enums.AssetStatus): List<AssetEntity>
    suspend fun create(tenantId: TenantId, asset: CreateAssetRequest): AssetEntity
    suspend fun update(tenantId: TenantId, assetId: UUID, update: UpdateAssetRequest): AssetEntity?
    suspend fun delete(tenantId: TenantId, assetId: UUID): Boolean
    suspend fun getTotalStorageBytes(tenantId: TenantId): Long
}

data class CreateAssetRequest(
    val filename: String,
    val mimeType: String,
    val sizeBytes: Long,
    val storageUrl: String
)

data class UpdateAssetRequest(
    val filename: String? = null,
    val status: com.playoutedge.domain.enums.AssetStatus? = null
)
