package com.playoutedge.persistence.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object DeviceLogs : UUIDTable("device_logs") {
    val deviceId = reference("device_id", Devices, onDelete = ReferenceOption.CASCADE)
    val level = varchar("level", 20)
    val message = text("message")
    val stackTrace = text("stack_trace").nullable()
    val createdAt = timestamp("created_at")
}

object DeviceScreenshots : UUIDTable("device_screenshots") {
    val deviceId = reference("device_id", Devices, onDelete = ReferenceOption.CASCADE)
    val storageKey = varchar("storage_key", 500)
    val createdAt = timestamp("created_at")
}
