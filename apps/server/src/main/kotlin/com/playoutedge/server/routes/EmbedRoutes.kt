package com.playoutedge.server.routes

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.ChannelRepository
import com.playoutedge.persistence.repositories.OverlayRepository
import com.playoutedge.server.services.PlaylistService
import com.playoutedge.server.views.embedPlayerView
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.playoutedge.persistence.tables.EmbedViews
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

@Serializable
private data class ManifestItem(
    val url: String,
    val type: String,
    val durationMs: Int?,
    val voiceoverUrl: String? = null
)

@Serializable
private data class EmbedManifest(
    val channelId: String,
    val items: List<ManifestItem>
)

fun Route.embedRoutes(
    channelRepository: ChannelRepository,
    playlistService: PlaylistService,
    overlayRepository: OverlayRepository? = null
) {
    route("/embed") {
        get("/{channelId}") {
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }
            if (channelId == null) {
                call.respondText("Channel not found", status = HttpStatusCode.NotFound)
                return@get
            }

            // Find channel - must have embed enabled
            val channelData = newSuspendedTransaction {
                val ch = channelRepository.findByIdAnyTenant(channelId) ?: return@newSuspendedTransaction null
                if (!ch.embedEnabled) return@newSuspendedTransaction null
                ch.name
            }
            if (channelData == null) {
                call.respondText("Channel not found or embed not enabled", status = HttpStatusCode.NotFound)
                return@get
            }

            // Record embed view
            val ipAddress = call.request.local.remoteAddress
            val userAgent = call.request.headers["User-Agent"]
            val referer = call.request.headers["Referer"]
            newSuspendedTransaction {
                EmbedViews.insertAndGetId {
                    it[EmbedViews.channelId] = channelId
                    it[EmbedViews.ipAddress] = ipAddress
                    it[EmbedViews.userAgent] = userAgent
                    it[EmbedViews.referer] = referer
                    it[EmbedViews.viewedAt] = Clock.System.now()
                }
            }

            // Read embed customization parameters
            val bgColor = call.request.queryParameters["bg"]
            val muted = call.request.queryParameters["muted"]?.toBooleanStrictOrNull() ?: true
            val controls = call.request.queryParameters["controls"]?.toBooleanStrictOrNull() ?: false
            val kenburns = call.request.queryParameters["kenburns"]?.toBooleanStrictOrNull() ?: true
            val shuffle = call.request.queryParameters["shuffle"]?.toBooleanStrictOrNull() ?: false

            call.respondHtml(HttpStatusCode.OK) {
                embedPlayerView(
                    channelId = channelId.toString(),
                    channelName = channelData,
                    bgColor = bgColor,
                    muted = muted,
                    controls = controls,
                    kenburns = kenburns,
                    shuffle = shuffle
                )
            }
        }

        get("/{channelId}/manifest.json") {
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }
            if (channelId == null) {
                call.respondText("{}", contentType = ContentType.Application.Json, status = HttpStatusCode.NotFound)
                return@get
            }

            val channelData = newSuspendedTransaction {
                val ch = channelRepository.findByIdAnyTenant(channelId) ?: return@newSuspendedTransaction null
                if (!ch.embedEnabled) return@newSuspendedTransaction null
                Pair(ch.name, ch.tenant.id.value)
            }
            if (channelData == null) {
                call.respondText("{}", contentType = ContentType.Application.Json, status = HttpStatusCode.NotFound)
                return@get
            }

            val tenantId = TenantId(channelData.second)
            val manifest = playlistService.getManifest(tenantId, channelId)

            val items = manifest?.items?.map { item ->
                ManifestItem(
                    url = item.url,
                    type = if (item.durationMs != null && item.durationMs > 0) "VIDEO" else "IMAGE",
                    durationMs = item.durationMs ?: 10000,
                    voiceoverUrl = item.voiceoverUrl
                )
            } ?: emptyList()

            val response = EmbedManifest(
                channelId = channelId.toString(),
                items = items
            )

            call.respondText(
                Json.encodeToString(response),
                contentType = ContentType.Application.Json
            )
        }

        // Public overlay state for embed player
        get("/{channelId}/overlay.json") {
            val channelId = call.parameters["channelId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }
            if (channelId == null || overlayRepository == null) {
                call.respondText("{}", contentType = ContentType.Application.Json, status = HttpStatusCode.NotFound)
                return@get
            }

            // Verify channel exists and has embed enabled
            val channelData = newSuspendedTransaction {
                val ch = channelRepository.findByIdAnyTenant(channelId) ?: return@newSuspendedTransaction null
                if (!ch.embedEnabled) return@newSuspendedTransaction null
                Pair(ch.tenant.id.value, ch.name)
            }
            if (channelData == null) {
                call.respondText("{}", contentType = ContentType.Application.Json, status = HttpStatusCode.NotFound)
                return@get
            }

            val tenantId = TenantId(channelData.first)
            val state = newSuspendedTransaction {
                overlayRepository.getState(tenantId, channelId)?.let {
                    it.stateJson.toString()
                }
            } ?: "{}"

            call.response.headers.append("Cache-Control", "no-cache")
            call.respondText(state, contentType = ContentType.Application.Json)
        }
    }
}
