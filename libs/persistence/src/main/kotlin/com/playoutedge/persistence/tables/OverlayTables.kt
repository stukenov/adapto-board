package com.playoutedge.persistence.tables

import com.playoutedge.domain.enums.BindingStatus
import com.playoutedge.domain.enums.OverlaySourceType
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object OverlayProfiles : UUIDTable("overlay_profiles") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val definitionJson = jsonbColumn("definition_json")
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex("uq_overlay_profiles_tenant_name", tenantId, name)
    }
}

object OverlayBindings : UUIDTable("overlay_bindings") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val channelId = reference("channel_id", Channels, onDelete = ReferenceOption.CASCADE)
    val overlayProfileId = reference("overlay_profile_id", OverlayProfiles, onDelete = ReferenceOption.CASCADE)
    val sourceType = enumerationByName<OverlaySourceType>("source_type", 20)
    val sourceConfigJson = jsonbColumn("source_config_json")
    val status = enumerationByName<BindingStatus>("status", 20)
    val webhookSecret = varchar("webhook_secret", 255).nullable()
    val createdAt = timestamp("created_at")
}

object OverlayStates : UUIDTable("overlay_states") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val channelId = reference("channel_id", Channels, onDelete = ReferenceOption.CASCADE)
    val stateJson = jsonbColumn("state_json")
    val version = long("version").default(1)
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex("uq_overlay_states_tenant_channel", tenantId, channelId)
    }
}
