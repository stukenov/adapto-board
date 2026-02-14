package com.playoutedge.server.services

import java.util.UUID

enum class ScheduleTemplate(
    val displayName: String,
    val description: String,
    val defaultDurationMs: Int,
    val icon: String
) {
    CORPORATE_LOBBY("Corporate Lobby", "8-hour mix of video and images, 30s per image", 30000, "🏢"),
    CAFETERIA("Cafeteria Menu", "Rotating menu images every 15 seconds", 15000, "🍽"),
    MEETING_ROOM("Meeting Room", "Agenda display with clock and weather widgets", 60000, "📊"),
    RETAIL_DISPLAY("Retail Display", "Product showcase with promotional videos", 20000, "🛍")
}

data class TemplateItem(
    val assetId: UUID,
    val orderIndex: Int,
    val durationMs: Int,
    val timeStart: String? = null,
    val timeEnd: String? = null,
    val daysOfWeek: Int = 127
)

class ScheduleTemplateService {
    fun getTemplates(): List<ScheduleTemplate> = ScheduleTemplate.entries.toList()

    fun generateItems(template: ScheduleTemplate, assetIds: List<UUID>): List<TemplateItem> {
        if (assetIds.isEmpty()) return emptyList()

        return when (template) {
            ScheduleTemplate.CORPORATE_LOBBY -> {
                assetIds.mapIndexed { index, assetId ->
                    TemplateItem(
                        assetId = assetId,
                        orderIndex = index,
                        durationMs = template.defaultDurationMs,
                        timeStart = "08:00",
                        timeEnd = "20:00"
                    )
                }
            }
            ScheduleTemplate.CAFETERIA -> {
                assetIds.mapIndexed { index, assetId ->
                    TemplateItem(
                        assetId = assetId,
                        orderIndex = index,
                        durationMs = template.defaultDurationMs,
                        timeStart = "07:00",
                        timeEnd = "19:00"
                    )
                }
            }
            ScheduleTemplate.MEETING_ROOM -> {
                assetIds.mapIndexed { index, assetId ->
                    TemplateItem(
                        assetId = assetId,
                        orderIndex = index,
                        durationMs = template.defaultDurationMs,
                        daysOfWeek = 31 // Mon-Fri
                    )
                }
            }
            ScheduleTemplate.RETAIL_DISPLAY -> {
                assetIds.mapIndexed { index, assetId ->
                    TemplateItem(
                        assetId = assetId,
                        orderIndex = index,
                        durationMs = template.defaultDurationMs,
                        timeStart = "09:00",
                        timeEnd = "21:00"
                    )
                }
            }
        }
    }
}
