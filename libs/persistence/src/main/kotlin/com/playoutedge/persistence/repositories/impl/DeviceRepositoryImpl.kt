package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.DeviceEnrollStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.DeviceEntity
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.persistence.repositories.UpdateDeviceRequest
import com.playoutedge.persistence.tables.Devices
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class DeviceRepositoryImpl : DeviceRepository {

    override suspend fun findById(tenantId: TenantId, deviceId: UUID): DeviceEntity? =
        newSuspendedTransaction {
            DeviceEntity.find {
                (Devices.id eq deviceId) and (Devices.tenantId eq tenantId.value)
            }.firstOrNull()
        }

    override suspend fun findAll(tenantId: TenantId): List<DeviceEntity> =
        newSuspendedTransaction {
            DeviceEntity.find { Devices.tenantId eq tenantId.value }.toList()
        }

    override suspend fun findAllPaged(tenantId: TenantId, limit: Int, offset: Int): Pair<List<DeviceEntity>, Long> =
        newSuspendedTransaction {
            val query = DeviceEntity.find { Devices.tenantId eq tenantId.value }
            val total = query.count()
            val items = query.limit(limit).offset(offset.toLong()).toList()
            Pair(items, total)
        }

    override suspend fun findByStatus(tenantId: TenantId, status: DeviceEnrollStatus): List<DeviceEntity> =
        newSuspendedTransaction {
            DeviceEntity.find {
                (Devices.tenantId eq tenantId.value) and (Devices.enrollStatus eq status)
            }.toList()
        }

    override suspend fun findByChannel(tenantId: TenantId, channelId: UUID): List<DeviceEntity> =
        newSuspendedTransaction {
            DeviceEntity.find {
                (Devices.tenantId eq tenantId.value) and (Devices.assignedChannelId eq channelId)
            }.toList()
        }

    override suspend fun update(tenantId: TenantId, deviceId: UUID, update: UpdateDeviceRequest): DeviceEntity? =
        newSuspendedTransaction {
            val entity = DeviceEntity.find {
                (Devices.id eq deviceId) and (Devices.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction null

            update.displayName?.let { entity.displayName = it }
            update.assignedChannelId?.let { entity.assignedChannelId = it }
            update.enrollStatus?.let { entity.enrollStatus = it }
            update.latitude?.let { entity.latitude = it }
            update.longitude?.let { entity.longitude = it }
            update.locationName?.let { entity.locationName = it }
            update.powerOnTime?.let { entity.powerOnTime = it }
            update.powerOffTime?.let { entity.powerOffTime = it }
            entity
        }

    override suspend fun updateHeartbeat(tenantId: TenantId, deviceId: UUID, appVersion: String?): DeviceEntity? =
        newSuspendedTransaction {
            val entity = DeviceEntity.find {
                (Devices.id eq deviceId) and (Devices.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction null

            entity.lastSeenAt = Clock.System.now()
            appVersion?.let { entity.appVersion = it }
            entity
        }

    override suspend fun delete(tenantId: TenantId, deviceId: UUID): Boolean =
        newSuspendedTransaction {
            val entity = DeviceEntity.find {
                (Devices.id eq deviceId) and (Devices.tenantId eq tenantId.value)
            }.firstOrNull() ?: return@newSuspendedTransaction false

            entity.delete()
            true
        }

    override suspend fun getEnrolledCount(tenantId: TenantId): Long =
        newSuspendedTransaction {
            DeviceEntity.find {
                (Devices.tenantId eq tenantId.value) and (Devices.enrollStatus eq DeviceEnrollStatus.ENROLLED)
            }.count()
        }
}
