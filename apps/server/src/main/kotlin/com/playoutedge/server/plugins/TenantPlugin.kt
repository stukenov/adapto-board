package com.playoutedge.server.plugins

import com.playoutedge.domain.tenant.TenantContext
import com.playoutedge.domain.tenant.TenantId
import io.ktor.server.application.*
import io.ktor.util.*

private val TenantContextKey = AttributeKey<TenantContext>("TenantContext")

/**
 * TenantPlugin is kept for compatibility but tenant context is now resolved lazily
 * on first access (after JWT auth has run) rather than eagerly in onCall (which runs
 * before authentication and therefore always sees null claims).
 */
val TenantPlugin = createApplicationPlugin(name = "TenantPlugin") {
    // Intentionally empty — tenant context is resolved lazily via the extension properties below.
}

private fun ApplicationCall.resolveTenantContext(): TenantContext? {
    attributes.getOrNull(TenantContextKey)?.let { return it }

    val adminClaims = this.adminClaims
    val deviceClaims = this.deviceClaims

    val context = when {
        adminClaims != null -> TenantContext(
            tenantId = TenantId(adminClaims.tenantId),
            userId = adminClaims.subject,
            roles = setOf(adminClaims.role)
        )
        deviceClaims != null -> TenantContext(
            tenantId = TenantId(deviceClaims.tenantId),
            userId = null,
            roles = emptySet()
        )
        else -> null
    }

    if (context != null) {
        attributes.put(TenantContextKey, context)
    }
    return context
}

val ApplicationCall.tenantContext: TenantContext?
    get() = resolveTenantContext()

val ApplicationCall.tenantContextOrThrow: TenantContext
    get() = tenantContext ?: throw IllegalStateException("TenantContext not available - authentication required")

val ApplicationCall.tenantId: TenantId?
    get() = tenantContext?.tenantId

val ApplicationCall.tenantIdOrThrow: TenantId
    get() = tenantId ?: throw IllegalStateException("TenantId not available - authentication required")
