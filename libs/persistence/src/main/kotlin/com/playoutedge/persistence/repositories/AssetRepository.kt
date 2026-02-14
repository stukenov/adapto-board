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
    suspend fun findAllActivePaged(tenantId: TenantId, limit: Int = 50, offset: Int = 0): Pair<List<AssetEntity>, Long>
    suspend fun findByStatus(tenantId: TenantId, status: AssetStatus): List<AssetEntity>
    suspend fun create(tenantId: TenantId, asset: CreateAssetRequest): AssetEntity
    suspend fun update(tenantId: TenantId, assetId: UUID, update: UpdateAssetRequest): AssetEntity?
    suspend fun archive(tenantId: TenantId, assetId: UUID): AssetEntity?
    suspend fun delete(tenantId: TenantId, assetId: UUID): Boolean
    suspend fun getTotalStorageBytes(tenantId: TenantId): Long
    suspend fun updateMetadata(tenantId: TenantId, assetId: UUID, description: String?, tags: String?): Boolean
    suspend fun findByChecksumSha256(tenantId: TenantId, checksum: String): List<AssetEntity>
    suspend fun getAllTags(tenantId: TenantId): List<String>
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
    val createdBy: UUID? = null,
    val description: String? = null,
    val tags: String? = null
)

data class UpdateAssetRequest(
    val name: String? = null,
    val status: AssetStatus? = null,
    val checksumSha256: String? = null,
    val fileSizeBytes: Long? = null,
    val durationMs: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val rejectionReason: String? = null,
    val description: String? = null,
    val tags: String? = null,
    val storageKey: String? = null,
    val mimeType: String? = null,
    val approvalStatus: String? = null,
    val expiresAt: kotlinx.datetime.Instant? = null,
    val slideshowDefinition: kotlinx.serialization.json.JsonObject? = null
)
