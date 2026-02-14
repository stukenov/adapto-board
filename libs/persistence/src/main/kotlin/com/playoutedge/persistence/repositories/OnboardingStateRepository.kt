package com.playoutedge.persistence.repositories

import java.util.UUID

interface OnboardingStateRepository {
    suspend fun findByTenantId(tenantId: UUID): OnboardingStateData?
    suspend fun save(tenantId: UUID, state: OnboardingStateData)
    suspend fun delete(tenantId: UUID)
}

data class OnboardingStateData(
    val currentStep: String,
    val completedSteps: Set<String>,
    val skippedSteps: Set<String>,
    val createdAssetId: UUID? = null,
    val createdChannelId: UUID? = null,
    val enrollCode: String? = null,
    val contentPolicies: String? = null
)
