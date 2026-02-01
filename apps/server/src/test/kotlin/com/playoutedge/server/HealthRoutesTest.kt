package com.playoutedge.server

import com.playoutedge.server.routes.healthRoutes
import com.playoutedge.storage.LocalStorageService
import com.playoutedge.storage.StorageConfig
import com.playoutedge.storage.StorageMode
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@DisplayName("Health Routes")
class HealthRoutesTest : DatabaseTestContainer() {

    companion object {
        @TempDir
        @JvmStatic
        lateinit var tempDir: File

        @BeforeAll
        @JvmStatic
        fun setup() {
            initDatabase()
        }
    }

    private fun Application.configureTestRouting() {
        val storageConfig = StorageConfig(
            mode = StorageMode.LOCAL,
            localBasePath = tempDir.absolutePath,
            localBaseUrl = "http://localhost/storage",
            signedUrlSecret = "test-secret"
        )
        val storageService = LocalStorageService(storageConfig)

        install(ContentNegotiation) {
            json()
        }

        routing {
            healthRoutes(storageService)
        }
    }

    @Test
    @DisplayName("GET /health/live should return UP status")
    fun `liveness probe should return UP`() = testApplication {
        application { configureTestRouting() }

        val response = client.get("/health/live")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("UP", body["status"]?.jsonPrimitive?.content)
    }

    @Test
    @DisplayName("GET /health/ready should return UP when all components healthy")
    fun `readiness probe should return UP when healthy`() = testApplication {
        application { configureTestRouting() }

        val response = client.get("/health/ready")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("UP", body["status"]?.jsonPrimitive?.content)
    }
}
