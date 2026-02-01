package com.playoutedge.server.services

import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.OverlayProfileEntity
import com.playoutedge.persistence.entities.OverlayStateEntity
import com.playoutedge.persistence.repositories.OverlayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
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
