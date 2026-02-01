package com.playoutedge.persistence.tables

import com.playoutedge.domain.enums.ActorType
import com.playoutedge.domain.enums.AsrunEventType
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AuditLog : UUIDTable("audit_log") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.SET_NULL).nullable()
    val actorUserId = reference("actor_user_id", Users, onDelete = ReferenceOption.SET_NULL).nullable()
    val actorType = enumerationByName<ActorType>("actor_type", 20)
    val action = varchar("action", 100)
    val entityType = varchar("entity_type", 100)
    val entityId = uuid("entity_id")
    val diffJson = jsonbColumnNullable("diff_json")
    val createdAt = timestamp("created_at")
}

object AsrunEvents : UUIDTable("asrun_events") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val deviceId = reference("device_id", Devices, onDelete = ReferenceOption.CASCADE)
    val channelId = reference("channel_id", Channels, onDelete = ReferenceOption.SET_NULL).nullable()
    val scheduleVersionId = reference("schedule_version_id", ScheduleVersions, onDelete = ReferenceOption.SET_NULL).nullable()
    val assetId = reference("asset_id", Assets, onDelete = ReferenceOption.SET_NULL).nullable()
    val eventType = enumerationByName<AsrunEventType>("event_type", 20)
    val at = timestamp("at")
    val detailsJson = jsonbColumnNullable("details_json")
    val createdAt = timestamp("created_at")
}
