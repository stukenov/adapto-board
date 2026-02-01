package com.playoutedge.player.playlist

import java.util.Calendar

/**
 * Calculates active playlist items based on time windows.
 */
class PlaylistCalculator {

    /**
     * Get items that should play now.
     */
    fun getActiveItems(manifest: PlaylistManifest): List<PlaylistItem> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val activeItems = manifest.items.filter { item ->
            isItemActive(item, now, calendar)
        }.sortedBy { it.orderIndex }

        // If no active items, use fallback or all items
        return if (activeItems.isEmpty()) {
            manifest.fallbackItem?.let { listOf(it) }
                ?: manifest.items.sortedBy { it.orderIndex }
        } else {
            activeItems
        }
    }

    /**
     * Check if an item is active based on its time window.
     */
    private fun isItemActive(item: PlaylistItem, now: Long, calendar: Calendar): Boolean {
        val window = item.timeWindow ?: return true

        // Check date range
        window.validFrom?.let { if (now < it) return false }
        window.validTo?.let { if (now > it) return false }

        // Check day of week
        window.daysOfWeek?.let { days ->
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            // Calendar: Sunday=1, Saturday=7
            // Convert to Monday=1, Sunday=7
            val normalizedDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
            if (normalizedDay !in days) return false
        }

        // Check time of day
        if (window.timeStartMinutes != null && window.timeEndMinutes != null) {
            val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                    calendar.get(Calendar.MINUTE)

            if (currentMinutes < window.timeStartMinutes || currentMinutes > window.timeEndMinutes) {
                return false
            }
        }

        return true
    }

    /**
     * Get next item in sequence.
     */
    fun getNextItem(
        activeItems: List<PlaylistItem>,
        currentItem: PlaylistItem?
    ): PlaylistItem? {
        if (activeItems.isEmpty()) return null
        if (currentItem == null) return activeItems.first()

        val currentIndex = activeItems.indexOfFirst { it.assetId == currentItem.assetId }
        val nextIndex = (currentIndex + 1) % activeItems.size

        return activeItems[nextIndex]
    }

    /**
     * Calculate total duration of active items.
     */
    fun getTotalDuration(activeItems: List<PlaylistItem>): Long =
        activeItems.sumOf { it.durationMs }
}
