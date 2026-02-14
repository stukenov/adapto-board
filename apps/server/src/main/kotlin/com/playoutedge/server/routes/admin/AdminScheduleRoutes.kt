package com.playoutedge.server.routes.admin

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.persistence.repositories.ChannelRepository
import com.playoutedge.persistence.repositories.ScheduleRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.services.PublishResult
import com.playoutedge.server.services.ScheduleItemInput
import com.playoutedge.server.services.ScheduleService
import com.playoutedge.server.services.TtsService
import com.playoutedge.tts.TtsVoice
import com.playoutedge.server.views.channels.*
import com.playoutedge.server.views.schedule.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

@Serializable
private data class ScheduleItemJson(
    val assetId: String,
    val orderIndex: Int,
    val timeStart: String? = null,
    val timeEnd: String? = null,
    val daysOfWeek: Int? = null
)

private val json = Json { ignoreUnknownKeys = true }

/**
 * Admin schedule management routes with drag & drop editor.
 */
fun Route.adminScheduleRoutes(
    channelRepository: ChannelRepository,
    scheduleRepository: ScheduleRepository,
    assetRepository: AssetRepository,
    scheduleService: ScheduleService,
    ttsService: TtsService? = null
) {
    route("/admin/channels/{channelId}/schedule") {
        // GET /admin/channels/:channelId/schedule - Schedule editor
        get {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val channel = channelRepository.findById(tenantId, channelId)
            if (channel == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            // Get draft or active version items — wrap in transaction to access lazy entity fields
            val (draftVersionId, activeVersionNumber, editorItems, libraryAssets) = newSuspendedTransaction {
                val draftVersion = scheduleRepository.findDraftVersion(tenantId, channelId)
                val activeVersion = scheduleRepository.findActiveVersion(tenantId, channelId)
                val sourceVersion = draftVersion ?: activeVersion
                val items = sourceVersion?.let { version ->
                    scheduleRepository.findItemsByVersion(tenantId, version.id.value)
                } ?: emptyList()

                val assets = assetRepository.findAllActive(tenantId)

                val editorItems = items.mapIndexed { index, item ->
                    ScheduleEditorItem(
                        assetId = item.asset.id.value,
                        assetName = item.asset.name,
                        assetType = item.asset.type,
                        durationMs = item.asset.durationMs,
                        orderIndex = index,
                        timeStart = item.timeStart?.toString(),
                        timeEnd = item.timeEnd?.toString(),
                        daysOfWeek = item.daysOfWeek
                    )
                }

                val libraryAssets = assets.map { asset ->
                    LibraryAsset(
                        id = asset.id.value,
                        name = asset.name,
                        type = asset.type,
                        durationMs = asset.durationMs,
                        fileSizeFormatted = formatFileSize(asset.fileSizeBytes ?: 0)
                    )
                }

                ScheduleEditorData(
                    draftVersionId = draftVersion?.id?.value,
                    activeVersionNumber = activeVersion?.version,
                    editorItems = editorItems,
                    libraryAssets = libraryAssets
                )
            }

            val successParam = call.request.queryParameters["success"]
            val errorParam = call.request.queryParameters["error"]

            // Get other channels for copy-from dropdown
            val otherChannels = channelRepository.findAll(tenantId)
                .filter { it.id.value != channelId }
                .map { CopySourceChannel(id = it.id.value, name = it.name) }

            call.respondHtml {
                scheduleEditorView(
                    session = session,
                    channelId = channelId,
                    channelName = channel.name,
                    items = editorItems,
                    libraryAssets = libraryAssets,
                    draftVersionId = draftVersionId,
                    activeVersionNumber = activeVersionNumber,
                    error = errorParam,
                    success = successParam,
                    otherChannels = otherChannels
                )
            }
        }

        // POST /admin/channels/:channelId/schedule/save - Save draft
        post("/save") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@post
            }

            val params = call.receiveParameters()
            val itemsJsonStr = params["itemsJson"] ?: "[]"

            val parsedItems = try {
                json.decodeFromString<List<ScheduleItemJson>>(itemsJsonStr)
            } catch (e: Exception) {
                call.respondRedirect("/admin/channels/$channelId/schedule?error=Invalid+data")
                return@post
            }

            // Get or create draft
            var draftVersion = scheduleRepository.findDraftVersion(tenantId, channelId)
            if (draftVersion == null) {
                draftVersion = scheduleService.createDraft(tenantId, channelId, session.subject)
            }

            // Replace items
            val inputs = parsedItems.map { item ->
                ScheduleItemInput(
                    assetId = UUID.fromString(item.assetId),
                    orderIndex = item.orderIndex,
                    timeStart = item.timeStart?.takeIf { it.isNotBlank() }?.let { LocalTime.parse(it) },
                    timeEnd = item.timeEnd?.takeIf { it.isNotBlank() }?.let { LocalTime.parse(it) },
                    daysOfWeek = item.daysOfWeek
                )
            }

            scheduleService.replaceItems(tenantId, draftVersion.id.value, inputs)

            call.respondRedirect("/admin/channels/$channelId/schedule?success=Draft+saved")
        }

        // POST /admin/channels/:channelId/schedule/publish - Publish draft
        post("/publish") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@post
            }

            val params = call.receiveParameters()
            val itemsJsonStr = params["itemsJson"] ?: "[]"

            val parsedItems = try {
                json.decodeFromString<List<ScheduleItemJson>>(itemsJsonStr)
            } catch (e: Exception) {
                call.respondRedirect("/admin/channels/$channelId/schedule?error=Invalid+data")
                return@post
            }

            // Get or create draft
            var draftVersion = scheduleRepository.findDraftVersion(tenantId, channelId)
            if (draftVersion == null) {
                draftVersion = scheduleService.createDraft(tenantId, channelId, session.subject)
            }

            // Replace items first
            val inputs = parsedItems.map { item ->
                ScheduleItemInput(
                    assetId = UUID.fromString(item.assetId),
                    orderIndex = item.orderIndex,
                    timeStart = item.timeStart?.takeIf { it.isNotBlank() }?.let { LocalTime.parse(it) },
                    timeEnd = item.timeEnd?.takeIf { it.isNotBlank() }?.let { LocalTime.parse(it) },
                    daysOfWeek = item.daysOfWeek
                )
            }

            scheduleService.replaceItems(tenantId, draftVersion.id.value, inputs)

            // Publish
            val result = scheduleService.publish(tenantId, draftVersion.id.value)

            val message = when (result) {
                is PublishResult.Success -> "Schedule+published+successfully"
                is PublishResult.AssetsNotReady -> "Some+assets+are+not+ready"
                is PublishResult.ScheduleEmpty -> "Cannot+publish+empty+schedule"
                is PublishResult.VersionNotFound -> "Version+not+found"
                is PublishResult.VersionNotDraft -> "Version+is+not+a+draft"
            }

            val param = if (result is PublishResult.Success) "success" else "error"
            call.respondRedirect("/admin/channels/$channelId/schedule?$param=$message")
        }

        // GET /admin/channels/:channelId/schedule/preview - Preview published schedule
        get("/preview") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val channel = channelRepository.findById(tenantId, channelId)
            if (channel == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val previewItems = newSuspendedTransaction {
                val activeVersion = scheduleRepository.findActiveVersion(tenantId, channelId)
                val sourceVersion = activeVersion ?: scheduleRepository.findDraftVersion(tenantId, channelId)
                val items = sourceVersion?.let { version ->
                    scheduleRepository.findItemsByVersion(tenantId, version.id.value)
                } ?: emptyList()

                items.map { item ->
                    SchedulePreviewItem(
                        assetName = item.asset.name,
                        startTime = item.timeStart?.toString(),
                        endTime = item.timeEnd?.toString(),
                        daysOfWeek = item.daysOfWeek?.let { decodeDaysOfWeek(it) },
                        duration = item.asset.durationMs
                    )
                }
            }

            call.respondHtml {
                schedulePreviewView(
                    session = session,
                    channelId = channelId,
                    channelName = channel.name,
                    items = previewItems
                )
            }
        }

        // GET /admin/channels/:channelId/schedule/history - Schedule version history
        get("/history") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val channel = channelRepository.findById(tenantId, channelId)
            if (channel == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val versions = newSuspendedTransaction {
                val allVersions = scheduleRepository.findVersionsByChannel(tenantId, channelId)
                allVersions.map { v ->
                    val itemCount = scheduleRepository.findItemsByVersion(tenantId, v.id.value).size
                    ScheduleVersionItem(
                        id = v.id.value,
                        state = v.state.name,
                        itemCount = itemCount,
                        createdAt = v.createdAt
                    )
                }
            }

            call.respondHtml {
                scheduleHistoryView(
                    session = session,
                    channelId = channelId,
                    channelName = channel.name,
                    versions = versions
                )
            }
        }

        // GET /admin/channels/:channelId/schedule/grid - Grid view
        get("/grid") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val channel = channelRepository.findById(tenantId, channelId)
            if (channel == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val (activeVersionNumber, editorItems) = newSuspendedTransaction {
                val activeVersion = scheduleRepository.findActiveVersion(tenantId, channelId)
                val sourceVersion = activeVersion ?: scheduleRepository.findDraftVersion(tenantId, channelId)
                val items = sourceVersion?.let { version ->
                    scheduleRepository.findItemsByVersion(tenantId, version.id.value)
                } ?: emptyList()

                val editorItems = items.mapIndexed { index, item ->
                    ScheduleEditorItem(
                        assetId = item.asset.id.value,
                        assetName = item.asset.name,
                        assetType = item.asset.type,
                        durationMs = item.asset.durationMs,
                        orderIndex = index,
                        timeStart = item.timeStart?.toString(),
                        timeEnd = item.timeEnd?.toString(),
                        daysOfWeek = item.daysOfWeek
                    )
                }

                Pair(activeVersion?.version, editorItems)
            }

            call.respondHtml {
                scheduleGridView(
                    session = session,
                    channelId = channelId,
                    channelName = channel.name,
                    items = editorItems,
                    activeVersionNumber = activeVersionNumber
                )
            }
        }

        // POST /admin/channels/:channelId/schedule/tts-preview - Generate TTS preview
        post("/tts-preview") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            if (ttsService == null) {
                call.respondText("TTS not available", status = io.ktor.http.HttpStatusCode.ServiceUnavailable)
                return@post
            }

            val params = call.receiveParameters()
            val text = params["text"] ?: ""
            val voiceName = params["voice"] ?: "RU_DARIYA"

            if (text.isBlank()) {
                call.respondText("Text is required", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@post
            }

            val voice = runCatching { TtsVoice.valueOf(voiceName) }.getOrDefault(TtsVoice.RU_DARIYA)
            val audioBytes = ttsService.previewVoiceover(text, voice)

            call.respondBytes(audioBytes, io.ktor.http.ContentType.Audio.MPEG)
        }

        // POST /admin/channels/:channelId/schedule/copy-from - Copy schedule from another channel
        post("/copy-from") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@post
            }

            val params = call.receiveParameters()
            val sourceChannelId = params["sourceChannelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (sourceChannelId == null) {
                call.respondRedirect("/admin/channels/$channelId/schedule?error=Invalid+source+channel")
                return@post
            }

            val draftId = scheduleService.copySchedule(tenantId, sourceChannelId, channelId, session.subject)

            if (draftId != null) {
                call.respondRedirect("/admin/channels/$channelId/schedule?success=Schedule+copied+as+draft")
            } else {
                call.respondRedirect("/admin/channels/$channelId/schedule?error=Source+channel+has+no+active+schedule")
            }
        }

        // POST /admin/channels/:channelId/schedule/validate - Validate schedule for overlaps/gaps
        post("/validate") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (channelId == null) {
                call.respondText("Channel not found", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@post
            }

            val version = newSuspendedTransaction {
                scheduleRepository.findDraftVersion(tenantId, channelId)
                    ?: scheduleRepository.findActiveVersion(tenantId, channelId)
            }

            if (version == null) {
                call.respondText("<div class=\"alert alert-info\">No schedule to validate.</div>",
                    io.ktor.http.ContentType.Text.Html)
                return@post
            }

            val warnings = scheduleService.validateSchedule(tenantId, version.id.value)

            val html = buildString {
                if (warnings.isEmpty()) {
                    append("<div class=\"alert alert-success\">Schedule is valid. No overlaps or gaps detected.</div>")
                } else {
                    append("<div class=\"alert alert-warning\">")
                    append("<strong>Schedule Warnings (${warnings.size}):</strong><ul>")
                    warnings.forEach { w -> append("<li>$w</li>") }
                    append("</ul></div>")
                }
            }

            call.respondText(html, io.ktor.http.ContentType.Text.Html)
        }

        // GET /admin/channels/:channelId/schedule/diff - Compare two versions
        get("/diff") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (channelId == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val channel = channelRepository.findById(tenantId, channelId)
            if (channel == null) {
                call.respondRedirect("/admin/channels")
                return@get
            }

            val fromId = call.request.queryParameters["from"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }
            val toId = call.request.queryParameters["to"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }

            if (fromId == null || toId == null) {
                call.respondRedirect("/admin/channels/$channelId/schedule/history")
                return@get
            }

            val (fromItems, toItems) = newSuspendedTransaction {
                val from = scheduleRepository.findItemsByVersion(tenantId, fromId).map { item ->
                    ScheduleDiffItem(
                        assetId = item.asset.id.value,
                        assetName = item.asset.name,
                        orderIndex = item.orderIndex,
                        timeStart = item.timeStart?.toString(),
                        timeEnd = item.timeEnd?.toString(),
                        daysOfWeek = item.daysOfWeek
                    )
                }
                val to = scheduleRepository.findItemsByVersion(tenantId, toId).map { item ->
                    ScheduleDiffItem(
                        assetId = item.asset.id.value,
                        assetName = item.asset.name,
                        orderIndex = item.orderIndex,
                        timeStart = item.timeStart?.toString(),
                        timeEnd = item.timeEnd?.toString(),
                        daysOfWeek = item.daysOfWeek
                    )
                }
                Pair(from, to)
            }

            call.respondHtml {
                scheduleDiffView(
                    session = session,
                    channelId = channelId,
                    channelName = channel.name,
                    fromItems = fromItems,
                    toItems = toItems
                )
            }
        }

        // POST /admin/channels/:channelId/schedule/remove/:itemIndex — fallback without JS
        post("/remove/{itemIndex}") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val channelId = call.parameters["channelId"]
            // Fallback: just redirect back, JS handles removal client-side
            call.respondRedirect("/admin/channels/$channelId/schedule")
        }
    }
}

private data class ScheduleEditorData(
    val draftVersionId: java.util.UUID?,
    val activeVersionNumber: Int?,
    val editorItems: List<ScheduleEditorItem>,
    val libraryAssets: List<LibraryAsset>
)

private fun decodeDaysOfWeek(bitmask: Int): String {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val active = days.filterIndexed { idx, _ -> (bitmask shr idx) and 1 == 1 }
    return if (active.size == 7) "All" else active.joinToString(", ")
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1_024 -> String.format("%.1f KB", bytes / 1_024.0)
        else -> "$bytes B"
    }
}
