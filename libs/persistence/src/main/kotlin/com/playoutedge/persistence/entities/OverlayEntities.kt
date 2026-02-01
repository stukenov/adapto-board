package com.playoutedge.persistence.entities

import com.playoutedge.persistence.tables.OverlayBindings
import com.playoutedge.persistence.tables.OverlayProfiles
import com.playoutedge.persistence.tables.OverlayStates
import com.playoutedge.persistence.tables.WebhookLogs
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class OverlayProfileEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<OverlayProfileEntity>(OverlayProfiles)

    var tenant by TenantEntity referencedOn OverlayProfiles.tenantId
    var name by OverlayProfiles.name
    var definitionJson by OverlayProfiles.definitionJson
    var createdAt by OverlayProfiles.createdAt
}

class OverlayBindingEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<OverlayBindingEntity>(OverlayBindings)

    var tenant by TenantEntity referencedOn OverlayBindings.tenantId
    var channel by ChannelEntity referencedOn OverlayBindings.channelId
    var overlayProfile by OverlayProfileEntity referencedOn OverlayBindings.overlayProfileId
    var sourceType by OverlayBindings.sourceType
    var sourceConfigJson by OverlayBindings.sourceConfigJson
    var status by OverlayBindings.status
    var webhookSecret by OverlayBindings.webhookSecret
    var createdAt by OverlayBindings.createdAt
}

class OverlayStateEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<OverlayStateEntity>(OverlayStates)

    var tenant by TenantEntity referencedOn OverlayStates.tenantId
    var channel by ChannelEntity referencedOn OverlayStates.channelId
    var stateJson by OverlayStates.stateJson
    var version by OverlayStates.version
    var updatedAt by OverlayStates.updatedAt
}

class WebhookLogEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<WebhookLogEntity>(WebhookLogs)

    var binding by OverlayBindingEntity referencedOn WebhookLogs.bindingId
    var statusCode by WebhookLogs.statusCode
    var latencyMs by WebhookLogs.latencyMs
    var payloadSize by WebhookLogs.payloadSize
    var error by WebhookLogs.error
    var createdAt by WebhookLogs.createdAt
}
