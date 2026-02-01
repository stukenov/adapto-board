package com.playoutedge.persistence.entities

import com.playoutedge.domain.enums.TenantStatus
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import com.playoutedge.persistence.tables.Tenants
import com.playoutedge.persistence.tables.UserRoles
import com.playoutedge.persistence.tables.Users
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class TenantEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TenantEntity>(Tenants)

    var name by Tenants.name
    var status by Tenants.status
    var supportTier by Tenants.supportTier
    var releaseRing by Tenants.releaseRing
    var maintenanceMode by Tenants.maintenanceMode
    var maintenanceReason by Tenants.maintenanceReason
    var maintenanceUntil by Tenants.maintenanceUntil
    var createdAt by Tenants.createdAt

    val users by UserEntity referrersOn Users.tenantId
}

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserEntity>(Users)

    var tenant by TenantEntity referencedOn Users.tenantId
    val tenantId: UUID get() = readValues[Users.tenantId].value
    var email by Users.email
    var displayName by Users.displayName
    var status by Users.status
    var passwordHash by Users.passwordHash
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt
}
