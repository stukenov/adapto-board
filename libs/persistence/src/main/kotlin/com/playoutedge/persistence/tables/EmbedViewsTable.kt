package com.playoutedge.persistence.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object EmbedViews : UUIDTable("embed_views") {
    val channelId = reference("channel_id", Channels, onDelete = ReferenceOption.CASCADE)
    val ipAddress = varchar("ip_address", 45).nullable()
    val userAgent = text("user_agent").nullable()
    val referer = text("referer").nullable()
    val durationSeconds = integer("duration_seconds").nullable()
    val country = varchar("country", 2).nullable()
    val viewedAt = timestamp("viewed_at")
}
