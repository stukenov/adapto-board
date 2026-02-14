package com.playoutedge.persistence.repositories.impl

import com.playoutedge.persistence.repositories.DeviceLogData
import com.playoutedge.persistence.repositories.DeviceLogRepository
import com.playoutedge.persistence.tables.DeviceLogs
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class DeviceLogRepositoryImpl : DeviceLogRepository {

    override suspend fun findByDevice(deviceId: UUID, limit: Int, offset: Int): List<DeviceLogData> =
        newSuspendedTransaction {
            DeviceLogs.selectAll()
                .where { DeviceLogs.deviceId eq deviceId }
                .orderBy(DeviceLogs.createdAt, SortOrder.DESC)
                .limit(limit)
                .offset(offset.toLong())
                .map { row ->
                    DeviceLogData(
                        id = row[DeviceLogs.id].value,
                        deviceId = row[DeviceLogs.deviceId].value,
                        level = row[DeviceLogs.level],
                        message = row[DeviceLogs.message],
                        stackTrace = row[DeviceLogs.stackTrace],
                        createdAt = row[DeviceLogs.createdAt]
                    )
                }
        }

    override suspend fun create(deviceId: UUID, level: String, message: String, stackTrace: String?): UUID =
        newSuspendedTransaction {
            DeviceLogs.insertAndGetId {
                it[DeviceLogs.deviceId] = deviceId
                it[DeviceLogs.level] = level
                it[DeviceLogs.message] = message
                it[DeviceLogs.stackTrace] = stackTrace
                it[createdAt] = Clock.System.now()
            }.value
        }

    override suspend fun countByDevice(deviceId: UUID): Long =
        newSuspendedTransaction {
            DeviceLogs.selectAll()
                .where { DeviceLogs.deviceId eq deviceId }
                .count()
        }
}
