package com.playoutedge.server.services

import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.ScheduleState
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.ScheduleItemEntity
import com.playoutedge.persistence.entities.ScheduleVersionEntity
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.persistence.repositories.CreateScheduleItemRequest
import com.playoutedge.persistence.repositories.ScheduleRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import java.util.UUID

sealed class PublishResult {
    data class Success(val version: ScheduleVersionEntity) : PublishResult()
    data class AssetsNotReady(val assetIds: List<UUID>) : PublishResult()
    data object ScheduleEmpty : PublishResult()
    data object VersionNotFound : PublishResult()
    data object VersionNotDraft : PublishResult()
}

sealed class RollbackResult {
    data class Success(val version: ScheduleVersionEntity) : RollbackResult()
    data class AssetsUnavailable(val assetIds: List<UUID>) : RollbackResult()
    data object VersionNotFound : RollbackResult()
}

data class ScheduleItemInput(
    val assetId: UUID,
    val orderIndex: Int,
    val validFrom: LocalDate? = null,
    val validTo: LocalDate? = null,
    val daysOfWeek: Int? = null,
    val timeStart: LocalTime? = null,
    val timeEnd: LocalTime? = null,
    val weight: Int = 1
)

class ScheduleService(
    private val scheduleRepo: ScheduleRepository,
    private val assetRepo: AssetRepository
) {
    suspend fun createDraft(tenantId: TenantId, channelId: UUID, createdBy: UUID?): ScheduleVersionEntity {
        return scheduleRepo.createVersion(tenantId, channelId, createdBy)
    }

    suspend fun findVersionById(tenantId: TenantId, versionId: UUID): ScheduleVersionEntity? {
        return scheduleRepo.findVersionById(tenantId, versionId)
    }

    suspend fun findVersionsByChannel(tenantId: TenantId, channelId: UUID): List<ScheduleVersionEntity> {
        return scheduleRepo.findVersionsByChannel(tenantId, channelId)
    }

    suspend fun getActiveVersion(tenantId: TenantId, channelId: UUID): ScheduleVersionEntity? {
        return scheduleRepo.findActiveVersion(tenantId, channelId)
    }

    suspend fun getDraftVersion(tenantId: TenantId, channelId: UUID): ScheduleVersionEntity? {
        return scheduleRepo.findDraftVersion(tenantId, channelId)
    }

    suspend fun getItemsByVersion(tenantId: TenantId, versionId: UUID): List<ScheduleItemEntity> {
        return scheduleRepo.findItemsByVersion(tenantId, versionId)
    }

    suspend fun replaceItems(
        tenantId: TenantId,
        versionId: UUID,
        items: List<ScheduleItemInput>
    ): List<ScheduleItemEntity> {
        val version = scheduleRepo.findVersionById(tenantId, versionId) ?: return emptyList()

        if (version.state != ScheduleState.DRAFT) {
            return emptyList()
        }

        val existingItems = scheduleRepo.findItemsByVersion(tenantId, versionId)
        for (item in existingItems) {
            scheduleRepo.removeItem(tenantId, item.id.value)
        }

        return items.map { input ->
            scheduleRepo.addItem(
                tenantId,
                versionId,
                CreateScheduleItemRequest(
                    assetId = input.assetId,
                    orderIndex = input.orderIndex,
                    validFrom = input.validFrom,
                    validTo = input.validTo,
                    daysOfWeek = input.daysOfWeek,
                    timeStart = input.timeStart,
                    timeEnd = input.timeEnd,
                    weight = input.weight
                )
            )
        }
    }

    suspend fun publish(tenantId: TenantId, versionId: UUID): PublishResult {
        val version = scheduleRepo.findVersionById(tenantId, versionId)
            ?: return PublishResult.VersionNotFound

        if (version.state != ScheduleState.DRAFT) {
            return PublishResult.VersionNotDraft
        }

        val items = scheduleRepo.findItemsByVersion(tenantId, versionId)
        if (items.isEmpty()) {
            return PublishResult.ScheduleEmpty
        }

        val notReadyAssets = mutableListOf<UUID>()
        for (item in items) {
            val asset = assetRepo.findById(tenantId, item.asset.id.value)
            if (asset == null || asset.status != AssetStatus.READY) {
                notReadyAssets.add(item.asset.id.value)
            }
        }

        if (notReadyAssets.isNotEmpty()) {
            return PublishResult.AssetsNotReady(notReadyAssets)
        }

        val previousActive = scheduleRepo.findActiveVersion(tenantId, version.channel.id.value)
        if (previousActive != null) {
            scheduleRepo.updateVersionState(tenantId, previousActive.id.value, ScheduleState.ROLLED_BACK)
        }

        val published = scheduleRepo.updateVersionState(tenantId, versionId, ScheduleState.PUBLISHED)
        return PublishResult.Success(published!!)
    }

    suspend fun rollback(
        tenantId: TenantId,
        channelId: UUID,
        toVersion: Int
    ): RollbackResult {
        val versions = scheduleRepo.findVersionsByChannel(tenantId, channelId)
        val targetVersion = versions.find { it.version == toVersion }
            ?: return RollbackResult.VersionNotFound

        val items = scheduleRepo.findItemsByVersion(tenantId, targetVersion.id.value)
        val unavailableAssets = mutableListOf<UUID>()

        for (item in items) {
            val asset = assetRepo.findById(tenantId, item.asset.id.value)
            if (asset == null || asset.status == AssetStatus.ARCHIVED) {
                unavailableAssets.add(item.asset.id.value)
            }
        }

        if (unavailableAssets.isNotEmpty()) {
            return RollbackResult.AssetsUnavailable(unavailableAssets)
        }

        val currentActive = scheduleRepo.findActiveVersion(tenantId, channelId)
        if (currentActive != null) {
            scheduleRepo.updateVersionState(tenantId, currentActive.id.value, ScheduleState.ROLLED_BACK)
        }

        val rolledBack = scheduleRepo.updateVersionState(tenantId, targetVersion.id.value, ScheduleState.PUBLISHED)
        return RollbackResult.Success(rolledBack!!)
    }
}
