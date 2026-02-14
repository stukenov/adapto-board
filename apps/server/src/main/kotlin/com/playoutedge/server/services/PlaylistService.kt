package com.playoutedge.server.services

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.persistence.repositories.ScheduleRepository
import com.playoutedge.storage.StorageService
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.time.Duration.Companion.hours

data class PlaylistManifest(
    val scheduleVersionId: UUID,
    val version: Int,
    val items: List<PlaylistItem>,
    val fallbackAsset: PlaylistItem?
)

data class PlaylistItem(
    val assetId: UUID,
    val url: String,
    val checksum: String?,
    val durationMs: Int?,
    val assetType: String = "IMAGE",
    val orderIndex: Int,
    val validFrom: LocalDate?,
    val validTo: LocalDate?,
    val daysOfWeek: Int?,
    val timeStart: LocalTime?,
    val timeEnd: LocalTime?,
    val voiceoverUrl: String? = null,
    val weight: Int = 1
)

class PlaylistService(
    private val scheduleRepo: ScheduleRepository,
    private val assetRepo: AssetRepository,
    private val storageService: StorageService
) {
    suspend fun getManifest(tenantId: TenantId, channelId: UUID): PlaylistManifest? {
        val activeVersion = scheduleRepo.findActiveVersion(tenantId, channelId) ?: return null

        // Extract all item data inside a single transaction to avoid lazy reference errors
        data class ItemData(
            val assetId: UUID,
            val orderIndex: Int,
            val validFrom: LocalDate?,
            val validTo: LocalDate?,
            val daysOfWeek: Int?,
            val timeStart: LocalTime?,
            val timeEnd: LocalTime?,
            val voiceoverKey: String?,
            val weight: Int
        )

        val itemsData = newSuspendedTransaction {
            val items = scheduleRepo.findItemsByVersion(tenantId, activeVersion.id.value)
            items.map { item ->
                ItemData(
                    assetId = item.asset.id.value,
                    orderIndex = item.orderIndex,
                    validFrom = item.validFrom,
                    validTo = item.validTo,
                    daysOfWeek = item.daysOfWeek,
                    timeStart = item.timeStart,
                    timeEnd = item.timeEnd,
                    voiceoverKey = item.voiceoverKey,
                    weight = item.weight
                )
            }
        }

        val playlistItems = itemsData.mapNotNull { item ->
            val asset = assetRepo.findById(tenantId, item.assetId) ?: return@mapNotNull null
            val signedUrl = storageService.getSignedUrl(asset.storageKey, 1.hours)

            val voiceoverUrl = item.voiceoverKey?.let { key ->
                storageService.getSignedUrl(key, 1.hours)
            }

            PlaylistItem(
                assetId = asset.id.value,
                url = signedUrl,
                checksum = asset.checksumSha256,
                durationMs = asset.durationMs,
                assetType = asset.type.name,
                orderIndex = item.orderIndex,
                validFrom = item.validFrom,
                validTo = item.validTo,
                daysOfWeek = item.daysOfWeek,
                timeStart = item.timeStart,
                timeEnd = item.timeEnd,
                voiceoverUrl = voiceoverUrl,
                weight = item.weight
            )
        }.filter { item ->
            // Filter by date validity
            val tz = TimeZone.currentSystemDefault()
            val nowDt = kotlinx.datetime.Clock.System.now().toLocalDateTime(tz)
            val today: kotlinx.datetime.LocalDate = nowDt.date
            val validFromOk = item.validFrom == null || item.validFrom <= today
            val validToOk = item.validTo == null || item.validTo >= today

            // Filter by day of week (bit mask: Mon=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64)
            val dayOfWeekOk = item.daysOfWeek == null || run {
                val dayBit = when (today.dayOfWeek) {
                    kotlinx.datetime.DayOfWeek.MONDAY -> 1
                    kotlinx.datetime.DayOfWeek.TUESDAY -> 2
                    kotlinx.datetime.DayOfWeek.WEDNESDAY -> 4
                    kotlinx.datetime.DayOfWeek.THURSDAY -> 8
                    kotlinx.datetime.DayOfWeek.FRIDAY -> 16
                    kotlinx.datetime.DayOfWeek.SATURDAY -> 32
                    kotlinx.datetime.DayOfWeek.SUNDAY -> 64
                    else -> 0
                }
                (item.daysOfWeek and dayBit) != 0
            }

            // Filter by time window
            val now: kotlinx.datetime.LocalTime = nowDt.time
            val timeOk = (item.timeStart == null || now >= item.timeStart) &&
                         (item.timeEnd == null || now <= item.timeEnd)

            validFromOk && validToOk && dayOfWeekOk && timeOk
        }.sortedBy { it.orderIndex }

        return PlaylistManifest(
            scheduleVersionId = activeVersion.id.value,
            version = activeVersion.version,
            items = playlistItems,
            fallbackAsset = null
        )
    }
}
