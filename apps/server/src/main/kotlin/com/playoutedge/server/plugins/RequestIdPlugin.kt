package com.playoutedge.server.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import org.slf4j.MDC
import java.util.UUID

private const val REQUEST_ID_HEADER = "X-Request-Id"
private const val MDC_REQUEST_ID_KEY = "requestId"

val RequestIdKey = AttributeKey<String>("RequestId")

val RequestIdPlugin = createApplicationPlugin(name = "RequestIdPlugin") {
    onCall { call ->
        val requestId = call.request.header(REQUEST_ID_HEADER) ?: UUID.randomUUID().toString()
        call.attributes.put(RequestIdKey, requestId)
        MDC.put(MDC_REQUEST_ID_KEY, requestId)
    }

    onCallRespond { call, _ ->
        val requestId = call.attributes.getOrNull(RequestIdKey)
        if (requestId != null) {
            call.response.header(REQUEST_ID_HEADER, requestId)
        }
        MDC.remove(MDC_REQUEST_ID_KEY)
    }
}

val ApplicationCall.requestId: String
    get() = attributes.getOrNull(RequestIdKey) ?: "unknown"
