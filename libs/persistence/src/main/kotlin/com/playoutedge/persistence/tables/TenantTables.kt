package com.playoutedge.persistence.tables

import com.playoutedge.domain.enums.TenantStatus
import com.playoutedge.domain.enums.UserRole
import com.playoutedge.domain.enums.UserStatus
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Tenants : UUIDTable("tenants") {
    val name = varchar("name", 255)
    val status = enumerationByName<TenantStatus>("status", 20)
    val createdAt = timestamp("created_at")
}

object Users : UUIDTable("users") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val email = varchar("email", 255)
    val displayName = varchar("display_name", 255)
    val status = enumerationByName<UserStatus>("status", 20)
    val passwordHash = varchar("password_hash", 255)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()

    init {
        uniqueIndex("uq_users_tenant_email", tenantId, email)
    }
}

object UserRoles : Table("user_roles") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val role = enumerationByName<UserRole>("role", 20)

    override val primaryKey = PrimaryKey(userId, role)
}
