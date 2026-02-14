package com.playoutedge.persistence.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserSessions : UUIDTable("user_sessions") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val ipAddress = varchar("ip_address", 45).nullable()
    val userAgent = varchar("user_agent", 500).nullable()
    val createdAt = timestamp("created_at")
    val lastActiveAt = timestamp("last_active_at")
    val isActive = bool("is_active").default(true)
}
