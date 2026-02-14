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
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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

private data class ScheduleValidationItem(
    val assetName: String,
    val timeStart: LocalTime?,
    val timeEnd: LocalTime?,
    val daysOfWeek: Int
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

        // Extract asset IDs within transaction context via repository lookup
        val itemAssetIds = newSuspendedTransaction {
            items.map { it.asset.id.value }
        }

        val notReadyAssets = mutableListOf<UUID>()
        for (assetId in itemAssetIds) {
            val asset = assetRepo.findById(tenantId, assetId)
            if (asset == null || asset.status != AssetStatus.READY) {
                notReadyAssets.add(assetId)
            }
        }

        if (notReadyAssets.isNotEmpty()) {
            return PublishResult.AssetsNotReady(notReadyAssets)
        }

        val channelId = newSuspendedTransaction { version.channel.id.value }
        val previousActive = scheduleRepo.findActiveVersion(tenantId, channelId)
        if (previousActive != null) {
            scheduleRepo.updateVersionState(tenantId, previousActive.id.value, ScheduleState.ROLLED_BACK)
        }

        val published = scheduleRepo.updateVersionState(tenantId, versionId, ScheduleState.PUBLISHED)
        return PublishResult.Success(published!!)
    }

    /**
     * Validate a schedule version for time overlaps and gaps.
     */
    suspend fun validateSchedule(tenantId: TenantId, versionId: UUID): List<String> {
        val items = newSuspendedTransaction {
            scheduleRepo.findItemsByVersion(tenantId, versionId).map { item ->
                ScheduleValidationItem(
                    assetName = item.asset.name,
                    timeStart = item.timeStart,
                    timeEnd = item.timeEnd,
                    daysOfWeek = item.daysOfWeek ?: 127
                )
            }
        }

        val warnings = mutableListOf<String>()

        if (items.isEmpty()) {
            warnings.add("Schedule is empty.")
            return warnings
        }

        // Check for time overlaps between items on the same days
        for (i in items.indices) {
            for (j in i + 1 until items.size) {
                val a = items[i]
                val b = items[j]
                // Check if they share any day
                if (a.daysOfWeek and b.daysOfWeek == 0) continue
                // Check time overlap
                val aStart = a.timeStart ?: LocalTime(0, 0)
                val aEnd = a.timeEnd ?: LocalTime(23, 59)
                val bStart = b.timeStart ?: LocalTime(0, 0)
                val bEnd = b.timeEnd ?: LocalTime(23, 59)
                if (aStart < bEnd && bStart < aEnd) {
                    warnings.add("Overlap: \"${a.assetName}\" (${aStart}-${aEnd}) and \"${b.assetName}\" (${bStart}-${bEnd})")
                }
            }
        }

        // Check for gaps in 24-hour coverage (only for items with explicit times)
        val timedItems = items.filter { it.timeStart != null || it.timeEnd != null }
        if (timedItems.isNotEmpty()) {
            val sortedByStart = timedItems.sortedBy { it.timeStart ?: LocalTime(0, 0) }
            var lastEnd = LocalTime(0, 0)
            for (item in sortedByStart) {
                val start = item.timeStart ?: LocalTime(0, 0)
                if (start > lastEnd) {
                    warnings.add("Gap: No content scheduled between $lastEnd and $start")
                }
                val end = item.timeEnd ?: LocalTime(23, 59)
                if (end > lastEnd) lastEnd = end
            }
            if (lastEnd < LocalTime(23, 59)) {
                warnings.add("Gap: No content scheduled after $lastEnd until end of day")
            }
        }

        return warnings
    }

    /**
     * Copy the active schedule from one channel to another, creating a draft on the target.
     */
    suspend fun copySchedule(tenantId: TenantId, fromChannelId: UUID, toChannelId: UUID, createdBy: UUID?): UUID? {
        return newSuspendedTransaction {
            val sourceVersion = scheduleRepo.findActiveVersion(tenantId, fromChannelId)
                ?: return@newSuspendedTransaction null

            val sourceItems = scheduleRepo.findItemsByVersion(tenantId, sourceVersion.id.value)

            val draft = scheduleRepo.createVersion(tenantId, toChannelId, createdBy)

            sourceItems.forEach { item ->
                scheduleRepo.addItem(
                    tenantId,
                    draft.id.value,
                    CreateScheduleItemRequest(
                        assetId = item.asset.id.value,
                        orderIndex = item.orderIndex,
                        validFrom = item.validFrom,
                        validTo = item.validTo,
                        daysOfWeek = item.daysOfWeek,
                        timeStart = item.timeStart,
                        timeEnd = item.timeEnd,
                        weight = item.weight
                    )
                )
            }

            draft.id.value
        }
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
        val itemAssetIds = newSuspendedTransaction {
            items.map { it.asset.id.value }
        }
        val unavailableAssets = mutableListOf<UUID>()

        for (assetId in itemAssetIds) {
            val asset = assetRepo.findById(tenantId, assetId)
            if (asset == null || asset.status == AssetStatus.ARCHIVED) {
                unavailableAssets.add(assetId)
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
