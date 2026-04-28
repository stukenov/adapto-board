package com.playoutedge.server.routes.admin

import com.playoutedge.domain.enums.BindingStatus
import com.playoutedge.domain.enums.OverlaySourceType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.ChannelRepository
import com.playoutedge.persistence.repositories.OverlayRepository
import com.playoutedge.persistence.repositories.WebhookLogRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.adminLayout
import com.playoutedge.server.views.displayName
import com.playoutedge.server.views.overlay.*
import com.playoutedge.server.views.pageHeader
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.playoutedge.persistence.entities.OverlayBindingEntity
import com.playoutedge.persistence.tables.OverlayBindings
import com.playoutedge.server.services.OverlayService
import com.playoutedge.server.services.WebhookService
import com.playoutedge.server.services.WidgetTemplateService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import kotlinx.html.*
import java.util.UUID

private fun getWebhookBaseUrl(): String {
    return System.getenv("APP_BASE_URL")?.trimEnd('/') ?: "https://tv.adapto.kz"
}

/**
 * Admin overlay management routes.
 */
fun Route.adminOverlayRoutes(
    overlayRepository: OverlayRepository,
    channelRepository: ChannelRepository,
    webhookLogRepository: WebhookLogRepository,
    overlayService: OverlayService,
    webhookService: WebhookService? = null,
    widgetTemplateService: WidgetTemplateService
) {
    route("/admin/overlay") {
        // Main overlay page - redirect to profiles
        get {
            call.respondRedirect("/admin/overlay/profiles")
        }

        // POST /admin/overlay/webhook-logs/:id/retry - Retry webhook log
        post("/webhook-logs/{id}/retry") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
            val logId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            if (logId != null) {
                newSuspendedTransaction {
                    val log = com.playoutedge.persistence.entities.WebhookLogEntity.findById(logId)
                    if (log != null) {
                        log.retryCount = log.retryCount + 1
                    }
                }
            }
            call.respondRedirect("/admin/overlay")
        }

        // === Template Catalog ===
        get("/templates") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }
            val tenantId = TenantId(session.tenantId)
            val templates = widgetTemplateService.getTemplates(tenantId)
            call.respondHtml {
                overlayTemplateCatalogView(session, templates)
            }
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
                val page = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
                val pageSize = 50
                val (profiles, totalCount) = overlayRepository.findAllProfilesPaged(tenantId, pageSize, (page - 1) * pageSize)

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

                val totalPages = ((totalCount + pageSize - 1) / pageSize).toInt().coerceAtLeast(1)
                val pagination = com.playoutedge.server.views.PaginationInfo(
                    currentPage = page,
                    totalPages = totalPages,
                    totalItems = totalCount,
                    basePath = "/admin/overlay/profiles"
                )

                call.respondHtml {
                    overlayProfilesListView(session, profileItems, pagination)
                }
            }

            // GET /admin/overlay/profiles/new - New profile form
            get("/new") {
                val session = call.adminSession ?: run {
                    call.respondRedirect("/admin/login")
                    return@get
                }

                val tenantId = TenantId(session.tenantId)
                val templates = widgetTemplateService.getTemplates(tenantId)

                call.respondHtml {
                    newOverlayProfileView(session, templates = templates)
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
                val templateCode = params["template"]
                val customJson = params["definitionJson"]?.takeIf { it.isNotBlank() }

                if (name.isBlank()) {
                    call.respondHtml {
                        newOverlayProfileView(session, error = "Name is required")
                    }
                    return@post
                }

                val definitionJson = customJson ?: if (templateCode != null) {
                    widgetTemplateService.getTemplateDefinition(tenantId, templateCode)
                } else {
                    """{"widgets":[]}"""
                }

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

                val result = newSuspendedTransaction {
                    val profile = overlayRepository.findProfileById(tenantId, profileId)
                        ?: return@newSuspendedTransaction null

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

                    Pair(profileDetail, bindingItems)
                }

                if (result == null) {
                    call.respondRedirect("/admin/overlay/profiles")
                    return@get
                }

                call.respondHtml {
                    overlayProfileDetailView(session, result.first, result.second)
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

                val profileDetail = newSuspendedTransaction {
                    val profile = overlayRepository.findProfileById(tenantId, profileId)
                        ?: return@newSuspendedTransaction null
                    OverlayProfileDetail(
                        id = profile.id.value,
                        name = profile.name,
                        definitionJson = formatJson(profile.definitionJson.toString()),
                        createdAt = profile.createdAt
                    )
                }

                if (profileDetail == null) {
                    call.respondRedirect("/admin/overlay/profiles")
                    return@get
                }

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
                    val profileDetail = newSuspendedTransaction {
                        val profile = overlayRepository.findProfileById(tenantId, profileId)
                            ?: return@newSuspendedTransaction null
                        OverlayProfileDetail(
                            id = profile.id.value,
                            name = profile.name,
                            definitionJson = definitionJson,
                            createdAt = profile.createdAt
                        )
                    }
                    if (profileDetail != null) {
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

                val (bindingItems, profileItems, channelOptions) = newSuspendedTransaction {
                    val bindings = overlayRepository.findAllBindings(tenantId)
                    val profiles = overlayRepository.findAllProfiles(tenantId)
                    val channels = channelRepository.findAll(tenantId)

                    val bi = bindings.map { binding ->
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

                    val pi = profiles.map { profile ->
                        OverlayProfileItem(
                            id = profile.id.value,
                            name = profile.name,
                            widgetTypes = emptyList(),
                            bindingsCount = 0,
                            createdAt = profile.createdAt
                        )
                    }

                    val co = channels.map { channel ->
                        ChannelOption(id = channel.id.value, name = channel.name)
                    }

                    Triple(bi, pi, co)
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

                val (profileItems, channelOptions) = newSuspendedTransaction {
                    val profiles = overlayRepository.findAllProfiles(tenantId)
                    val channels = channelRepository.findAll(tenantId)

                    val pi = profiles.map { profile ->
                        OverlayProfileItem(
                            id = profile.id.value,
                            name = profile.name,
                            widgetTypes = emptyList(),
                            bindingsCount = 0,
                            createdAt = profile.createdAt
                        )
                    }

                    val co = channels.map { channel ->
                        ChannelOption(id = channel.id.value, name = channel.name)
                    }

                    Pair(pi, co)
                }

                val preselectedChannelId = call.request.queryParameters["channel"]?.let {
                    runCatching { UUID.fromString(it) }.getOrNull()
                }

                call.respondHtml {
                    newOverlayBindingView(session, profileItems, channelOptions, preselectedChannelId = preselectedChannelId)
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

                // Auto-populate overlay state from profile definition with initial data
                val definitionJson = newSuspendedTransaction {
                    overlayRepository.findProfileById(tenantId, profileId)?.definitionJson?.toString()
                }
                if (definitionJson != null) {
                    val initialState = widgetTemplateService.buildInitialState(tenantId, definitionJson)
                    val stateObj = Json.decodeFromString<JsonObject>(initialState)
                    overlayService.setBindingState(tenantId, channelId, binding.id.value, stateObj)
                }

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

                val result = newSuspendedTransaction {
                    val binding = overlayRepository.findBindingById(tenantId, bindingId)
                        ?: return@newSuspendedTransaction null

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

                    val profileDefJson = binding.overlayProfile.definitionJson.toString()
                    val channelId = binding.channel.id.value
                    val fullState = overlayRepository.getState(tenantId, channelId)?.stateJson
                    val currentState = if (fullState != null) {
                        overlayService.filterStateForBinding(fullState, bindingId)
                    } else null

                    val bindingDetail = OverlayBindingDetail(
                        id = binding.id.value,
                        profileId = binding.overlayProfile.id.value,
                        profileName = binding.overlayProfile.name,
                        channelId = channelId,
                        channelName = binding.channel.name,
                        sourceType = binding.sourceType,
                        sourceConfigJson = formatJson(binding.sourceConfigJson.toString()),
                        status = binding.status,
                        webhookSecret = binding.webhookSecret,
                        webhookUrl = binding.webhookSecret?.let { "${getWebhookBaseUrl()}/api/webhook/${binding.id.value}" },
                        createdAt = binding.createdAt,
                        profileDefinitionJson = formatJson(profileDefJson),
                        currentStateJson = formatJson(currentState?.toString() ?: "{}")
                    )

                    Pair(bindingDetail, webhookLogs)
                }

                if (result == null) {
                    call.respondRedirect("/admin/overlay/bindings")
                    return@get
                }

                call.respondHtml {
                    overlayBindingDetailView(session, result.first, result.second)
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
                    val channelId = newSuspendedTransaction {
                        overlayRepository.findBindingById(tenantId, bindingId)?.channel?.id?.value
                    }
                    overlayRepository.updateBindingStatus(tenantId, bindingId, BindingStatus.PAUSED)
                    if (channelId != null) {
                        overlayService.removeBindingWidgets(tenantId, channelId, bindingId)
                    }
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
                    // Re-populate state from profile definition
                    val info = newSuspendedTransaction {
                        val binding = overlayRepository.findBindingById(tenantId, bindingId)
                            ?: return@newSuspendedTransaction null
                        Pair(binding.channel.id.value, binding.overlayProfile.definitionJson.toString())
                    }
                    if (info != null) {
                        val (channelId, defJson) = info
                        val initialState = widgetTemplateService.buildInitialState(tenantId, defJson)
                        val stateObj = Json.decodeFromString<JsonObject>(initialState)
                        overlayService.setBindingState(tenantId, channelId, bindingId, stateObj)
                    }
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

                val channelId = newSuspendedTransaction {
                    val binding = overlayRepository.findBindingById(tenantId, bindingId)
                        ?: return@newSuspendedTransaction null
                    binding.channel.id.value
                }
                if (channelId == null) {
                    call.respondRedirect("/admin/overlay/bindings")
                    return@post
                }

                val params = call.receiveParameters()
                val rawStateJson = params["stateJson"] ?: "{}"

                // Auto-migrate: wrap in widgets[] if missing
                val stateJson = migrateOverlayState(rawStateJson)
                val stateObj = Json.decodeFromString<JsonObject>(stateJson)

                overlayService.setBindingState(tenantId, channelId, bindingId, stateObj)
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
                val configObj = kotlinx.serialization.json.buildJsonObject {
                    put("url", kotlinx.serialization.json.JsonPrimitive(params["url"] ?: ""))
                    put("authType", kotlinx.serialization.json.JsonPrimitive(params["authType"] ?: "none"))
                    put("authValue", kotlinx.serialization.json.JsonPrimitive(params["authValue"] ?: ""))
                    put("intervalSeconds", kotlinx.serialization.json.JsonPrimitive(params["intervalSeconds"]?.toIntOrNull() ?: 60))
                    put("mappingPreset", kotlinx.serialization.json.JsonPrimitive(params["mappingPreset"] ?: "raw"))
                }
                val configJson = Json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), configObj)

                overlayRepository.updateBindingConfig(tenantId, bindingId, configJson)
                call.respondRedirect("/admin/overlay/bindings/$bindingId")
            }

            // GET /admin/overlay/bindings/:id/preview - Iframe preview
            get("/{id}/preview") {
                val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@get }
                val tenantId = TenantId(session.tenantId)
                val bindingId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                if (bindingId == null) { call.respondRedirect("/admin/overlay/bindings"); return@get }

                val bindingInfo = newSuspendedTransaction {
                    val binding = overlayRepository.findBindingById(tenantId, bindingId)
                        ?: return@newSuspendedTransaction null
                    Triple(binding.channel.name, binding.channel.id.value, binding.id.value)
                }
                if (bindingInfo == null) { call.respondRedirect("/admin/overlay/bindings"); return@get }

                val (channelName, channelId, bId) = bindingInfo
                call.respondHtml {
                    adminLayout(title = "Overlay Preview", userName = session.displayName, currentPath = "/admin/overlay") {
                        pageHeader(
                            title = "Preview: $channelName",
                            backHref = "/admin/overlay/bindings/$bId",
                            backLabel = "Back to Binding"
                        )
                        div("preview-container") {
                            iframe {
                                src = "/embed/$channelId"
                                style = "width:100%;height:600px;border:1px solid #ddd;border-radius:8px;"
                            }
                        }
                    }
                }
            }

            // POST /admin/overlay/bindings/:id/test-webhook - Send test payload
            post("/{id}/test-webhook") {
                val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
                val tenantId = TenantId(session.tenantId)
                val bindingId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                if (bindingId == null) { call.respondRedirect("/admin/overlay/bindings"); return@post }

                val params = call.receiveParameters()
                val testPayload = params["payload"] ?: "{}"

                // Validate JSON
                try { Json.parseToJsonElement(testPayload) }
                catch (e: Exception) { call.respondRedirect("/admin/overlay/bindings/$bindingId?error=Invalid+JSON+payload"); return@post }

                val channelId = newSuspendedTransaction {
                    val binding = overlayRepository.findBindingById(tenantId, bindingId)
                        ?: return@newSuspendedTransaction null
                    binding.channel.id.value
                }
                if (channelId == null) { call.respondRedirect("/admin/overlay/bindings"); return@post }

                val testStateObj = Json.decodeFromString<JsonObject>(testPayload)
                overlayService.setBindingState(tenantId, channelId, bindingId, testStateObj)
                call.respondRedirect("/admin/overlay/bindings/$bindingId?success=Test+payload+sent")
            }

            // POST /admin/overlay/bindings/:id/update-schedule - Set schedule start/end
            post("/{id}/update-schedule") {
                val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
                val bindingId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                if (bindingId == null) { call.respondRedirect("/admin/overlay/bindings"); return@post }

                val params = call.receiveParameters()
                newSuspendedTransaction {
                    OverlayBindings.update({ (OverlayBindings.id eq bindingId) and (OverlayBindings.tenantId eq session.tenantId) }) {
                        it[scheduleStart] = params["scheduleStart"]?.takeIf { s -> s.isNotBlank() }?.let { s ->
                            kotlinx.datetime.Instant.parse(s + "T00:00:00Z")
                        }
                        it[scheduleEnd] = params["scheduleEnd"]?.takeIf { s -> s.isNotBlank() }?.let { s ->
                            kotlinx.datetime.Instant.parse(s + "T23:59:59Z")
                        }
                    }
                }
                call.respondRedirect("/admin/overlay/bindings/$bindingId?success=Schedule+updated")
            }

            // POST /admin/overlay/bindings/:id/regenerate-secret - Regenerate webhook secret
            post("/{id}/regenerate-secret") {
                val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
                val tenantId = TenantId(session.tenantId)
                val bindingId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                if (bindingId == null) { call.respondRedirect("/admin/overlay/bindings"); return@post }

                val exists = newSuspendedTransaction {
                    overlayRepository.findBindingById(tenantId, bindingId) != null
                }
                if (!exists) { call.respondRedirect("/admin/overlay/bindings"); return@post }

                webhookService?.regenerateSecret(bindingId)
                call.respondRedirect("/admin/overlay/bindings/$bindingId?success=Webhook+secret+regenerated")
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
                    val channelId = newSuspendedTransaction {
                        overlayRepository.findBindingById(tenantId, bindingId)?.channel?.id?.value
                    }
                    if (channelId != null) {
                        overlayService.removeBindingWidgets(tenantId, channelId, bindingId)
                    }
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
        val widgets = json["widgets"] as? kotlinx.serialization.json.JsonArray ?: return emptyList()
        widgets.mapNotNull { widget ->
            (widget as? JsonObject)?.get("type")?.let { type ->
                (type as? kotlinx.serialization.json.JsonPrimitive)?.content
            }
        }.distinct()
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * Migrate old overlay state format: if JSON doesn't contain "widgets",
 * attempt to wrap known top-level keys as widgets.
 */
internal fun migrateOverlayState(json: String): String {
    return try {
        val obj = Json.decodeFromString<JsonObject>(json)
        if (obj.containsKey("widgets")) return json

        // Old format: top-level keys like {"ticker": {"text": "..."}}
        // Convert to {"widgets": [{"type": "ticker", "text": "..."}]}
        val widgetTypes = setOf("ticker", "kpi-grid", "kpi", "table", "qr-card", "clock",
            "poll", "weather", "reactions", "news-ticker", "queue-table",
            "logo", "animated-logo", "lower-third", "score", "breaking-news", "countdown")
        val widgets = kotlinx.serialization.json.buildJsonArray {
            obj.forEach { (key, value) ->
                if (key in widgetTypes && value is JsonObject) {
                    add(kotlinx.serialization.json.buildJsonObject {
                        put("type", kotlinx.serialization.json.JsonPrimitive(key))
                        value.forEach { (k, v) -> put(k, v) }
                    })
                }
            }
        }
        if (widgets.isEmpty()) return json

        val result = kotlinx.serialization.json.buildJsonObject {
            put("widgets", widgets)
        }
        Json.encodeToString(JsonObject.serializer(), result)
    } catch (_: Exception) {
        json
    }
}

private fun formatJson(json: String): String {
    return try {
        val obj = Json.decodeFromString<JsonObject>(json)
        Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), obj)
    } catch (e: Exception) {
        json
    }
}
