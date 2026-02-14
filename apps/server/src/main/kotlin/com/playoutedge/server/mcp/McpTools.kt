package com.playoutedge.server.mcp

import com.playoutedge.domain.enums.ActionType
import com.playoutedge.domain.enums.AlertStatus
import com.playoutedge.domain.enums.AlertType
import com.playoutedge.domain.enums.ChannelStatus
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AsrunFilters
import com.playoutedge.persistence.repositories.AuditFilters
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.server.services.*
import kotlinx.datetime.Instant
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

fun registerAllTools(
    registry: McpToolRegistry,
    channelService: ChannelService,
    deviceService: DeviceService,
    deviceActionService: DeviceActionService,
    scheduleService: ScheduleService,
    overlayService: OverlayService,
    auditService: AuditService,
    asrunService: AsrunService,
    alertService: AlertService,
    assetRepository: AssetRepository
) {
    // === Channels ===
    registry.register(
        McpToolDefinition(
            name = "channels_list",
            description = "List all channels for the tenant",
            inputSchema = toolSchema {}
        )
    ) { ctx, _ ->
        val channels = channelService.findAll(TenantId(ctx.tenantId))
        buildJsonArray {
            channels.forEach { ch ->
                addJsonObject {
                    put("id", ch.id.value.toString())
                    put("name", ch.name)
                    put("status", ch.status.name)
                    put("defaultOverlayProfileId", ch.defaultOverlayProfileId?.toString())
                    put("createdAt", ch.createdAt.toString())
                }
            }
        }
    }

    registry.register(
        McpToolDefinition(
            name = "channels_create",
            description = "Create a new channel",
            inputSchema = toolSchema {
                properties { stringProp("name", "Channel display name") }
                required("name")
            }
        )
    ) { ctx, args ->
        val name = args["name"]!!.jsonPrimitive.content
        val ch = channelService.create(TenantId(ctx.tenantId), name)
        buildJsonObject {
            put("id", ch.id.value.toString())
            put("name", ch.name)
            put("status", ch.status.name)
            put("createdAt", ch.createdAt.toString())
        }
    }

    registry.register(
        McpToolDefinition(
            name = "channels_get",
            description = "Get a channel by ID",
            inputSchema = toolSchema {
                properties { stringProp("id", "Channel UUID") }
                required("id")
            }
        )
    ) { ctx, args ->
        val id = UUID.fromString(args["id"]!!.jsonPrimitive.content)
        val ch = channelService.findById(TenantId(ctx.tenantId), id)
            ?: return@register JsonPrimitive("Channel not found")
        buildJsonObject {
            put("id", ch.id.value.toString())
            put("name", ch.name)
            put("status", ch.status.name)
            put("defaultOverlayProfileId", ch.defaultOverlayProfileId?.toString())
            put("createdAt", ch.createdAt.toString())
        }
    }

    registry.register(
        McpToolDefinition(
            name = "channels_update",
            description = "Update a channel's name or status",
            inputSchema = toolSchema {
                properties {
                    stringProp("id", "Channel UUID")
                    stringProp("name", "New name (optional)")
                    stringProp("status", "ACTIVE, PAUSED, or ARCHIVED (optional)")
                }
                required("id")
            }
        )
    ) { ctx, args ->
        val id = UUID.fromString(args["id"]!!.jsonPrimitive.content)
        val name = args["name"]?.jsonPrimitive?.contentOrNull
        val status = args["status"]?.jsonPrimitive?.contentOrNull?.let { ChannelStatus.valueOf(it) }
        val ch = channelService.update(TenantId(ctx.tenantId), id, name = name, status = status)
            ?: return@register JsonPrimitive("Channel not found")
        buildJsonObject {
            put("id", ch.id.value.toString())
            put("name", ch.name)
            put("status", ch.status.name)
        }
    }

    registry.register(
        McpToolDefinition(
            name = "channels_delete",
            description = "Archive (soft-delete) a channel",
            inputSchema = toolSchema {
                properties { stringProp("id", "Channel UUID") }
                required("id")
            }
        )
    ) { ctx, args ->
        val id = UUID.fromString(args["id"]!!.jsonPrimitive.content)
        val ok = channelService.archive(TenantId(ctx.tenantId), id)
        JsonPrimitive(if (ok) "Channel archived" else "Channel not found")
    }

    // === Assets ===
    registry.register(
        McpToolDefinition(
            name = "assets_list",
            description = "List all active assets",
            inputSchema = toolSchema {}
        )
    ) { ctx, _ ->
        val assets = assetRepository.findAllActive(TenantId(ctx.tenantId))
        buildJsonArray {
            assets.forEach { a ->
                addJsonObject {
                    put("id", a.id.value.toString())
                    put("name", a.name)
                    put("type", a.type.name)
                    put("status", a.status.name)
                    put("mimeType", a.mimeType)
                    put("sizeBytes", a.fileSizeBytes)
                    put("durationMs", a.durationMs)
                    put("createdAt", a.createdAt.toString())
                }
            }
        }
    }

    registry.register(
        McpToolDefinition(
            name = "assets_get",
            description = "Get an asset by ID",
            inputSchema = toolSchema {
                properties { stringProp("id", "Asset UUID") }
                required("id")
            }
        )
    ) { ctx, args ->
        val id = UUID.fromString(args["id"]!!.jsonPrimitive.content)
        val a = assetRepository.findById(TenantId(ctx.tenantId), id)
            ?: return@register JsonPrimitive("Asset not found")
        buildJsonObject {
            put("id", a.id.value.toString())
            put("name", a.name)
            put("type", a.type.name)
            put("status", a.status.name)
            put("mimeType", a.mimeType)
            put("sizeBytes", a.fileSizeBytes)
            put("durationMs", a.durationMs)
            put("width", a.width)
            put("height", a.height)
            put("createdAt", a.createdAt.toString())
        }
    }

    registry.register(
        McpToolDefinition(
            name = "assets_delete",
            description = "Archive (soft-delete) an asset",
            inputSchema = toolSchema {
                properties { stringProp("id", "Asset UUID") }
                required("id")
            }
        )
    ) { ctx, args ->
        val id = UUID.fromString(args["id"]!!.jsonPrimitive.content)
        val result = assetRepository.archive(TenantId(ctx.tenantId), id)
        JsonPrimitive(if (result != null) "Asset archived" else "Asset not found")
    }

    // === Devices ===
    registry.register(
        McpToolDefinition(
            name = "devices_list",
            description = "List all devices with online status",
            inputSchema = toolSchema {}
        )
    ) { ctx, _ ->
        val devices = deviceService.findAll(TenantId(ctx.tenantId))
        buildJsonArray {
            devices.forEach { d ->
                addJsonObject {
                    put("id", d.id.value.toString())
                    put("displayName", d.displayName)
                    put("enrollStatus", d.enrollStatus.name)
                    put("assignedChannelId", d.assignedChannelId?.toString())
                    put("lastSeenAt", d.lastSeenAt?.toString())
                    put("isOnline", deviceService.isOnline(d.lastSeenAt))
                    put("appVersion", d.appVersion)
                    put("createdAt", d.createdAt.toString())
                }
            }
        }
    }

    registry.register(
        McpToolDefinition(
            name = "devices_get",
            description = "Get a device by ID",
            inputSchema = toolSchema {
                properties { stringProp("id", "Device UUID") }
                required("id")
            }
        )
    ) { ctx, args ->
        val id = UUID.fromString(args["id"]!!.jsonPrimitive.content)
        val d = deviceService.findById(TenantId(ctx.tenantId), id)
            ?: return@register JsonPrimitive("Device not found")
        buildJsonObject {
            put("id", d.id.value.toString())
            put("displayName", d.displayName)
            put("enrollStatus", d.enrollStatus.name)
            put("assignedChannelId", d.assignedChannelId?.toString())
            put("lastSeenAt", d.lastSeenAt?.toString())
            put("isOnline", deviceService.isOnline(d.lastSeenAt))
            put("appVersion", d.appVersion)
            put("androidModel", d.androidModel)
            put("androidVersion", d.androidVersion)
            put("createdAt", d.createdAt.toString())
        }
    }

    registry.register(
        McpToolDefinition(
            name = "devices_update",
            description = "Update device display name",
            inputSchema = toolSchema {
                properties {
                    stringProp("id", "Device UUID")
                    stringProp("displayName", "New display name")
                }
                required("id")
            }
        )
    ) { ctx, args ->
        val id = UUID.fromString(args["id"]!!.jsonPrimitive.content)
        val name = args["displayName"]?.jsonPrimitive?.contentOrNull
        val d = deviceService.update(TenantId(ctx.tenantId), id, displayName = name)
            ?: return@register JsonPrimitive("Device not found")
        buildJsonObject {
            put("id", d.id.value.toString())
            put("displayName", d.displayName)
        }
    }

    registry.register(
        McpToolDefinition(
            name = "devices_assign_channel",
            description = "Assign or unassign a channel to a device",
            inputSchema = toolSchema {
                properties {
                    stringProp("id", "Device UUID")
                    stringProp("channelId", "Channel UUID (null to unassign)")
                }
                required("id")
            }
        )
    ) { ctx, args ->
        val id = UUID.fromString(args["id"]!!.jsonPrimitive.content)
        val channelId = args["channelId"]?.jsonPrimitive?.contentOrNull?.let { UUID.fromString(it) }
        val d = deviceService.assignChannel(TenantId(ctx.tenantId), id, channelId)
            ?: return@register JsonPrimitive("Device not found")
        buildJsonObject {
            put("id", d.id.value.toString())
            put("assignedChannelId", d.assignedChannelId?.toString())
        }
    }

    registry.register(
        McpToolDefinition(
            name = "devices_actions_create",
            description = "Send an action command to a device (REBOOT, REFRESH_PLAYLIST, SCREENSHOT, UPDATE, FACTORY_RESET)",
            inputSchema = toolSchema {
                properties {
                    stringProp("deviceId", "Device UUID")
                    stringProp("action", "Action type: REBOOT, REFRESH_PLAYLIST, SCREENSHOT, UPDATE, FACTORY_RESET")
                }
                required("deviceId", "action")
            }
        )
    ) { ctx, args ->
        val deviceId = UUID.fromString(args["deviceId"]!!.jsonPrimitive.content)
        val actionType = ActionType.valueOf(args["action"]!!.jsonPrimitive.content)
        val action = deviceActionService.createAction(
            tenantId = TenantId(ctx.tenantId),
            deviceId = deviceId,
            actionType = actionType,
            params = null,
            createdBy = ctx.userId
        )
        buildJsonObject {
            put("id", action.id.value.toString())
            put("action", action.action.name)
            put("status", action.status.name)
            put("createdAt", action.createdAt.toString())
        }
    }

    // === Schedules ===
    registry.register(
        McpToolDefinition(
            name = "schedules_list",
            description = "List schedule versions for a channel",
            inputSchema = toolSchema {
                properties { stringProp("channelId", "Channel UUID") }
                required("channelId")
            }
        )
    ) { ctx, args ->
        val channelId = UUID.fromString(args["channelId"]!!.jsonPrimitive.content)
        val versions = scheduleService.findVersionsByChannel(TenantId(ctx.tenantId), channelId)
        buildJsonArray {
            versions.forEach { v ->
                addJsonObject {
                    put("id", v.id.value.toString())
                    put("version", v.version)
                    put("state", v.state.name)
                    put("publishedAt", v.publishedAt?.toString())
                    put("createdAt", v.createdAt.toString())
                }
            }
        }
    }

    registry.register(
        McpToolDefinition(
            name = "schedules_get",
            description = "Get a schedule version by ID",
            inputSchema = toolSchema {
                properties { stringProp("versionId", "Schedule version UUID") }
                required("versionId")
            }
        )
    ) { ctx, args ->
        val versionId = UUID.fromString(args["versionId"]!!.jsonPrimitive.content)
        val v = scheduleService.findVersionById(TenantId(ctx.tenantId), versionId)
            ?: return@register JsonPrimitive("Version not found")
        // Map entity to JSON inside transaction to safely access lazy ref `channel`
        newSuspendedTransaction {
            buildJsonObject {
                put("id", v.id.value.toString())
                put("channelId", v.channel.id.value.toString())
                put("version", v.version)
                put("state", v.state.name)
                put("publishedAt", v.publishedAt?.toString())
                put("createdAt", v.createdAt.toString())
            }
        }
    }

    registry.register(
        McpToolDefinition(
            name = "schedule_items_get",
            description = "Get all items in a schedule version",
            inputSchema = toolSchema {
                properties { stringProp("versionId", "Schedule version UUID") }
                required("versionId")
            }
        )
    ) { ctx, args ->
        val versionId = UUID.fromString(args["versionId"]!!.jsonPrimitive.content)
        // Load items AND map to JSON in same transaction to safely access lazy ref `asset`
        newSuspendedTransaction {
            val items = scheduleService.getItemsByVersion(TenantId(ctx.tenantId), versionId)
            buildJsonArray {
                items.forEach { item ->
                    addJsonObject {
                        put("id", item.id.value.toString())
                        put("assetId", item.asset.id.value.toString())
                        put("orderIndex", item.orderIndex)
                        put("validFrom", item.validFrom?.toString())
                        put("validTo", item.validTo?.toString())
                        put("daysOfWeek", item.daysOfWeek)
                        put("timeStart", item.timeStart?.toString())
                        put("timeEnd", item.timeEnd?.toString())
                        put("weight", item.weight)
                    }
                }
            }
        }
    }

    registry.register(
        McpToolDefinition(
            name = "schedule_items_update",
            description = "Replace all items in a draft schedule version",
            inputSchema = toolSchema {
                properties {
                    stringProp("versionId", "Schedule version UUID")
                    putJsonObject("items") {
                        put("type", "array")
                        put("description", "Array of schedule items")
                        putJsonObject("items") {
                            put("type", "object")
                            putJsonObject("properties") {
                                putJsonObject("assetId") { put("type", "string") }
                                putJsonObject("orderIndex") { put("type", "integer") }
                                putJsonObject("weight") { put("type", "integer") }
                            }
                        }
                    }
                }
                required("versionId", "items")
            }
        )
    ) { ctx, args ->
        val versionId = UUID.fromString(args["versionId"]!!.jsonPrimitive.content)
        val itemsJson = args["items"]!!.jsonArray
        val items = itemsJson.map { item ->
            val obj = item.jsonObject
            ScheduleItemInput(
                assetId = UUID.fromString(obj["assetId"]!!.jsonPrimitive.content),
                orderIndex = obj["orderIndex"]?.jsonPrimitive?.intOrNull ?: 0,
                weight = obj["weight"]?.jsonPrimitive?.intOrNull ?: 1
            )
        }
        val result = scheduleService.replaceItems(TenantId(ctx.tenantId), versionId, items)
        JsonPrimitive("Replaced ${result.size} items")
    }

    registry.register(
        McpToolDefinition(
            name = "schedule_publish",
            description = "Publish a draft schedule version, making it active",
            inputSchema = toolSchema {
                properties { stringProp("versionId", "Schedule version UUID") }
                required("versionId")
            }
        )
    ) { ctx, args ->
        val versionId = UUID.fromString(args["versionId"]!!.jsonPrimitive.content)
        when (val result = scheduleService.publish(TenantId(ctx.tenantId), versionId)) {
            is PublishResult.Success -> JsonPrimitive("Published version ${result.version.version}")
            is PublishResult.VersionNotFound -> JsonPrimitive("Error: Version not found")
            is PublishResult.VersionNotDraft -> JsonPrimitive("Error: Only draft versions can be published")
            is PublishResult.ScheduleEmpty -> JsonPrimitive("Error: Cannot publish empty schedule")
            is PublishResult.AssetsNotReady -> JsonPrimitive("Error: Some assets are not ready: ${result.assetIds}")
        }
    }

    registry.register(
        McpToolDefinition(
            name = "schedule_rollback",
            description = "Rollback to a previous schedule version",
            inputSchema = toolSchema {
                properties {
                    stringProp("channelId", "Channel UUID")
                    intProp("version", "Version number to rollback to")
                }
                required("channelId", "version")
            }
        )
    ) { ctx, args ->
        val channelId = UUID.fromString(args["channelId"]!!.jsonPrimitive.content)
        val version = args["version"]!!.jsonPrimitive.int
        when (val result = scheduleService.rollback(TenantId(ctx.tenantId), channelId, version)) {
            is RollbackResult.Success -> JsonPrimitive("Rolled back to version ${result.version.version}")
            is RollbackResult.VersionNotFound -> JsonPrimitive("Error: Version not found")
            is RollbackResult.AssetsUnavailable -> JsonPrimitive("Error: Some assets are unavailable: ${result.assetIds}")
        }
    }

    // === Overlays ===
    registry.register(
        McpToolDefinition(
            name = "overlay_profiles_list",
            description = "List all overlay profiles",
            inputSchema = toolSchema {}
        )
    ) { ctx, _ ->
        val profiles = overlayService.findAllProfiles(TenantId(ctx.tenantId))
        buildJsonArray {
            profiles.forEach { p ->
                addJsonObject {
                    put("id", p.id.value.toString())
                    put("name", p.name)
                    put("definition", p.definitionJson)
                    put("createdAt", p.createdAt.toString())
                }
            }
        }
    }

    registry.register(
        McpToolDefinition(
            name = "overlay_profiles_create",
            description = "Create a new overlay profile",
            inputSchema = toolSchema {
                properties {
                    stringProp("name", "Profile name")
                    putJsonObject("definition") {
                        put("type", "object")
                        put("description", "Overlay profile definition JSON")
                    }
                }
                required("name", "definition")
            }
        )
    ) { ctx, args ->
        val name = args["name"]!!.jsonPrimitive.content
        val definition = args["definition"]!!.jsonObject
        val p = overlayService.createProfile(TenantId(ctx.tenantId), name, definition)
        buildJsonObject {
            put("id", p.id.value.toString())
            put("name", p.name)
            put("createdAt", p.createdAt.toString())
        }
    }

    registry.register(
        McpToolDefinition(
            name = "overlay_state_get",
            description = "Get current overlay state for a channel",
            inputSchema = toolSchema {
                properties { stringProp("channelId", "Channel UUID") }
                required("channelId")
            }
        )
    ) { ctx, args ->
        val channelId = UUID.fromString(args["channelId"]!!.jsonPrimitive.content)
        val state = overlayService.getState(TenantId(ctx.tenantId), channelId)
            ?: return@register JsonPrimitive("No state for channel")
        buildJsonObject {
            put("channelId", channelId.toString())
            put("state", state.stateJson)
            put("version", state.version)
            put("updatedAt", state.updatedAt.toString())
        }
    }

    registry.register(
        McpToolDefinition(
            name = "overlay_state_update",
            description = "Replace overlay state for a channel",
            inputSchema = toolSchema {
                properties {
                    stringProp("channelId", "Channel UUID")
                    putJsonObject("state") {
                        put("type", "object")
                        put("description", "New overlay state JSON")
                    }
                }
                required("channelId", "state")
            }
        )
    ) { ctx, args ->
        val channelId = UUID.fromString(args["channelId"]!!.jsonPrimitive.content)
        val state = args["state"]!!.jsonObject
        val result = overlayService.setState(TenantId(ctx.tenantId), channelId, state)
        buildJsonObject {
            put("version", result.version)
            put("updatedAt", result.updatedAt.toString())
        }
    }

    // === Reports ===
    registry.register(
        McpToolDefinition(
            name = "audit_logs",
            description = "Query audit log entries with filters",
            inputSchema = toolSchema {
                properties {
                    stringProp("entityType", "Filter by entity type (optional)")
                    stringProp("action", "Filter by action (optional)")
                    intProp("limit", "Max results, default 100")
                    intProp("offset", "Offset, default 0")
                }
            }
        )
    ) { ctx, args ->
        val filters = AuditFilters(
            entityType = args["entityType"]?.jsonPrimitive?.contentOrNull,
            action = args["action"]?.jsonPrimitive?.contentOrNull,
            limit = args["limit"]?.jsonPrimitive?.intOrNull ?: 100,
            offset = args["offset"]?.jsonPrimitive?.intOrNull ?: 0
        )
        val result = auditService.query(TenantId(ctx.tenantId), filters)
        buildJsonObject {
            put("total", result.total)
            putJsonArray("items") {
                result.items.forEach { log ->
                    addJsonObject {
                        put("id", log.id.value.toString())
                        put("actorType", log.actorType.name)
                        put("action", log.action)
                        put("entityType", log.entityType)
                        put("entityId", log.entityId.toString())
                        put("createdAt", log.createdAt.toString())
                    }
                }
            }
        }
    }

    registry.register(
        McpToolDefinition(
            name = "asrun_events",
            description = "Query as-run playback events with filters",
            inputSchema = toolSchema {
                properties {
                    stringProp("deviceId", "Filter by device UUID (optional)")
                    stringProp("channelId", "Filter by channel UUID (optional)")
                    stringProp("from", "ISO 8601 start datetime (optional)")
                    stringProp("to", "ISO 8601 end datetime (optional)")
                    intProp("limit", "Max results, default 100")
                    intProp("offset", "Offset, default 0")
                }
            }
        )
    ) { ctx, args ->
        val filters = AsrunFilters(
            deviceId = args["deviceId"]?.jsonPrimitive?.contentOrNull?.let { UUID.fromString(it) },
            channelId = args["channelId"]?.jsonPrimitive?.contentOrNull?.let { UUID.fromString(it) },
            from = args["from"]?.jsonPrimitive?.contentOrNull?.let { Instant.parse(it) },
            to = args["to"]?.jsonPrimitive?.contentOrNull?.let { Instant.parse(it) },
            limit = args["limit"]?.jsonPrimitive?.intOrNull ?: 100,
            offset = args["offset"]?.jsonPrimitive?.intOrNull ?: 0
        )
        val result = asrunService.query(TenantId(ctx.tenantId), filters)
        // Map entities inside transaction to safely access lazy ref `device`
        newSuspendedTransaction {
            buildJsonObject {
                put("total", result.total)
                putJsonArray("events") {
                    result.events.forEach { event ->
                        addJsonObject {
                            put("id", event.id.value.toString())
                            put("deviceId", event.device.id.value.toString())
                            put("eventType", event.eventType.name)
                            put("at", event.at.toString())
                            put("createdAt", event.createdAt.toString())
                        }
                    }
                }
            }
        }
    }

    registry.register(
        McpToolDefinition(
            name = "alerts_list",
            description = "List alerts with optional status/type filters",
            inputSchema = toolSchema {
                properties {
                    stringProp("status", "ACTIVE, ACKNOWLEDGED, or RESOLVED (optional)")
                    stringProp("type", "Alert type filter (optional)")
                    intProp("limit", "Max results, default 50")
                    intProp("offset", "Offset, default 0")
                }
            }
        )
    ) { ctx, args ->
        val status = args["status"]?.jsonPrimitive?.contentOrNull?.let { AlertStatus.valueOf(it) }
        val type = args["type"]?.jsonPrimitive?.contentOrNull?.let { AlertType.valueOf(it) }
        val limit = args["limit"]?.jsonPrimitive?.intOrNull ?: 50
        val offset = args["offset"]?.jsonPrimitive?.intOrNull ?: 0
        val result = alertService.query(TenantId(ctx.tenantId), status, type, limit, offset)
        buildJsonArray {
            result.alerts.forEach { alert ->
                addJsonObject {
                    put("id", alert.id.value.toString())
                    put("type", alert.type.name)
                    put("status", alert.status.name)
                    put("payload", alert.payloadJson)
                    put("firstSeenAt", alert.firstSeenAt.toString())
                    put("lastSeenAt", alert.lastSeenAt.toString())
                    put("resolvedAt", alert.resolvedAt?.toString())
                }
            }
        }
    }
}
