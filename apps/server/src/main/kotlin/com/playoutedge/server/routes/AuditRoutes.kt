package com.playoutedge.server.routes

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.persistence.repositories.AuditFilters
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.tenantId
import com.playoutedge.server.services.AuditService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.util.UUID

@Serializable
data class AuditLogResponse(
    val id: String,
    val tenantId: String?,
    val actorUserId: String?,
    val actorType: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val diff: JsonObject?,
    val createdAt: String
)

@Serializable
data class AuditQueryResponse(
    val items: List<AuditLogResponse>,
    val total: Long,
    val limit: Int,
    val offset: Int
)

fun Route.auditRoutes(auditService: AuditService) {
    authenticate(AUTH_ADMIN) {
        route("/api/admin/audit") {
            // GET /api/admin/audit - Query audit log
            get {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val entityType = call.request.queryParameters["entityType"]
                val entityId = call.request.queryParameters["entityId"]?.let { UUID.fromString(it) }
                val actorUserId = call.request.queryParameters["actorUserId"]?.let { UUID.fromString(it) }
                val action = call.request.queryParameters["action"]
                val fromDate = call.request.queryParameters["from"]?.let { Instant.parse(it) }
                val toDate = call.request.queryParameters["to"]?.let { Instant.parse(it) }
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                val filters = AuditFilters(
                    entityType = entityType,
                    entityId = entityId,
                    actorUserId = actorUserId,
                    action = action,
                    fromDate = fromDate,
                    toDate = toDate,
                    limit = limit.coerceIn(1, 1000),
                    offset = offset.coerceAtLeast(0)
                )

                val result = auditService.query(tenantId, filters)

                call.respond(AuditQueryResponse(
                    items = result.items.map { log ->
                        AuditLogResponse(
                            id = log.id.value.toString(),
                            tenantId = log.tenant?.id?.value?.toString(),
                            actorUserId = log.actorUser?.id?.value?.toString(),
                            actorType = log.actorType.name,
                            action = log.action,
                            entityType = log.entityType,
                            entityId = log.entityId.toString(),
                            diff = log.diffJson,
                            createdAt = log.createdAt.toString()
                        )
                    },
                    total = result.total,
                    limit = result.limit,
                    offset = result.offset
                ))
            }

            // GET /api/admin/audit/export - Export as CSV
            get("/export") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val entityType = call.request.queryParameters["entityType"]
                val fromDate = call.request.queryParameters["from"]?.let { Instant.parse(it) }
                val toDate = call.request.queryParameters["to"]?.let { Instant.parse(it) }

                val filters = AuditFilters(
                    entityType = entityType,
                    fromDate = fromDate,
                    toDate = toDate,
                    limit = 10000,
                    offset = 0
                )

                val result = auditService.query(tenantId, filters)

                val csv = buildString {
                    appendLine("id,created_at,actor_type,actor_user_id,action,entity_type,entity_id")
                    result.items.forEach { log ->
                        appendLine("${log.id.value},${log.createdAt},${log.actorType.name},${log.actorUser?.id?.value ?: ""},${log.action},${log.entityType},${log.entityId}")
                    }
                }

                call.response.header(
                    HttpHeaders.ContentDisposition,
                    "attachment; filename=\"audit-log.csv\""
                )
                call.respondText(csv, ContentType.Text.CSV)
            }
        }
    }
}
