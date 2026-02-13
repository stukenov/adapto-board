package com.playoutedge.persistence.repositories.impl

import com.playoutedge.domain.enums.TenantStatus
import com.playoutedge.persistence.entities.TenantEntity
import com.playoutedge.persistence.repositories.TenantRepository
import com.playoutedge.persistence.tables.Tenants
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TenantRepositoryImpl : TenantRepository {

    override suspend fun create(name: String): TenantEntity = newSuspendedTransaction {
        TenantEntity.new {
            this.name = name
            this.status = TenantStatus.ACTIVE
            this.createdAt = Clock.System.now()
        }
    }

    override suspend fun findByName(name: String): TenantEntity? = newSuspendedTransaction {
        TenantEntity.find { Tenants.name eq name }.firstOrNull()
    }

    override suspend fun findById(id: UUID): TenantEntity? = newSuspendedTransaction {
        TenantEntity.findById(id)
    }
}
