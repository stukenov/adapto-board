package com.playoutedge.server.plugins

import com.playoutedge.domain.tenant.TenantContext
import com.playoutedge.domain.tenant.TenantId
import io.ktor.server.application.*
import io.ktor.util.*

private val TenantContextKey = AttributeKey<TenantContext>("TenantContext")

val TenantPlugin = createApplicationPlugin(name = "TenantPlugin") {
    onCall { call ->
        // Try to get tenant context from admin claims first, then device claims
        val adminClaims = call.adminClaims
        val deviceClaims = call.deviceClaims

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
            call.attributes.put(TenantContextKey, context)
        }
    }
}

val ApplicationCall.tenantContext: TenantContext?
    get() = attributes.getOrNull(TenantContextKey)

val ApplicationCall.tenantContextOrThrow: TenantContext
    get() = tenantContext ?: throw IllegalStateException("TenantContext not available - authentication required")

val ApplicationCall.tenantId: TenantId?
    get() = tenantContext?.tenantId

val ApplicationCall.tenantIdOrThrow: TenantId
    get() = tenantId ?: throw IllegalStateException("TenantId not available - authentication required")
