package com.playoutedge.server.services

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.OverlayProfileEntity
import com.playoutedge.persistence.entities.OverlayStateEntity
import com.playoutedge.persistence.repositories.OverlayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID

class OverlayService(
    private val overlayRepo: OverlayRepository,
    private val subscribers: OverlaySubscribers
) {
    companion object {
        const val MAX_STATE_SIZE = 256 * 1024 // 256KB
    }

    // Profile management
    suspend fun findAllProfiles(tenantId: TenantId): List<OverlayProfileEntity> {
        return overlayRepo.findAllProfiles(tenantId)
    }

    suspend fun findProfileById(tenantId: TenantId, profileId: UUID): OverlayProfileEntity? {
        return overlayRepo.findProfileById(tenantId, profileId)
    }

    suspend fun createProfile(tenantId: TenantId, name: String, definition: JsonObject): OverlayProfileEntity {
        return overlayRepo.createProfile(tenantId, name, Json.encodeToString(JsonObject.serializer(), definition))
    }

    // State management
    suspend fun getState(tenantId: TenantId, channelId: UUID): OverlayStateEntity? {
        return overlayRepo.getState(tenantId, channelId)
    }

    suspend fun setState(tenantId: TenantId, channelId: UUID, state: JsonObject): OverlayStateEntity {
        val stateJson = Json.encodeToString(JsonObject.serializer(), state)
        if (stateJson.length > MAX_STATE_SIZE) {
            throw IllegalArgumentException("State exceeds maximum size of ${MAX_STATE_SIZE} bytes")
        }

        val result = overlayRepo.setState(tenantId, channelId, stateJson)

        // Broadcast full state to subscribers
        subscribers.broadcast(channelId, OverlayEvent.State(result.stateJson, result.version))

        return result
    }

    suspend fun patchState(tenantId: TenantId, channelId: UUID, patch: JsonObject): OverlayStateEntity? {
        val current = overlayRepo.getState(tenantId, channelId) ?: return null

        val newState = applyPatch(current.stateJson, patch)
        val stateJson = Json.encodeToString(JsonObject.serializer(), newState)

        if (stateJson.length > MAX_STATE_SIZE) {
            throw IllegalArgumentException("State exceeds maximum size of ${MAX_STATE_SIZE} bytes")
        }

        val newVersion = current.version + 1
        val result = overlayRepo.updateState(tenantId, channelId, stateJson, newVersion)

        // Broadcast patch to subscribers
        if (result != null) {
            val patchWithVersion = buildJsonObject {
                put("version", newVersion)
                patch["upsert"]?.let { put("upsert", it) }
                patch["remove"]?.let { put("remove", it) }
            }
            subscribers.broadcast(channelId, OverlayEvent.Patch(patchWithVersion, newVersion))
        }

        return result
    }

    /**
     * Merge-on-write: tag widgets with _bindingId, replace only this binding's widgets,
     * keep widgets from other bindings intact.
     */
    suspend fun setBindingState(tenantId: TenantId, channelId: UUID, bindingId: UUID, newState: JsonObject): OverlayStateEntity {
        val taggedWidgets = tagWidgets(newState["widgets"]?.jsonArray, bindingId)

        val current = overlayRepo.getState(tenantId, channelId)
        val existingWidgets = if (current != null) {
            val stateObj = Json.decodeFromString<JsonObject>(
                Json.encodeToString(JsonObject.serializer(), current.stateJson)
            )
            stateObj["widgets"]?.jsonArray?.filter { widget ->
                val tag = (widget as? JsonObject)?.get("_bindingId")?.jsonPrimitive?.content
                tag != bindingId.toString()
            } ?: emptyList()
        } else {
            emptyList()
        }

        val mergedWidgets = buildJsonArray {
            existingWidgets.forEach { add(it) }
            taggedWidgets.forEach { add(it) }
        }

        val mergedState = buildJsonObject {
            newState.forEach { (key, value) ->
                if (key != "widgets") put(key, value)
            }
            put("widgets", mergedWidgets)
        }

        return setState(tenantId, channelId, mergedState)
    }

    /**
     * Remove all widgets belonging to a specific binding from channel state.
     */
    suspend fun removeBindingWidgets(tenantId: TenantId, channelId: UUID, bindingId: UUID) {
        val current = overlayRepo.getState(tenantId, channelId) ?: return
        val stateObj = Json.decodeFromString<JsonObject>(
            Json.encodeToString(JsonObject.serializer(), current.stateJson)
        )
        val widgets = stateObj["widgets"]?.jsonArray ?: return

        val filtered = buildJsonArray {
            widgets.forEach { widget ->
                val tag = (widget as? JsonObject)?.get("_bindingId")?.jsonPrimitive?.content
                if (tag != bindingId.toString()) add(widget)
            }
        }

        val newState = buildJsonObject {
            stateObj.forEach { (key, value) ->
                if (key != "widgets") put(key, value)
            }
            put("widgets", filtered)
        }

        setState(tenantId, channelId, newState)
    }

    /**
     * Filter channel state to only return widgets for a specific binding.
     */
    fun filterStateForBinding(stateJson: JsonObject, bindingId: UUID): JsonObject {
        val widgets = stateJson["widgets"]?.jsonArray ?: return stateJson
        val filtered = buildJsonArray {
            widgets.forEach { widget ->
                val tag = (widget as? JsonObject)?.get("_bindingId")?.jsonPrimitive?.content
                if (tag == bindingId.toString()) add(widget)
            }
        }
        return buildJsonObject {
            stateJson.forEach { (key, value) ->
                if (key != "widgets") put(key, value)
            }
            put("widgets", filtered)
        }
    }

    private fun tagWidgets(widgets: JsonArray?, bindingId: UUID): List<JsonObject> {
        if (widgets == null) return emptyList()
        return widgets.map { element ->
            val widget = element.jsonObject
            buildJsonObject {
                widget.forEach { (key, value) -> put(key, value) }
                put("_bindingId", JsonPrimitive(bindingId.toString()))
            }
        }
    }

    // SSE subscription
    fun subscribe(channelId: UUID): Flow<OverlayEvent> {
        return subscribers.subscribe(channelId)
    }

    private fun applyPatch(state: JsonObject, patch: JsonObject): JsonObject {
        val widgets = state["widgets"]?.jsonObject?.toMutableMap() ?: mutableMapOf()

        // Apply upserts
        patch["upsert"]?.jsonArray?.forEach { item ->
            val obj = item.jsonObject
            val id = obj["id"]?.toString()?.trim('"') ?: return@forEach
            widgets[id] = obj
        }

        // Apply removes
        patch["remove"]?.jsonArray?.forEach { item ->
            val id = item.toString().trim('"')
            widgets.remove(id)
        }

        return buildJsonObject {
            state.forEach { (key, value) ->
                if (key != "widgets") {
                    put(key, value)
                }
            }
            put("widgets", buildJsonObject {
                widgets.forEach { (k, v) -> put(k, v) }
            })
        }
    }
}
