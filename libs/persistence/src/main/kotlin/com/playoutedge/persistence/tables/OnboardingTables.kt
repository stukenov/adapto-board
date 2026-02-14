package com.playoutedge.persistence.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object OnboardingStates : UUIDTable("onboarding_state") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val currentStep = varchar("current_step", 100)
    val completedSteps = text("completed_steps")
    val skippedSteps = text("skipped_steps")
    val createdAssetId = uuid("created_asset_id").nullable()
    val createdChannelId = uuid("created_channel_id").nullable()
    val enrollCode = varchar("enroll_code", 50).nullable()
    val contentPolicies = jsonbColumnNullable("content_policies")
    val updatedAt = timestamp("updated_at")
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex("uq_onboarding_states_tenant", tenantId)
    }
}
