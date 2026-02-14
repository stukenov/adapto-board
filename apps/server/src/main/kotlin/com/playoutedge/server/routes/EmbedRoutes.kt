package com.playoutedge.server.routes

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.ChannelRepository
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
    playlistService: PlaylistService
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

            // Find channel across all tenants (public endpoint)
            val channel = channelRepository.findByIdAnyTenant(channelId)
            if (channel == null) {
                call.respondText("Channel not found", status = HttpStatusCode.NotFound)
                return@get
            }

            call.respondHtml(HttpStatusCode.OK) {
                embedPlayerView(channelId.toString(), channel.name)
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
    }
}
