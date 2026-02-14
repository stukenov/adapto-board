package com.playoutedge.persistence.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object RefreshTokens : UUIDTable("refresh_tokens") {
    val tokenHash = varchar("token_hash", 128).uniqueIndex()
    val subjectId = uuid("subject_id")
    val subjectType = varchar("subject_type", 20)
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")
    val revokedAt = timestamp("revoked_at").nullable()
}

object ApiKeys : UUIDTable("api_keys") {
    val tenantId = reference("tenant_id", Tenants, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val keyHash = varchar("key_hash", 128).uniqueIndex()
    val keyPrefix = varchar("key_prefix", 12)
    val scopes = text("scopes") // stored as comma-separated
    val expiresAt = timestamp("expires_at").nullable()
    val createdAt = timestamp("created_at")
    val revokedAt = timestamp("revoked_at").nullable()
}
