package com.playoutedge.domain.tenant

import com.playoutedge.domain.enums.UserRole
import java.util.UUID

@JvmInline
value class TenantId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun fromString(value: String): TenantId = TenantId(UUID.fromString(value))
    }
}

data class TenantContext(
    val tenantId: TenantId,
    val userId: UUID? = null,
    val roles: Set<UserRole> = emptySet()
) {
    val isAdmin: Boolean
        get() = UserRole.TENANT_ADMIN in roles || UserRole.SUPPORT_ADMIN in roles

    val isSupport: Boolean
        get() = UserRole.SUPPORT_AGENT in roles || UserRole.SUPPORT_ADMIN in roles

    fun hasRole(role: UserRole): Boolean = role in roles
}
