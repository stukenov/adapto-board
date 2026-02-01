package com.playoutedge.persistence.tables

import com.playoutedge.domain.enums.DeviceEnrollStatus
import com.playoutedge.domain.enums.EnrollCodeStatus
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Devices : UUIDTable("devices") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val displayName = varchar("display_name", 255)
    val enrollStatus = pgEnum<DeviceEnrollStatus>("enroll_status", "device_enroll_status")
    val deviceSecretHash = varchar("device_secret_hash", 255).nullable()
    val assignedChannelId = uuid("assigned_channel_id").nullable()
    val lastSeenAt = timestamp("last_seen_at").nullable()
    val appVersion = varchar("app_version", 50).nullable()
    val androidModel = varchar("android_model", 100).nullable()
    val androidVersion = varchar("android_version", 50).nullable()
    val revokedAt = timestamp("revoked_at").nullable()
    val createdAt = timestamp("created_at")
}

object EnrollCodes : UUIDTable("enroll_codes") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val code = varchar("code", 10)
    val status = pgEnum<EnrollCodeStatus>("status", "enroll_code_status")
    val channelId = reference("channel_id", Channels, onDelete = ReferenceOption.SET_NULL).nullable()
    val expiresAt = timestamp("expires_at")
    val usedAt = timestamp("used_at").nullable()
    val usedByDeviceId = reference("used_by_device_id", Devices, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdBy = reference("created_by", Users, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex("uq_enroll_codes_code", code)
    }
}

object DeviceGroups : UUIDTable("device_groups") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex("uq_device_groups_tenant_name", tenantId, name)
    }
}

object DeviceGroupMembers : Table("device_group_members") {
    val deviceId = reference("device_id", Devices, onDelete = ReferenceOption.CASCADE)
    val groupId = reference("group_id", DeviceGroups, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(deviceId, groupId)
}
