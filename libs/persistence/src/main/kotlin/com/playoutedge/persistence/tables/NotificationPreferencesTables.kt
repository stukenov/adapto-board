package com.playoutedge.persistence.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object NotificationPreferences : UUIDTable("notification_preferences") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val emailOnDeviceOffline = bool("email_on_device_offline").default(true)
    val emailOnAlert = bool("email_on_alert").default(true)
    val emailOnSchedulePublish = bool("email_on_schedule_publish").default(false)
    val emailDailyDigest = bool("email_daily_digest").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
