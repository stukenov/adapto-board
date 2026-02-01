package com.playoutedge.contracts.error

import kotlinx.serialization.json.JsonObject

sealed class ApiException(
    val code: ErrorCode,
    override val message: String,
    val details: JsonObject? = null
) : Exception(message) {

    class BadRequest(
        code: ErrorCode = ErrorCode.VALIDATION_ERROR,
        message: String,
        details: JsonObject? = null
    ) : ApiException(code, message, details)

    class Unauthorized(
        code: ErrorCode = ErrorCode.TOKEN_INVALID,
        message: String,
        details: JsonObject? = null
    ) : ApiException(code, message, details)

    class Forbidden(
        code: ErrorCode = ErrorCode.FORBIDDEN_ROLE,
        message: String,
        details: JsonObject? = null
    ) : ApiException(code, message, details)

    class NotFound(
        code: ErrorCode = ErrorCode.NOT_FOUND,
        message: String,
        details: JsonObject? = null
    ) : ApiException(code, message, details)

    class Conflict(
        code: ErrorCode = ErrorCode.SCHEDULE_VERSION_CONFLICT,
        message: String,
        details: JsonObject? = null
    ) : ApiException(code, message, details)

    class UnprocessableEntity(
        code: ErrorCode,
        message: String,
        details: JsonObject? = null
    ) : ApiException(code, message, details)

    class InternalError(
        code: ErrorCode = ErrorCode.INTERNAL_ERROR,
        message: String,
        details: JsonObject? = null
    ) : ApiException(code, message, details)

    companion object {
        fun badRequest(code: ErrorCode, message: String, details: JsonObject? = null): BadRequest =
            BadRequest(code, message, details)

        fun unauthorized(code: ErrorCode, message: String, details: JsonObject? = null): Unauthorized =
            Unauthorized(code, message, details)

        fun forbidden(code: ErrorCode, message: String, details: JsonObject? = null): Forbidden =
            Forbidden(code, message, details)

        fun notFound(code: ErrorCode, message: String, details: JsonObject? = null): NotFound =
            NotFound(code, message, details)

        fun conflict(code: ErrorCode, message: String, details: JsonObject? = null): Conflict =
            Conflict(code, message, details)

        fun unprocessable(code: ErrorCode, message: String, details: JsonObject? = null): UnprocessableEntity =
            UnprocessableEntity(code, message, details)

        fun internal(message: String, details: JsonObject? = null): InternalError =
            InternalError(ErrorCode.INTERNAL_ERROR, message, details)
    }
}
