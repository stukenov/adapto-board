package com.playoutedge.contracts.error

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: JsonObject? = null,
    val requestId: String
)
