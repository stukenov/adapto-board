package com.playoutedge.persistence.entities

import com.playoutedge.persistence.tables.Assets
import com.playoutedge.persistence.tables.AssetVersions
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class AssetEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AssetEntity>(Assets)

    var tenant by TenantEntity referencedOn Assets.tenantId
    var type by Assets.type
    var name by Assets.name
    var status by Assets.status
    var durationMs by Assets.durationMs
    var mimeType by Assets.mimeType
    var checksumSha256 by Assets.checksumSha256
    var storageKey by Assets.storageKey
    var width by Assets.width
    var height by Assets.height
    var fileSizeBytes by Assets.fileSizeBytes
    var createdBy by UserEntity optionalReferencedOn Assets.createdBy
    var createdAt by Assets.createdAt

    val versions by AssetVersionEntity referrersOn AssetVersions.assetId
}

class AssetVersionEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AssetVersionEntity>(AssetVersions)

    var asset by AssetEntity referencedOn AssetVersions.assetId
    var profile by AssetVersions.profile
    var storageKey by AssetVersions.storageKey
    var codec by AssetVersions.codec
    var bitrate by AssetVersions.bitrate
    var container by AssetVersions.container
    var createdAt by AssetVersions.createdAt
}
