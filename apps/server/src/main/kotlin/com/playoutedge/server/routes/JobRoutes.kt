package com.playoutedge.server.routes

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.persistence.repositories.JobRepository
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.tenantId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class JobResponse(
    val id: String,
    val type: String,
    val status: String,
    val attempts: Int,
    val maxAttempts: Int,
    val nextRunAt: String,
    val errorMessage: String?,
    val completedAt: String?,
    val createdAt: String
)

@Serializable
data class JobsListResponse(
    val jobs: List<JobResponse>
)

fun Route.jobRoutes(jobRepo: JobRepository) {
    authenticate(AUTH_ADMIN) {
        route("/api/admin/jobs") {
            // GET /api/admin/jobs - List jobs
            get {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50

                val jobs = jobRepo.findAll(limit.coerceIn(1, 100))

                call.respond(JobsListResponse(
                    jobs = jobs.map { job ->
                        JobResponse(
                            id = job.id.value.toString(),
                            type = job.type,
                            status = job.status.name,
                            attempts = job.attempts,
                            maxAttempts = job.maxAttempts,
                            nextRunAt = job.nextRunAt.toString(),
                            errorMessage = job.errorMessage,
                            completedAt = job.completedAt?.toString(),
                            createdAt = job.createdAt.toString()
                        )
                    }
                ))
            }

            // GET /api/admin/jobs/{id} - Get job details
            get("/{id}") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val jobId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid job ID")

                val job = jobRepo.findById(jobId)
                    ?: throw ApiException.notFound(ErrorCode.NOT_FOUND, "Job not found")

                call.respond(JobResponse(
                    id = job.id.value.toString(),
                    type = job.type,
                    status = job.status.name,
                    attempts = job.attempts,
                    maxAttempts = job.maxAttempts,
                    nextRunAt = job.nextRunAt.toString(),
                    errorMessage = job.errorMessage,
                    completedAt = job.completedAt?.toString(),
                    createdAt = job.createdAt.toString()
                ))
            }
        }
    }
}
