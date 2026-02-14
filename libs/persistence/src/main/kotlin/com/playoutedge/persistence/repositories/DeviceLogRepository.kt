package com.playoutedge.persistence.repositories

import kotlinx.datetime.Instant
import java.util.UUID

interface DeviceLogRepository {
    suspend fun findByDevice(deviceId: UUID, limit: Int = 100, offset: Int = 0): List<DeviceLogData>
    suspend fun create(deviceId: UUID, level: String, message: String, stackTrace: String? = null): UUID
    suspend fun countByDevice(deviceId: UUID): Long
}

data class DeviceLogData(
    val id: UUID,
    val deviceId: UUID,
    val level: String,
    val message: String,
    val stackTrace: String?,
    val createdAt: Instant
)
