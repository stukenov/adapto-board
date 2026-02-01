package com.playoutedge.server.jobs

import kotlinx.serialization.json.JsonObject

interface JobHandler {
    val type: String

    suspend fun handle(payload: JsonObject): JobResult
}

sealed class JobResult {
    data object Success : JobResult()
    data class Failure(val error: String) : JobResult()
    data class Reschedule(val delayMinutes: Int) : JobResult()
}
