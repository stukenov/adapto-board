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
    val windowDuration: Duration = 1.minutes
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
    var lastCleanup = Clock.System.now()

    onCall { call ->
        // Periodic cleanup of stale buckets (every 5 minutes)
        val now = Clock.System.now()
        if ((now - lastCleanup) > 5.minutes) {
            lastCleanup = now
            adminBuckets.entries.removeIf { it.value.getResetTime() < now }
            playerBuckets.entries.removeIf { it.value.getResetTime() < now }
        }
        val path = call.request.local.uri

        // Determine if admin or player API
        val (buckets, limit) = when {
            path.startsWith("/api/admin") -> adminBuckets to config.adminLimit
            path.startsWith("/api/player") || path.startsWith("/api/device") -> playerBuckets to config.playerLimit
            else -> return@onCall
        }

        // Get identifier (user ID for admin, device ID for player)
        val identifier = call.tokenClaims?.subject?.toString() ?: call.request.local.remoteHost

        val bucket = buckets.computeIfAbsent(identifier) {
            RateLimitBucket(limit, config.windowDuration)
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
