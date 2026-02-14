package com.playoutedge.server.routes

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.storage.LocalStorageService
import com.playoutedge.storage.StorageConfig
import com.playoutedge.storage.StorageMode
import java.util.UUID
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.net.URLDecoder
import kotlin.io.path.exists
import kotlin.io.path.inputStream

private fun resolveContentType(key: String): ContentType = when {
    key.endsWith(".mp4") -> ContentType.Video.MP4
    key.endsWith(".png") -> ContentType.Image.PNG
    key.endsWith(".jpg") || key.endsWith(".jpeg") -> ContentType.Image.JPEG
    key.endsWith(".wav") -> ContentType.Audio.Any
    key.endsWith(".mp3") -> ContentType("audio", "mpeg")
    key.endsWith(".ogg") -> ContentType("audio", "ogg")
    else -> ContentType.Application.OctetStream
}

fun Route.storageRoutes(
    storageConfig: StorageConfig,
    localStorageService: LocalStorageService?,
    assetRepository: AssetRepository? = null
) {
    // Only register local storage routes in LOCAL mode
    if (storageConfig.mode != StorageMode.LOCAL || localStorageService == null) {
        return
    }

    // Admin-session-authenticated storage serving for UI previews
    // URL: /storage/{assetId} — resolves storageKey from DB
    route("/storage") {
        get("/{assetId}") {
            val session = call.adminSession
            if (session == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val assetId = call.parameters["assetId"]?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
            }
            if (assetId == null || assetRepository == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val asset = assetRepository.findById(tenantId, assetId)
            if (asset == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            val filePath = localStorageService.getFilePath(asset.storageKey)
            if (!filePath.exists()) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.response.header(HttpHeaders.CacheControl, "private, max-age=3600")
            call.respondOutputStream(resolveContentType(asset.storageKey)) {
                filePath.inputStream().copyTo(this)
            }
        }
    }

    route("/api/storage") {
        get("/{key...}") {
            val encodedKey = call.parameters.getAll("key")?.joinToString("/") ?: ""
            val key = URLDecoder.decode(encodedKey, "UTF-8")
            val token = call.request.queryParameters["token"]
            val expiresStr = call.request.queryParameters["expires"]

            if (token.isNullOrBlank() || expiresStr.isNullOrBlank()) {
                call.respond(HttpStatusCode.Forbidden, "Missing token or expires")
                return@get
            }

            val expires = expiresStr.toLongOrNull()
            if (expires == null) {
                call.respond(HttpStatusCode.Forbidden, "Invalid expires")
                return@get
            }

            if (!localStorageService.verifyToken(key, token, expires)) {
                call.respond(HttpStatusCode.Forbidden, "Invalid or expired token")
                return@get
            }

            val filePath = localStorageService.getFilePath(key)
            if (!filePath.exists()) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respondOutputStream(resolveContentType(key)) {
                filePath.inputStream().copyTo(this)
            }
        }
    }
}
