package com.playoutedge.persistence.entities

import com.playoutedge.persistence.tables.DeviceGroups
import com.playoutedge.persistence.tables.Devices
import com.playoutedge.persistence.tables.EnrollCodes
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
