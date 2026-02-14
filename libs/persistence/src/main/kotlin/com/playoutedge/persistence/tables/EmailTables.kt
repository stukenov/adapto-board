package com.playoutedge.persistence.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PasswordResetTokens : UUIDTable("password_reset_tokens") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val token = varchar("token", 64).uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val usedAt = timestamp("used_at").nullable()
    val createdAt = timestamp("created_at")
}

object EmailVerificationTokens : UUIDTable("email_verification_tokens") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val token = varchar("token", 64).uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")
}
