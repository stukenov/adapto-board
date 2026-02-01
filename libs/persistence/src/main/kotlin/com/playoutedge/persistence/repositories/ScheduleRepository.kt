package com.playoutedge.persistence.repositories

import com.playoutedge.domain.enums.ScheduleState
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.entities.ScheduleItemEntity
import com.playoutedge.persistence.entities.ScheduleVersionEntity
import java.util.UUID

interface ScheduleRepository {
    suspend fun findVersionById(tenantId: TenantId, versionId: UUID): ScheduleVersionEntity?
    suspend fun findVersionsByChannel(tenantId: TenantId, channelId: UUID): List<ScheduleVersionEntity>
    suspend fun findActiveVersion(tenantId: TenantId, channelId: UUID): ScheduleVersionEntity?
    suspend fun findDraftVersion(tenantId: TenantId, channelId: UUID): ScheduleVersionEntity?
    suspend fun createVersion(tenantId: TenantId, channelId: UUID, createdBy: UUID?): ScheduleVersionEntity
    suspend fun updateVersionState(tenantId: TenantId, versionId: UUID, state: ScheduleState): ScheduleVersionEntity?
    suspend fun findItemsByVersion(tenantId: TenantId, versionId: UUID): List<ScheduleItemEntity>
    suspend fun addItem(tenantId: TenantId, versionId: UUID, item: CreateScheduleItemRequest): ScheduleItemEntity
    suspend fun removeItem(tenantId: TenantId, itemId: UUID): Boolean
}

data class CreateScheduleItemRequest(
    val assetId: UUID,
    val orderIndex: Int,
    val validFrom: kotlinx.datetime.LocalDate? = null,
    val validTo: kotlinx.datetime.LocalDate? = null,
    val daysOfWeek: Int? = null,
    val timeStart: kotlinx.datetime.LocalTime? = null,
    val timeEnd: kotlinx.datetime.LocalTime? = null,
    val weight: Int = 1
)
