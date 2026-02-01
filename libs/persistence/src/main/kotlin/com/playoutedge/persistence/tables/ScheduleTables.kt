package com.playoutedge.persistence.tables

import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.domain.enums.ScheduleState
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.time
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Channels : UUIDTable("channels") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val status = pgEnum<ChannelStatus>("status", "channel_status")
    val defaultOverlayProfileId = uuid("default_overlay_profile_id").nullable()
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex("uq_channels_tenant_name", tenantId, name)
    }
}

object ScheduleVersions : UUIDTable("schedule_versions") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val channelId = reference("channel_id", Channels, onDelete = ReferenceOption.CASCADE)
    val version = integer("version")
    val state = pgEnum<ScheduleState>("state", "schedule_state")
    val publishedAt = timestamp("published_at").nullable()
    val createdBy = reference("created_by", Users, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex("uq_schedule_versions_tenant_channel_version", tenantId, channelId, version)
    }
}

object ScheduleItems : UUIDTable("schedule_items") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val scheduleVersionId = reference("schedule_version_id", ScheduleVersions, onDelete = ReferenceOption.CASCADE)
    val assetId = reference("asset_id", Assets, onDelete = ReferenceOption.CASCADE)
    val orderIndex = integer("order_index")
    val validFrom = date("valid_from").nullable()
    val validTo = date("valid_to").nullable()
    val daysOfWeek = integer("days_of_week").nullable()
    val timeStart = time("time_start").nullable()
    val timeEnd = time("time_end").nullable()
    val weight = integer("weight").default(1)

    init {
        uniqueIndex("uq_schedule_items_version_order", tenantId, scheduleVersionId, orderIndex)
    }
}
