package com.playoutedge.persistence.repositories

import com.playoutedge.persistence.entities.TenantEntity
import java.util.UUID

interface TenantRepository {
    suspend fun create(name: String): TenantEntity
    suspend fun findByName(name: String): TenantEntity?
    suspend fun findById(id: UUID): TenantEntity?
}
