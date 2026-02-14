package com.playoutedge.persistence.repositories.impl

import com.playoutedge.persistence.repositories.OnboardingStateData
import com.playoutedge.persistence.repositories.OnboardingStateRepository
import com.playoutedge.persistence.tables.OnboardingStates
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class OnboardingStateRepositoryImpl : OnboardingStateRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun findByTenantId(tenantId: UUID): OnboardingStateData? =
        newSuspendedTransaction {
            OnboardingStates.selectAll()
                .where { OnboardingStates.tenantId eq tenantId }
                .firstOrNull()
                ?.let { row ->
                    OnboardingStateData(
                        currentStep = row[OnboardingStates.currentStep],
                        completedSteps = row[OnboardingStates.completedSteps]
                            .split(",").filter { it.isNotBlank() }.toSet(),
                        skippedSteps = row[OnboardingStates.skippedSteps]
                            .split(",").filter { it.isNotBlank() }.toSet(),
                        createdAssetId = row[OnboardingStates.createdAssetId],
                        createdChannelId = row[OnboardingStates.createdChannelId],
                        enrollCode = row[OnboardingStates.enrollCode],
                        contentPolicies = row[OnboardingStates.contentPolicies]?.toString()
                    )
                }
        }

    override suspend fun save(tenantId: UUID, state: OnboardingStateData) {
        newSuspendedTransaction {
            val existing = OnboardingStates.selectAll()
                .where { OnboardingStates.tenantId eq tenantId }
                .firstOrNull()

            val now = Clock.System.now()
            val policiesJson = state.contentPolicies?.let {
                json.decodeFromString(JsonObject.serializer(), it)
            }

            if (existing != null) {
                OnboardingStates.update({ OnboardingStates.tenantId eq tenantId }) {
                    it[currentStep] = state.currentStep
                    it[completedSteps] = state.completedSteps.joinToString(",")
                    it[skippedSteps] = state.skippedSteps.joinToString(",")
                    it[createdAssetId] = state.createdAssetId
                    it[createdChannelId] = state.createdChannelId
                    it[enrollCode] = state.enrollCode
                    it[contentPolicies] = policiesJson
                    it[updatedAt] = now
                }
            } else {
                OnboardingStates.insertAndGetId {
                    it[OnboardingStates.tenantId] = tenantId
                    it[currentStep] = state.currentStep
                    it[completedSteps] = state.completedSteps.joinToString(",")
                    it[skippedSteps] = state.skippedSteps.joinToString(",")
                    it[createdAssetId] = state.createdAssetId
                    it[createdChannelId] = state.createdChannelId
                    it[enrollCode] = state.enrollCode
                    it[contentPolicies] = policiesJson
                    it[updatedAt] = now
                    it[createdAt] = now
                }
            }
        }
    }

    override suspend fun delete(tenantId: UUID) {
        newSuspendedTransaction {
            OnboardingStates.deleteWhere { OnboardingStates.tenantId eq tenantId }
        }
    }
}
