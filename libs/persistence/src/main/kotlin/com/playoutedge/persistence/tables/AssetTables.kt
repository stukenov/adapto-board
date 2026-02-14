package com.playoutedge.persistence.tables

import com.playoutedge.domain.enums.AssetProfile
import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.AssetType
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Assets : UUIDTable("assets") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val type = pgEnum<AssetType>("type", "asset_type")
    val name = varchar("name", 255)
    val status = pgEnum<AssetStatus>("status", "asset_status")
    val durationMs = integer("duration_ms").nullable()
    val mimeType = varchar("mime_type", 100)
    val checksumSha256 = varchar("checksum_sha256", 64).nullable()
    val storageKey = varchar("storage_key", 500)
    val width = integer("width").nullable()
    val height = integer("height").nullable()
    val fileSizeBytes = long("file_size_bytes").nullable()
    val createdBy = reference("created_by", Users, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = timestamp("created_at")
    val archivedAt = timestamp("archived_at").nullable()
    val rejectionReason = text("rejection_reason").nullable()
    val voiceoverKey = varchar("voiceover_key", 500).nullable()
}

object AssetVersions : UUIDTable("asset_versions") {
    val assetId = reference("asset_id", Assets, onDelete = ReferenceOption.CASCADE)
    val profile = pgEnum<AssetProfile>("profile", "asset_profile")
    val storageKey = varchar("storage_key", 500)
    val codec = varchar("codec", 50).nullable()
    val bitrate = integer("bitrate").nullable()
    val container = varchar("container", 20).nullable()
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex("uq_asset_versions_asset_profile", assetId, profile)
    }
}
