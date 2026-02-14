package com.playoutedge.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class RateLimitConfig(
    val adminLimit: Int = 100,
    val playerLimit: Int = 60,
    val windowDuration: Duration = 1.minutes,
    val loginLimit: Int = 10,
    val loginWindowDuration: Duration = 15.minutes,
    val signupLimit: Int = 5,
    val forgotPasswordLimit: Int = 5,
    val signupWindowDuration: Duration = 60.minutes,
    val forgotPasswordWindowDuration: Duration = 60.minutes,
    val manifestLimit: Int = 100,
    val manifestWindowDuration: Duration = 1.minutes
)

class RateLimitBucket(
    private val limit: Int,
    private val windowDuration: Duration
) {
    private var tokens: Int = limit
    private var lastRefill: Instant = Clock.System.now()

    @Synchronized
    fun tryConsume(): Pair<Boolean, Int> {
        refillIfNeeded()
        return if (tokens > 0) {
            tokens--
            true to tokens
        } else {
            false to 0
        }
    }

    @Synchronized
    fun getRemaining(): Int {
        refillIfNeeded()
        return tokens
    }

    private fun refillIfNeeded() {
        val now = Clock.System.now()
        if ((now - lastRefill) >= windowDuration) {
            tokens = limit
            lastRefill = now
        }
    }

    fun getResetTime(): Instant = lastRefill + windowDuration
}

val RateLimitPlugin = createApplicationPlugin(name = "RateLimitPlugin") {
    val config = RateLimitConfig()
    val adminBuckets = ConcurrentHashMap<String, RateLimitBucket>()
    val playerBuckets = ConcurrentHashMap<String, RateLimitBucket>()
    val loginBuckets = ConcurrentHashMap<String, RateLimitBucket>()
    val signupBuckets = ConcurrentHashMap<String, RateLimitBucket>()
    val forgotPasswordBuckets = ConcurrentHashMap<String, RateLimitBucket>()
    val manifestBuckets = ConcurrentHashMap<String, RateLimitBucket>()
    var lastCleanup = Clock.System.now()

    onCall { call ->
        // Periodic cleanup of stale buckets (every 5 minutes)
        val now = Clock.System.now()
        if ((now - lastCleanup) > 5.minutes) {
            lastCleanup = now
            adminBuckets.entries.removeIf { it.value.getResetTime() < now }
            playerBuckets.entries.removeIf { it.value.getResetTime() < now }
            loginBuckets.entries.removeIf { it.value.getResetTime() < now }
            signupBuckets.entries.removeIf { it.value.getResetTime() < now }
            forgotPasswordBuckets.entries.removeIf { it.value.getResetTime() < now }
            manifestBuckets.entries.removeIf { it.value.getResetTime() < now }
        }
        val path = call.request.local.uri
        val method = call.request.local.method

        // Check signup, forgot-password, and manifest routes first (IP-based)
        val ipIdentifier = call.request.local.remoteHost

        val (buckets, limit, windowDuration) = when {
            method == HttpMethod.Post && path == "/admin/login" ->
                Triple(loginBuckets, config.loginLimit, config.loginWindowDuration)
            method == HttpMethod.Post && path.startsWith("/signup") ->
                Triple(signupBuckets, config.signupLimit, config.signupWindowDuration)
            method == HttpMethod.Post && path.startsWith("/admin/forgot-password") ->
                Triple(forgotPasswordBuckets, config.forgotPasswordLimit, config.forgotPasswordWindowDuration)
            path.startsWith("/embed/") && path.contains("manifest") ->
                Triple(manifestBuckets, config.manifestLimit, config.manifestWindowDuration)
            // Existing API rate limits
            path.startsWith("/api/admin") ->
                Triple(adminBuckets, config.adminLimit, config.windowDuration)
            path.startsWith("/api/player") || path.startsWith("/api/device") ->
                Triple(playerBuckets, config.playerLimit, config.windowDuration)
            else -> return@onCall
        }

        // Get identifier (IP for auth/manifest routes, user/device ID for API routes)
        val identifier = when {
            path == "/admin/login" || path.startsWith("/signup") || path.startsWith("/admin/forgot-password") || path.startsWith("/embed/") ->
                ipIdentifier
            else ->
                call.tokenClaims?.subject?.toString() ?: ipIdentifier
        }

        val bucket = buckets.computeIfAbsent(identifier) {
            RateLimitBucket(limit, windowDuration)
        }

        val (allowed, remaining) = bucket.tryConsume()
        val resetTime = bucket.getResetTime()

        // Add rate limit headers
        call.response.header("X-RateLimit-Limit", limit.toString())
        call.response.header("X-RateLimit-Remaining", remaining.toString())
        call.response.header("X-RateLimit-Reset", resetTime.epochSeconds.toString())

        if (!allowed) {
            call.respond(HttpStatusCode.TooManyRequests, mapOf(
                "error" to "Rate limit exceeded",
                "retryAfter" to (resetTime.epochSeconds - Clock.System.now().epochSeconds)
            ))
        }
    }
}
