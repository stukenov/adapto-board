package com.playoutedge.tts

import java.security.MessageDigest

object GecTokenGenerator {
    private const val TRUSTED_CLIENT_TOKEN = "6A5AA1D4EAFF4E9FB37E23D68491D6F4"
    private const val WIN_EPOCH_DIFF = 116444736000000000L
    private const val TICKS_PER_SEC = 10_000_000L

    fun generate(): String {
        val ticks = (System.currentTimeMillis() / 1000) * TICKS_PER_SEC + WIN_EPOCH_DIFF
        // Round down to nearest 5 minutes (300 seconds)
        val roundedTicks = ticks - (ticks % (300 * TICKS_PER_SEC))
        val input = "${roundedTicks}${TRUSTED_CLIENT_TOKEN}"
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02X".format(it) }
    }

    fun getTrustedClientToken(): String = TRUSTED_CLIENT_TOKEN
}
