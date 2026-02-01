package com.playoutedge.player.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * HTTP client for API communication with automatic token refresh.
 */
class ApiClient(
    @PublishedApi internal val tokenStorage: TokenStorage,
    @PublishedApi internal val baseUrl: String = "https://api.playoutedge.com"
) {
    @PublishedApi internal val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @PublishedApi internal val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }

        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }

        engine {
            config {
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
            }
        }
    }

    private val refreshMutex = Mutex()
    private var isRefreshing = false

    @Serializable
    private data class RefreshRequest(val refreshToken: String)

    @Serializable
    private data class RefreshResponse(val accessToken: String, val refreshToken: String)

    /**
     * Refresh the access token using the refresh token.
     */
    suspend fun refreshAccessToken(): Boolean = refreshMutex.withLock {
        if (isRefreshing) return@withLock false
        isRefreshing = true

        try {
            val refreshToken = tokenStorage.getRefreshToken() ?: return@withLock false

            val response = client.post("$baseUrl/player/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken))
            }

            if (response.status == HttpStatusCode.OK) {
                val tokens = response.body<RefreshResponse>()
                tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        } finally {
            isRefreshing = false
        }
    }

    /**
     * Make authenticated GET request with automatic 401 retry.
     */
    @PublishedApi
    internal suspend inline fun <reified T> get(path: String): Result<T> = runCatching {
        val response = executeWithAuth { token ->
            client.get("$baseUrl$path") {
                if (token != null) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
                contentType(ContentType.Application.Json)
            }
        }
        response.body()
    }

    /**
     * Make authenticated POST request with automatic 401 retry.
     */
    @PublishedApi
    internal suspend inline fun <reified T, reified R> post(path: String, body: T): Result<R> = runCatching {
        val response = executeWithAuth { token ->
            client.post("$baseUrl$path") {
                if (token != null) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        response.body()
    }

    /**
     * Make unauthenticated POST request (for enrollment).
     */
    @PublishedApi
    internal suspend inline fun <reified T, reified R> postPublic(path: String, body: T): Result<R> = runCatching {
        client.post("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    /**
     * Execute request with automatic 401 → refresh → retry.
     */
    @PublishedApi
    internal suspend inline fun executeWithAuth(
        crossinline request: suspend (token: String?) -> HttpResponse
    ): HttpResponse {
        val token = tokenStorage.getAccessToken()
        val response = request(token)

        // If 401, try to refresh and retry once
        if (response.status == HttpStatusCode.Unauthorized) {
            val refreshed = refreshAccessToken()
            if (refreshed) {
                val newToken = tokenStorage.getAccessToken()
                return request(newToken)
            }
        }

        return response
    }

    /**
     * Get raw response for SSE connections.
     */
    suspend fun getStream(path: String): HttpStatement {
        val token = tokenStorage.getAccessToken()
        return client.prepareGet("$baseUrl$path") {
            if (token != null) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            header(HttpHeaders.Accept, "text/event-stream")
        }
    }

    fun close() {
        client.close()
    }
}
