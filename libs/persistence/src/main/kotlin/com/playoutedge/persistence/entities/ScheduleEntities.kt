package com.playoutedge.persistence.entities

import com.playoutedge.persistence.tables.Channels
import com.playoutedge.persistence.tables.ScheduleItems
import com.playoutedge.persistence.tables.ScheduleVersions
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class ChannelEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ChannelEntity>(Channels)

    var tenant by TenantEntity referencedOn Channels.tenantId
    var name by Channels.name
    var status by Channels.status
    var defaultOverlayProfileId by Channels.defaultOverlayProfileId
    var createdAt by Channels.createdAt

    val scheduleVersions by ScheduleVersionEntity referrersOn ScheduleVersions.channelId
}

class ScheduleVersionEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ScheduleVersionEntity>(ScheduleVersions)

    var tenant by TenantEntity referencedOn ScheduleVersions.tenantId
    var channel by ChannelEntity referencedOn ScheduleVersions.channelId
    var version by ScheduleVersions.version
    var state by ScheduleVersions.state
    var publishedAt by ScheduleVersions.publishedAt
    var createdBy by UserEntity optionalReferencedOn ScheduleVersions.createdBy
    var createdAt by ScheduleVersions.createdAt

    val items by ScheduleItemEntity referrersOn ScheduleItems.scheduleVersionId
}

class ScheduleItemEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ScheduleItemEntity>(ScheduleItems)

    var tenant by TenantEntity referencedOn ScheduleItems.tenantId
    var scheduleVersion by ScheduleVersionEntity referencedOn ScheduleItems.scheduleVersionId
    var asset by AssetEntity referencedOn ScheduleItems.assetId
    var orderIndex by ScheduleItems.orderIndex
    var validFrom by ScheduleItems.validFrom
    var validTo by ScheduleItems.validTo
    var daysOfWeek by ScheduleItems.daysOfWeek
    var timeStart by ScheduleItems.timeStart
    var timeEnd by ScheduleItems.timeEnd
    var weight by ScheduleItems.weight
    var voiceoverKey by ScheduleItems.voiceoverKey
    var storyGroupId by ScheduleItems.storyGroupId
}
