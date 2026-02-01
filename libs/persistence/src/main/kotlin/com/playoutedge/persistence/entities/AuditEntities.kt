package com.playoutedge.persistence.entities

import com.playoutedge.persistence.tables.AuditLog
import com.playoutedge.persistence.tables.AsrunEvents
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class AuditLogEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AuditLogEntity>(AuditLog)

    var tenant by TenantEntity optionalReferencedOn AuditLog.tenantId
    var actorUser by UserEntity optionalReferencedOn AuditLog.actorUserId
    var actorType by AuditLog.actorType
    var action by AuditLog.action
    var entityType by AuditLog.entityType
    var entityId by AuditLog.entityId
    var diffJson by AuditLog.diffJson
    var createdAt by AuditLog.createdAt
}

class AsrunEventEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AsrunEventEntity>(AsrunEvents)

    var tenant by TenantEntity referencedOn AsrunEvents.tenantId
    var device by DeviceEntity referencedOn AsrunEvents.deviceId
    var channel by ChannelEntity optionalReferencedOn AsrunEvents.channelId
    var scheduleVersion by ScheduleVersionEntity optionalReferencedOn AsrunEvents.scheduleVersionId
    var asset by AssetEntity optionalReferencedOn AsrunEvents.assetId
    var eventType by AsrunEvents.eventType
    var at by AsrunEvents.at
    var detailsJson by AsrunEvents.detailsJson
    var createdAt by AsrunEvents.createdAt
}
