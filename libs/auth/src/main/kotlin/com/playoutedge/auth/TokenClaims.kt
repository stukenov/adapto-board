package com.playoutedge.auth

import com.playoutedge.domain.enums.UserRole
import java.util.UUID

sealed class TokenClaims {
    abstract val subject: UUID
    abstract val tenantId: UUID
    abstract val type: TokenType
}

data class AdminClaims(
    override val subject: UUID,
    override val tenantId: UUID,
    val role: UserRole
) : TokenClaims() {
    override val type: TokenType = TokenType.ADMIN
}

data class DeviceClaims(
    override val subject: UUID,
    override val tenantId: UUID,
    val channelId: UUID?
) : TokenClaims() {
    override val type: TokenType = TokenType.DEVICE
}

enum class TokenType {
    ADMIN,
    DEVICE
}
