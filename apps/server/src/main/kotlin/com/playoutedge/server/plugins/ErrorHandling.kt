package com.playoutedge.server.plugins

import com.playoutedge.contracts.error.ApiError
import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ErrorHandling")

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            val requestId = call.requestId
            val status = cause.toHttpStatus()
            val error = ApiError(
                code = cause.code.name,
                message = cause.message,
                details = cause.details,
                requestId = requestId
            )

            logger.warn("API error [{}]: {} - {}", requestId, cause.code, cause.message)
            call.respond(status, error)
        }

        exception<Throwable> { call, cause ->
            val requestId = call.requestId
            val error = ApiError(
                code = ErrorCode.INTERNAL_ERROR.name,
                message = "An unexpected error occurred",
                details = null,
                requestId = requestId
            )

            logger.error("Unexpected error [{}]: {}", requestId, cause.message, cause)
            call.respond(HttpStatusCode.InternalServerError, error)
        }
    }
}

private fun ApiException.toHttpStatus(): HttpStatusCode = when (this) {
    is ApiException.BadRequest -> HttpStatusCode.BadRequest
    is ApiException.Unauthorized -> HttpStatusCode.Unauthorized
    is ApiException.Forbidden -> HttpStatusCode.Forbidden
    is ApiException.NotFound -> HttpStatusCode.NotFound
    is ApiException.Conflict -> HttpStatusCode.Conflict
    is ApiException.UnprocessableEntity -> HttpStatusCode.UnprocessableEntity
    is ApiException.InternalError -> HttpStatusCode.InternalServerError
}
