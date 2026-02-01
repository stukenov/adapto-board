package com.playoutedge.persistence.entities

import com.playoutedge.persistence.tables.DeviceGroups
import com.playoutedge.persistence.tables.Devices
import com.playoutedge.persistence.tables.EnrollCodes
import com.playoutedge.persistence.tables.DeviceActions
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class DeviceEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DeviceEntity>(Devices)

    var tenant by TenantEntity referencedOn Devices.tenantId
    var displayName by Devices.displayName
    var enrollStatus by Devices.enrollStatus
    var deviceSecretHash by Devices.deviceSecretHash
    var assignedChannelId by Devices.assignedChannelId
    var lastSeenAt by Devices.lastSeenAt
    var appVersion by Devices.appVersion
    var androidModel by Devices.androidModel
    var androidVersion by Devices.androidVersion
    var revokedAt by Devices.revokedAt
    var createdAt by Devices.createdAt
}

class DeviceGroupEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DeviceGroupEntity>(DeviceGroups)

    var tenant by TenantEntity referencedOn DeviceGroups.tenantId
    var name by DeviceGroups.name
    var createdAt by DeviceGroups.createdAt
}

class EnrollCodeEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<EnrollCodeEntity>(EnrollCodes)

    var tenant by TenantEntity referencedOn EnrollCodes.tenantId
    var code by EnrollCodes.code
    var status by EnrollCodes.status
    var channel by ChannelEntity optionalReferencedOn EnrollCodes.channelId
    var expiresAt by EnrollCodes.expiresAt
    var usedAt by EnrollCodes.usedAt
    var usedByDevice by DeviceEntity optionalReferencedOn EnrollCodes.usedByDeviceId
    var createdBy by UserEntity optionalReferencedOn EnrollCodes.createdBy
    var createdAt by EnrollCodes.createdAt
}

class DeviceActionEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<DeviceActionEntity>(DeviceActions)

    var tenant by TenantEntity referencedOn DeviceActions.tenantId
    var device by DeviceEntity referencedOn DeviceActions.deviceId
    var action by DeviceActions.action
    var status by DeviceActions.status
    var paramsJson by DeviceActions.paramsJson
    var createdBy by UserEntity optionalReferencedOn DeviceActions.createdBy
    var createdAt by DeviceActions.createdAt
    var ackAt by DeviceActions.ackAt
    var expiresAt by DeviceActions.expiresAt
}
