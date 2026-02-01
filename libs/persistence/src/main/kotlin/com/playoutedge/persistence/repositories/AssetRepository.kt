package com.playoutedge.persistence.repositories

import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.AssetEntity
import java.util.UUID

interface AssetRepository {
    suspend fun findById(tenantId: TenantId, assetId: UUID): AssetEntity?
    suspend fun findAll(tenantId: TenantId): List<AssetEntity>
    suspend fun findAllActive(tenantId: TenantId): List<AssetEntity>
    suspend fun findByStatus(tenantId: TenantId, status: AssetStatus): List<AssetEntity>
    suspend fun create(tenantId: TenantId, asset: CreateAssetRequest): AssetEntity
    suspend fun update(tenantId: TenantId, assetId: UUID, update: UpdateAssetRequest): AssetEntity?
    suspend fun archive(tenantId: TenantId, assetId: UUID): AssetEntity?
    suspend fun delete(tenantId: TenantId, assetId: UUID): Boolean
    suspend fun getTotalStorageBytes(tenantId: TenantId): Long
}

data class CreateAssetRequest(
    val type: AssetType,
    val name: String,
    val mimeType: String,
    val storageKey: String,
    val fileSizeBytes: Long,
    val checksumSha256: String? = null,
    val durationMs: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val createdBy: UUID? = null
)

data class UpdateAssetRequest(
    val name: String? = null,
    val status: AssetStatus? = null,
    val checksumSha256: String? = null,
    val fileSizeBytes: Long? = null,
    val durationMs: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val rejectionReason: String? = null
)
