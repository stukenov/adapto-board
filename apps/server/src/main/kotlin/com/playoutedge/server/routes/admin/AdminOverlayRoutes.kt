package com.playoutedge.server.routes.admin

import com.playoutedge.domain.enums.BindingStatus
import com.playoutedge.domain.enums.OverlaySourceType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.ChannelRepository
import com.playoutedge.persistence.repositories.OverlayRepository
import com.playoutedge.persistence.repositories.WebhookLogRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.overlay.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.UUID

private const val WEBHOOK_BASE_URL = "https://api.playoutedge.com/webhooks/overlay"

/**
 * Admin overlay management routes.
 */
fun Route.adminOverlayRoutes(
    overlayRepository: OverlayRepository,
    channelRepository: ChannelRepository,
    webhookLogRepository: WebhookLogRepository
) {
    route("/admin/overlay") {
        // Main overlay page - redirect to profiles
        get {
            call.respondRedirect("/admin/overlay/profiles")
        }

        // === Profile Routes ===

        route("/profiles") {
            // GET /admin/overlay/profiles - List profiles
            get {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                val tenantId = TenantId(session.tenantId)
                val profiles = overlayRepository.findAllProfiles(tenantId)

                val profileItems = profiles.map { profile ->
                    val bindingsCount = overlayRepository.countBindingsByProfile(tenantId, profile.id.value)
                    val widgetTypes = extractWidgetTypes(profile.definitionJson.toString())
                    OverlayProfileItem(
                        id = profile.id.value,
                        name = profile.name,
                        widgetTypes = widgetTypes,
                        bindingsCount = bindingsCount.toInt(),
                        createdAt = profile.createdAt
                    )
                }

                call.respondHtml {
                    overlayProfilesListView(session, profileItems)
                }
            }

            // GET /admin/overlay/profiles/new - New profile form
            get("/new") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                call.respondHtml {
                    newOverlayProfileView(session)
                }
            }

            // POST /admin/overlay/profiles - Create profile
            post {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val tenantId = TenantId(session.tenantId)
                val params = call.receiveParameters()
                val name = params["name"] ?: ""
                val template = params["template"]?.let { runCatching { OverlayTemplate.valueOf(it) }.getOrNull() }
                val customJson = params["definitionJson"]?.takeIf { it.isNotBlank() }

                if (name.isBlank()) {
                    call.respondHtml {
                        newOverlayProfileView(session, error = "Name is required")
                    }
                    return@post
                }

                val definitionJson = customJson ?: getTemplateDefinition(template ?: OverlayTemplate.TICKER)

                try {
                    overlayRepository.createProfile(tenantId, name, definitionJson)
                    call.respondRedirect("/admin/overlay/profiles")
                } catch (e: Exception) {
                    call.respondHtml {
                        newOverlayProfileView(session, error = "Failed to create profile: ${e.message}")
                    }
                }
            }

            // GET /admin/overlay/profiles/:id - Profile detail
            get("/{id}") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                val tenantId = TenantId(session.tenantId)
                val profileId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                if (profileId == null) {
                    call.respondRedirect("/admin/overlay/profiles")
                    return@get
                }

                val profile = overlayRepository.findProfileById(tenantId, profileId)
                if (profile == null) {
                    call.respondRedirect("/admin/overlay/profiles")
                    return@get
                }

                val bindings = overlayRepository.findBindingsByProfile(tenantId, profileId)
                val bindingItems = bindings.map { binding ->
                    OverlayBindingItem(
                        id = binding.id.value,
                        profileId = profile.id.value,
                        profileName = profile.name,
                        channelId = binding.channel.id.value,
                        channelName = binding.channel.name,
                        sourceType = binding.sourceType,
                        status = binding.status,
                        createdAt = binding.createdAt
                    )
                }

                val profileDetail = OverlayProfileDetail(
                    id = profile.id.value,
                    name = profile.name,
                    definitionJson = formatJson(profile.definitionJson.toString()),
                    createdAt = profile.createdAt
                )

                call.respondHtml {
                    overlayProfileDetailView(session, profileDetail, bindingItems)
                }
            }

            // GET /admin/overlay/profiles/:id/edit - Edit profile form
            get("/{id}/edit") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                val tenantId = TenantId(session.tenantId)
                val profileId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                if (profileId == null) {
                    call.respondRedirect("/admin/overlay/profiles")
                    return@get
                }

                val profile = overlayRepository.findProfileById(tenantId, profileId)
                if (profile == null) {
                    call.respondRedirect("/admin/overlay/profiles")
                    return@get
                }

                val profileDetail = OverlayProfileDetail(
                    id = profile.id.value,
                    name = profile.name,
                    definitionJson = formatJson(profile.definitionJson.toString()),
                    createdAt = profile.createdAt
                )

                call.respondHtml {
                    editOverlayProfileView(session, profileDetail)
                }
            }

            // POST /admin/overlay/profiles/:id - Update profile
            post("/{id}") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val tenantId = TenantId(session.tenantId)
                val profileId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                if (profileId == null) {
                    call.respondRedirect("/admin/overlay/profiles")
                    return@post
                }

                val params = call.receiveParameters()
                val name = params["name"] ?: ""
                val definitionJson = params["definitionJson"] ?: "{}"

                if (name.isBlank()) {
                    val profile = overlayRepository.findProfileById(tenantId, profileId)
                    if (profile != null) {
                        val profileDetail = OverlayProfileDetail(
                            id = profile.id.value,
                            name = profile.name,
                            definitionJson = definitionJson,
                            createdAt = profile.createdAt
                        )
                        call.respondHtml {
                            editOverlayProfileView(session, profileDetail, error = "Name is required")
                        }
                    } else {
                        call.respondRedirect("/admin/overlay/profiles")
                    }
                    return@post
                }

                overlayRepository.updateProfile(tenantId, profileId, name, definitionJson)
                call.respondRedirect("/admin/overlay/profiles/$profileId")
            }

            // POST /admin/overlay/profiles/:id/delete - Delete profile
            post("/{id}/delete") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val tenantId = TenantId(session.tenantId)
                val profileId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                if (profileId != null) {
                    overlayRepository.deleteProfile(tenantId, profileId)
                }

                call.respondRedirect("/admin/overlay/profiles")
            }
        }

        // === Binding Routes ===

        route("/bindings") {
            // GET /admin/overlay/bindings - List bindings
            get {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                val tenantId = TenantId(session.tenantId)
                val bindings = overlayRepository.findAllBindings(tenantId)
                val profiles = overlayRepository.findAllProfiles(tenantId)
                val channels = channelRepository.findAll(tenantId)

                val bindingItems = bindings.map { binding ->
                    OverlayBindingItem(
                        id = binding.id.value,
                        profileId = binding.overlayProfile.id.value,
                        profileName = binding.overlayProfile.name,
                        channelId = binding.channel.id.value,
                        channelName = binding.channel.name,
                        sourceType = binding.sourceType,
                        status = binding.status,
                        createdAt = binding.createdAt
                    )
                }

                val profileItems = profiles.map { profile ->
                    OverlayProfileItem(
                        id = profile.id.value,
                        name = profile.name,
                        widgetTypes = emptyList(),
                        bindingsCount = 0,
                        createdAt = profile.createdAt
                    )
                }

                val channelOptions = channels.map { channel ->
                    ChannelOption(id = channel.id.value, name = channel.name)
                }

                call.respondHtml {
                    overlayBindingsListView(session, bindingItems, profileItems, channelOptions)
                }
            }

            // GET /admin/overlay/bindings/new - New binding form
            get("/new") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                val tenantId = TenantId(session.tenantId)
                val profiles = overlayRepository.findAllProfiles(tenantId)
                val channels = channelRepository.findAll(tenantId)

                val profileItems = profiles.map { profile ->
                    OverlayProfileItem(
                        id = profile.id.value,
                        name = profile.name,
                        widgetTypes = emptyList(),
                        bindingsCount = 0,
                        createdAt = profile.createdAt
                    )
                }

                val channelOptions = channels.map { channel ->
                    ChannelOption(id = channel.id.value, name = channel.name)
                }

                call.respondHtml {
                    newOverlayBindingView(session, profileItems, channelOptions)
                }
            }

            // POST /admin/overlay/bindings - Create binding
            post {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val tenantId = TenantId(session.tenantId)
                val params = call.receiveParameters()
                val channelId = params["channelId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                val profileId = params["profileId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                val sourceType = params["sourceType"]?.let { runCatching { OverlaySourceType.valueOf(it) }.getOrNull() }
                    ?: OverlaySourceType.MANUAL

                if (channelId == null || profileId == null) {
                    call.respondRedirect("/admin/overlay/bindings/new")
                    return@post
                }

                val webhookSecret = if (sourceType == OverlaySourceType.WEBHOOK) {
                    UUID.randomUUID().toString()
                } else null

                val binding = overlayRepository.createBinding(
                    tenantId = tenantId,
                    channelId = channelId,
                    profileId = profileId,
                    sourceType = sourceType,
                    sourceConfigJson = "{}",
                    webhookSecret = webhookSecret
                )

                call.respondRedirect("/admin/overlay/bindings/${binding.id.value}")
            }

            // GET /admin/overlay/bindings/:id - Binding detail
            get("/{id}") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                val tenantId = TenantId(session.tenantId)
                val bindingId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                if (bindingId == null) {
                    call.respondRedirect("/admin/overlay/bindings")
                    return@get
                }

                val binding = overlayRepository.findBindingById(tenantId, bindingId)
                if (binding == null) {
                    call.respondRedirect("/admin/overlay/bindings")
                    return@get
                }

                val webhookLogs = if (binding.sourceType == OverlaySourceType.WEBHOOK) {
                    webhookLogRepository.findByBinding(bindingId, 20).map { log ->
                        WebhookLogItem(
                            id = log.id.value,
                            statusCode = log.statusCode,
                            latencyMs = log.latencyMs,
                            payloadSize = log.payloadSize,
                            error = log.error,
                            createdAt = log.createdAt
                        )
                    }
                } else emptyList()

                val bindingDetail = OverlayBindingDetail(
                    id = binding.id.value,
                    profileId = binding.overlayProfile.id.value,
                    profileName = binding.overlayProfile.name,
                    channelId = binding.channel.id.value,
                    channelName = binding.channel.name,
                    sourceType = binding.sourceType,
                    sourceConfigJson = formatJson(binding.sourceConfigJson.toString()),
                    status = binding.status,
                    webhookSecret = binding.webhookSecret,
                    webhookUrl = binding.webhookSecret?.let { "$WEBHOOK_BASE_URL/${binding.id.value}" },
                    createdAt = binding.createdAt
                )

                call.respondHtml {
                    overlayBindingDetailView(session, bindingDetail, webhookLogs)
                }
            }

            // POST /admin/overlay/bindings/:id/pause - Pause binding
            post("/{id}/pause") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val tenantId = TenantId(session.tenantId)
                val bindingId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                if (bindingId != null) {
                    overlayRepository.updateBindingStatus(tenantId, bindingId, BindingStatus.PAUSED)
                }

                call.respondRedirect("/admin/overlay/bindings/$bindingId")
            }

            // POST /admin/overlay/bindings/:id/activate - Activate binding
            post("/{id}/activate") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val tenantId = TenantId(session.tenantId)
                val bindingId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                if (bindingId != null) {
                    overlayRepository.updateBindingStatus(tenantId, bindingId, BindingStatus.ACTIVE)
                }

                call.respondRedirect("/admin/overlay/bindings/$bindingId")
            }

            // POST /admin/overlay/bindings/:id/state - Update overlay state (manual editor)
            post("/{id}/state") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val tenantId = TenantId(session.tenantId)
                val bindingId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                if (bindingId == null) {
                    call.respondRedirect("/admin/overlay/bindings")
                    return@post
                }

                val binding = overlayRepository.findBindingById(tenantId, bindingId)
                if (binding == null) {
                    call.respondRedirect("/admin/overlay/bindings")
                    return@post
                }

                val params = call.receiveParameters()
                val stateJson = params["stateJson"] ?: "{}"

                overlayRepository.setState(tenantId, binding.channel.id.value, stateJson)
                call.respondRedirect("/admin/overlay/bindings/$bindingId")
            }

            // POST /admin/overlay/bindings/:id/rest-config - Update REST config
            post("/{id}/rest-config") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val tenantId = TenantId(session.tenantId)
                val bindingId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                if (bindingId == null) {
                    call.respondRedirect("/admin/overlay/bindings")
                    return@post
                }

                val params = call.receiveParameters()
                val configJson = buildString {
                    append("{")
                    append("\"url\":\"${params["url"] ?: ""}\",")
                    append("\"authType\":\"${params["authType"] ?: "none"}\",")
                    append("\"authValue\":\"${params["authValue"] ?: ""}\",")
                    append("\"intervalSeconds\":${params["intervalSeconds"]?.toIntOrNull() ?: 60},")
                    append("\"mappingPreset\":\"${params["mappingPreset"] ?: "raw"}\"")
                    append("}")
                }

                overlayRepository.updateBindingConfig(tenantId, bindingId, configJson)
                call.respondRedirect("/admin/overlay/bindings/$bindingId")
            }

            // POST /admin/overlay/bindings/:id/delete - Delete binding
            post("/{id}/delete") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@post
                }

                val tenantId = TenantId(session.tenantId)
                val bindingId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

                if (bindingId != null) {
                    overlayRepository.deleteBinding(tenantId, bindingId)
                }

                call.respondRedirect("/admin/overlay/bindings")
            }
        }
    }
}

private fun extractWidgetTypes(definitionJson: String): List<String> {
    return try {
        val json = Json.decodeFromString<JsonObject>(definitionJson)
        json["widgets"]?.let { widgets ->
            // Extract widget types from definition
            listOf("ticker") // Simplified - would parse actual widget types
        } ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

private fun getTemplateDefinition(template: OverlayTemplate): String = when (template) {
    OverlayTemplate.TICKER -> """{"widgets":[{"type":"ticker","position":"bottom","height":48}]}"""
    OverlayTemplate.KPI_TILES -> """{"widgets":[{"type":"kpi-grid","position":"top-right","columns":2}]}"""
    OverlayTemplate.QUEUE_TABLE -> """{"widgets":[{"type":"table","position":"center","columns":["name","status","wait"]}]}"""
    OverlayTemplate.QR_CARD -> """{"widgets":[{"type":"qr-card","position":"bottom-right","size":120}]}"""
}

private fun formatJson(json: String): String {
    return try {
        val obj = Json.decodeFromString<JsonObject>(json)
        Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), obj)
    } catch (e: Exception) {
        json
    }
}
