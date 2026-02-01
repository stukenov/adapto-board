package com.playoutedge.server.routes.player

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.server.plugins.AUTH_DEVICE
import com.playoutedge.server.plugins.deviceClaims
import com.playoutedge.server.services.OverlayEvent
import com.playoutedge.server.services.OverlayService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration.Companion.seconds

fun Route.overlayStreamRoutes(overlayService: OverlayService) {
    authenticate(AUTH_DEVICE) {
        route("/api/player/overlay") {
            // GET /api/player/overlay/stream - SSE stream
            get("/stream") {
                val deviceClaims = call.deviceClaims ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing device claims"
                )

                val channelId = deviceClaims.channelId ?: throw ApiException.badRequest(
                    ErrorCode.DEVICE_NO_CHANNEL,
                    "Device has no assigned channel"
                )

                val tenantId = TenantId(deviceClaims.tenantId)

                call.response.cacheControl(CacheControl.NoCache(null))

                call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                    // Send initial state
                    val initialState = overlayService.getState(tenantId, channelId)
                    if (initialState != null) {
                        write("event: state\n")
                        write("data: ${Json.encodeToString(JsonObject.serializer(), initialState.stateJson)}\n")
                        write("id: ${initialState.version}\n")
                        write("\n")
                        flush()
                    }

                    // Start keepalive job
                    val keepaliveJob = launch {
                        while (true) {
                            delay(15.seconds)
                            try {
                                write("event: keepalive\n")
                                write("data: {}\n")
                                write("\n")
                                flush()
                            } catch (e: Exception) {
                                break
                            }
                        }
                    }

                    try {
                        // Subscribe to overlay events
                        overlayService.subscribe(channelId)
                            .onEach { event ->
                                when (event) {
                                    is OverlayEvent.State -> {
                                        write("event: state\n")
                                        write("data: ${Json.encodeToString(JsonObject.serializer(), event.state)}\n")
                                        write("id: ${event.version}\n")
                                        write("\n")
                                        flush()
                                    }
                                    is OverlayEvent.Patch -> {
                                        write("event: patch\n")
                                        write("data: ${Json.encodeToString(JsonObject.serializer(), event.patch)}\n")
                                        write("id: ${event.version}\n")
                                        write("\n")
                                        flush()
                                    }
                                    is OverlayEvent.Keepalive -> {
                                        // Keepalive is handled by separate job
                                    }
                                }
                            }
                            .collect()
                    } finally {
                        keepaliveJob.cancel()
                    }
                }
            }
        }
    }
}
