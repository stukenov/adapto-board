package com.playoutedge.server.routes

import com.playoutedge.storage.LocalStorageService
import com.playoutedge.storage.StorageConfig
import com.playoutedge.storage.StorageMode
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.net.URLDecoder
import kotlin.io.path.exists
import kotlin.io.path.inputStream

fun Route.storageRoutes(
    storageConfig: StorageConfig,
    localStorageService: LocalStorageService?
) {
    // Only register local storage routes in LOCAL mode
    if (storageConfig.mode != StorageMode.LOCAL || localStorageService == null) {
        return
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

            // Determine content type from extension
            val contentType = when {
                key.endsWith(".mp4") -> ContentType.Video.MP4
                key.endsWith(".png") -> ContentType.Image.PNG
                key.endsWith(".jpg") || key.endsWith(".jpeg") -> ContentType.Image.JPEG
                else -> ContentType.Application.OctetStream
            }

            call.respondOutputStream(contentType) {
                filePath.inputStream().copyTo(this)
            }
        }
    }
}
