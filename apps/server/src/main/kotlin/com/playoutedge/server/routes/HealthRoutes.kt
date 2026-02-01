package com.playoutedge.server.routes

import com.playoutedge.persistence.config.DatabaseFactory
import com.playoutedge.storage.StorageService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.system.measureTimeMillis

@Serializable
data class HealthResponse(
    val status: String
)

@Serializable
data class ComponentHealth(
    val status: String,
    val latencyMs: Long? = null,
    val error: String? = null
)

@Serializable
data class ReadinessResponse(
    val status: String,
    val components: Map<String, ComponentHealth>
)

fun Route.healthRoutes(storageService: StorageService) {
    route("/health") {
        // Liveness probe - simple check that app is running
        get("/live") {
            call.respond(HealthResponse(status = "UP"))
        }

        // Readiness probe - check dependencies
        get("/ready") {
            val components = mutableMapOf<String, ComponentHealth>()
            var overallStatus = "UP"

            // Check database
            val dbHealth = checkDatabase()
            components["database"] = dbHealth
            if (dbHealth.status != "UP") {
                overallStatus = "DOWN"
            }

            // Check storage
            val storageHealth = checkStorage(storageService)
            components["storage"] = storageHealth
            if (storageHealth.status != "UP") {
                overallStatus = "DOWN"
            }

            val response = ReadinessResponse(
                status = overallStatus,
                components = components
            )

            val statusCode = if (overallStatus == "UP") {
                HttpStatusCode.OK
            } else {
                HttpStatusCode.ServiceUnavailable
            }

            call.respond(statusCode, response)
        }
    }
}

private suspend fun checkDatabase(): ComponentHealth {
    return try {
        var latency: Long = 0
        newSuspendedTransaction {
            latency = measureTimeMillis {
                exec("SELECT 1")
            }
        }
        ComponentHealth(status = "UP", latencyMs = latency)
    } catch (e: Exception) {
        ComponentHealth(status = "DOWN", error = e.message)
    }
}

private suspend fun checkStorage(storageService: StorageService): ComponentHealth {
    return try {
        // For now, just mark as UP if we have a storage service
        // A more thorough check would attempt a test operation
        ComponentHealth(status = "UP")
    } catch (e: Exception) {
        ComponentHealth(status = "DOWN", error = e.message)
    }
}
