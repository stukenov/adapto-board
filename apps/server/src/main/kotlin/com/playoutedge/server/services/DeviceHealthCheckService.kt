package com.playoutedge.server.services

import com.playoutedge.domain.enums.AlertType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.DeviceRepository
import com.playoutedge.persistence.tables.Devices
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.time.Duration.Companion.minutes

class DeviceHealthCheckService(
    private val deviceRepository: DeviceRepository,
    private val alertService: AlertService
) {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /** Default offline threshold: device is offline if not seen for 5 minutes. */
    private val offlineThreshold = 5.minutes

    fun start() {
        job = scope.launch {
            while (isActive) {
                try {
                    checkDevices()
                } catch (e: Exception) {
                    // Log and continue
                }
                delay(5.minutes.inWholeMilliseconds)
            }
        }
    }

    fun stop() {
        job?.cancel()
    }

    private suspend fun checkDevices() {
        val now = Clock.System.now()
        val threshold = now - offlineThreshold

        // Get all distinct tenant IDs that have devices
        val tenantIds = newSuspendedTransaction {
            Devices.selectAll()
                .withDistinct()
                .map { it[Devices.tenantId].value }
                .distinct()
        }

        for (tenantIdValue in tenantIds) {
            val tenantId = TenantId(tenantIdValue)
            try {
                checkTenantDevices(tenantId, threshold)
            } catch (e: Exception) {
                // Log per-tenant errors and continue with next tenant
            }
        }
    }

    private suspend fun checkTenantDevices(
        tenantId: TenantId,
        threshold: kotlinx.datetime.Instant
    ) {
        val devices = deviceRepository.findAll(tenantId)

        for (device in devices) {
            val lastSeen = device.lastSeenAt

            if (lastSeen == null || lastSeen < threshold) {
                // Device is offline - create alert if not already open
                alertService.createAlert(
                    tenantId = tenantId,
                    type = AlertType.DEVICE_OFFLINE,
                    payload = JsonObject(mapOf(
                        "deviceId" to JsonPrimitive(device.id.value.toString()),
                        "deviceName" to JsonPrimitive(device.displayName),
                        "lastSeen" to JsonPrimitive(lastSeen?.toString() ?: "never")
                    ))
                )
            }
            // Note: Auto-resolve of offline alerts when a device comes back online
            // would require tracking per-device alert state. The current AlertService
            // uses findOpenByType which is per-type, not per-device. For now, alerts
            // are resolved manually or when the alert deduplication in createAlert
            // prevents duplicates for already-alerted devices.
        }
    }
}
