package com.playoutedge.persistence.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ScheduledReports : UUIDTable("scheduled_reports") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val reportType = varchar("report_type", 100)
    val filtersJson = jsonbColumnNullable("filters_json")
    val format = varchar("format", 20)
    val schedule = varchar("schedule", 100)
    val recipients = text("recipients")
    val createdBy = reference("created_by", Users, onDelete = ReferenceOption.SET_NULL).nullable()
    val lastRun = timestamp("last_run").nullable()
    val createdAt = timestamp("created_at")
}
