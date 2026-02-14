package com.playoutedge.server.services

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.persistence.repositories.ScheduleRepository
import com.playoutedge.storage.StorageService
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
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

        val items = scheduleRepo.findItemsByVersion(tenantId, activeVersion.id.value)
        val playlistItems = items.mapNotNull { item ->
            val asset = assetRepo.findById(tenantId, item.asset.id.value) ?: return@mapNotNull null
            val signedUrl = storageService.getSignedUrl(asset.storageKey, 1.hours)

            // Generate voiceover signed URL if available
            val voiceoverUrl = item.voiceoverKey?.let { key ->
                storageService.getSignedUrl(key, 1.hours)
            }

            PlaylistItem(
                assetId = asset.id.value,
                url = signedUrl,
                checksum = asset.checksumSha256,
                durationMs = asset.durationMs,
                orderIndex = item.orderIndex,
                validFrom = item.validFrom,
                validTo = item.validTo,
                daysOfWeek = item.daysOfWeek,
                timeStart = item.timeStart,
                timeEnd = item.timeEnd,
                voiceoverUrl = voiceoverUrl,
                weight = item.weight
            )
        }.sortedWith(compareByDescending<PlaylistItem> { it.weight }.thenBy { it.orderIndex })

        return PlaylistManifest(
            scheduleVersionId = activeVersion.id.value,
            version = activeVersion.version,
            items = playlistItems,
            fallbackAsset = null
        )
    }
}
