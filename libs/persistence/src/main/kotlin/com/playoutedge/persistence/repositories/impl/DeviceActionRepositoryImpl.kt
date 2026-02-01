package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.ActionStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.DeviceActionEntity
import com.playoutedge.persistence.entities.DeviceEntity
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.entities.UserEntity
import com.playoutedge.persistence.repositories.CreateDeviceAction
import com.playoutedge.persistence.repositories.DeviceActionRepository
import com.playoutedge.persistence.tables.DeviceActions
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID
import kotlin.time.Duration.Companion.hours

class DeviceActionRepositoryImpl : DeviceActionRepository {

    override suspend fun create(action: CreateDeviceAction): DeviceActionEntity =
        newSuspendedTransaction {
            val tenant = TenantEntity.findById(action.tenantId.value)
                ?: error("Tenant not found: ${action.tenantId.value}")
            val device = DeviceEntity.findById(action.deviceId)
                ?: error("Device not found: ${action.deviceId}")
            val createdBy = action.createdBy?.let { UserEntity.findById(it) }

            DeviceActionEntity.new {
                this.tenant = tenant
                this.device = device
                this.action = action.actionType
                this.status = ActionStatus.PENDING
                this.paramsJson = action.params
                this.createdBy = createdBy
                this.createdAt = Clock.System.now()
                this.expiresAt = Clock.System.now() + 24.hours
            }
        }

    override suspend fun findPending(tenantId: TenantId, deviceId: UUID): List<DeviceActionEntity> =
        newSuspendedTransaction {
            val now = Clock.System.now()
            DeviceActionEntity.find {
                (DeviceActions.tenantId eq tenantId.value) and
                    (DeviceActions.deviceId eq deviceId) and
                    (DeviceActions.status eq ActionStatus.PENDING)
            }.filter { entity ->
                entity.expiresAt == null || entity.expiresAt!! > now
            }.toList()
        }

    override suspend fun findByDevice(tenantId: TenantId, deviceId: UUID): List<DeviceActionEntity> =
        newSuspendedTransaction {
            DeviceActionEntity.find {
                (DeviceActions.tenantId eq tenantId.value) and
                    (DeviceActions.deviceId eq deviceId)
            }.orderBy(DeviceActions.createdAt to SortOrder.DESC)
                .limit(50)
                .toList()
        }

    override suspend fun acknowledge(actionId: UUID, status: ActionStatus): DeviceActionEntity? =
        newSuspendedTransaction {
            val entity = DeviceActionEntity.findById(actionId)
                ?: return@newSuspendedTransaction null

            if (entity.status != ActionStatus.PENDING) {
                return@newSuspendedTransaction null
            }

            entity.status = status
            entity.ackAt = Clock.System.now()
            entity
        }

    override suspend fun expireOldActions(): Long =
        newSuspendedTransaction {
            val now = Clock.System.now()
            DeviceActions.update({
                (DeviceActions.status eq ActionStatus.PENDING) and
                    (DeviceActions.expiresAt.isNotNull()) and
                    (DeviceActions.expiresAt less now)
            }) {
                it[status] = ActionStatus.EXPIRED
            }.toLong()
        }
}
