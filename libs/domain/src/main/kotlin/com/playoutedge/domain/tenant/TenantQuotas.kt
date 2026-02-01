package com.playoutedge.domain.tenant

data class TenantQuotas(
    val maxStorageBytes: Long = DEFAULT_MAX_STORAGE_BYTES,
    val maxDevices: Int = DEFAULT_MAX_DEVICES,
    val maxConcurrentConnections: Int = DEFAULT_MAX_CONNECTIONS
) {
    companion object {
        const val DEFAULT_MAX_STORAGE_BYTES = 10L * 1024 * 1024 * 1024 // 10 GB
        const val DEFAULT_MAX_DEVICES = 100
        const val DEFAULT_MAX_CONNECTIONS = 50

        val DEFAULT = TenantQuotas()
    }
}

data class QuotaUsage(
    val storageBytes: Long,
    val deviceCount: Long,
    val connectionCount: Int = 0
)

data class QuotaCheck(
    val allowed: Boolean,
    val reason: String? = null
)
