package com.playoutedge.persistence.entities

import com.playoutedge.persistence.tables.Alerts
import com.playoutedge.persistence.tables.Jobs
import com.playoutedge.persistence.tables.TenantContacts
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class TenantContactEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TenantContactEntity>(TenantContacts)

    var tenant by TenantEntity referencedOn TenantContacts.tenantId
    var type by TenantContacts.type
    var name by TenantContacts.name
    var email by TenantContacts.email
    var phone by TenantContacts.phone
    var createdAt by TenantContacts.createdAt
}

class AlertEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AlertEntity>(Alerts)

    var tenant by TenantEntity referencedOn Alerts.tenantId
    var type by Alerts.type
    var status by Alerts.status
    var payloadJson by Alerts.payloadJson
    var firstSeenAt by Alerts.firstSeenAt
    var lastSeenAt by Alerts.lastSeenAt
    var resolvedAt by Alerts.resolvedAt
}

class JobEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<JobEntity>(Jobs)

    var type by Jobs.type
    var payloadJson by Jobs.payloadJson
    var status by Jobs.status
    var nextRunAt by Jobs.nextRunAt
    var attempts by Jobs.attempts
    var maxAttempts by Jobs.maxAttempts
    var lockedBy by Jobs.lockedBy
    var lockedAt by Jobs.lockedAt
    var completedAt by Jobs.completedAt
    var errorMessage by Jobs.errorMessage
    var createdAt by Jobs.createdAt
}
