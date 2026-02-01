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
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Smoke tests to verify basic application functionality.
 * These tests ensure critical health endpoints work correctly.
 */
@DisplayName("Smoke Tests")
@Tag("smoke")
class SmokeTest : DatabaseTestContainer() {

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

    private fun Application.configureMinimalApp() {
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
    @DisplayName("Liveness probe should return UP")
    fun `liveness probe should return UP`() = testApplication {
        application { configureMinimalApp() }

        val response = client.get("/health/live")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("UP", body["status"]?.jsonPrimitive?.content)
    }

    @Test
    @DisplayName("Readiness probe should return UP when healthy")
    fun `readiness probe should return UP`() = testApplication {
        application { configureMinimalApp() }

        val response = client.get("/health/ready")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("UP", body["status"]?.jsonPrimitive?.content)
    }

    @Test
    @DisplayName("Readiness response should include database component")
    fun `readiness should include database component`() = testApplication {
        application { configureMinimalApp() }

        val response = client.get("/health/ready")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val components = body["components"]?.jsonObject
        assertNotNull(components, "Health response should include components")
        assertTrue(components!!.containsKey("database"), "Should include database component")
    }

    @Test
    @DisplayName("Readiness response should include storage component")
    fun `readiness should include storage component`() = testApplication {
        application { configureMinimalApp() }

        val response = client.get("/health/ready")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val components = body["components"]?.jsonObject
        assertNotNull(components, "Health response should include components")
        assertTrue(components!!.containsKey("storage"), "Should include storage component")
    }

    @Test
    @DisplayName("Invalid health endpoint should return 404")
    fun `invalid health path should return 404`() = testApplication {
        application { configureMinimalApp() }

        val response = client.get("/health/nonexistent")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    @DisplayName("Database component should be UP when healthy")
    fun `database component should be UP`() = testApplication {
        application { configureMinimalApp() }

        val response = client.get("/health/ready")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val components = body["components"]?.jsonObject
        val database = components?.get("database")?.jsonObject
        assertNotNull(database, "Database component should exist")
        assertEquals("UP", database!!["status"]?.jsonPrimitive?.content)
    }
}
