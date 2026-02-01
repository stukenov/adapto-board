package com.playoutedge.server.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.JsonObject
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

sealed class OverlayEvent {
    data class State(val state: JsonObject, val version: Long) : OverlayEvent()
    data class Patch(val patch: JsonObject, val version: Long) : OverlayEvent()
    data object Keepalive : OverlayEvent()
}

class OverlaySubscribers {
    private val channels = ConcurrentHashMap<UUID, MutableSharedFlow<OverlayEvent>>()

    fun subscribe(channelId: UUID): Flow<OverlayEvent> {
        val flow = channels.computeIfAbsent(channelId) {
            MutableSharedFlow(replay = 0, extraBufferCapacity = 64)
        }
        return flow.asSharedFlow()
    }

    suspend fun broadcast(channelId: UUID, event: OverlayEvent) {
        channels[channelId]?.emit(event)
    }

    fun getSubscriberCount(channelId: UUID): Int {
        return channels[channelId]?.subscriptionCount?.value ?: 0
    }
}
