package com.playoutedge.persistence.tables

import com.playoutedge.domain.enums.*
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object TenantContacts : UUIDTable("tenant_contacts") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val type = pgEnum<ContactType>("type", "contact_type")
    val name = varchar("name", 255)
    val email = varchar("email", 255).nullable()
    val phone = varchar("phone", 50).nullable()
    val createdAt = timestamp("created_at")
}

object DeviceActions : UUIDTable("device_actions") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val deviceId = reference("device_id", Devices, onDelete = ReferenceOption.CASCADE)
    val action = pgEnum<ActionType>("action", "action_type")
    val paramsJson = jsonbColumnNullable("params_json")
    val status = pgEnum<ActionStatus>("status", "action_status")
    val createdBy = reference("created_by", Users, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = timestamp("created_at")
    val ackAt = timestamp("ack_at").nullable()
    val expiresAt = timestamp("expires_at").nullable()
}

object Alerts : UUIDTable("alerts") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val type = pgEnum<AlertType>("type", "alert_type")
    val status = pgEnum<AlertStatus>("status", "alert_status")
    val payloadJson = jsonbColumn("payload_json")
    val firstSeenAt = timestamp("first_seen_at")
    val lastSeenAt = timestamp("last_seen_at")
    val resolvedAt = timestamp("resolved_at").nullable()
}

object Jobs : UUIDTable("jobs") {
    val type = varchar("type", 100)
    val payloadJson = jsonbColumn("payload_json")
    val status = pgEnum<JobStatus>("status", "job_status")
    val nextRunAt = timestamp("next_run_at")
    val attempts = integer("attempts").default(0)
    val maxAttempts = integer("max_attempts").default(3)
    val lockedBy = varchar("locked_by", 100).nullable()
    val lockedAt = timestamp("locked_at").nullable()
    val completedAt = timestamp("completed_at").nullable()
    val errorMessage = text("error_message").nullable()
    val createdAt = timestamp("created_at")
}
