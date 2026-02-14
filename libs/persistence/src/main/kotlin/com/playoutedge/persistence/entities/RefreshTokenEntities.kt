package com.playoutedge.persistence.entities

import com.playoutedge.persistence.tables.ApiKeys
import com.playoutedge.persistence.tables.RefreshTokens
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class RefreshTokenEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RefreshTokenEntity>(RefreshTokens)

    var tokenHash by RefreshTokens.tokenHash
    var subjectId by RefreshTokens.subjectId
    var subjectType by RefreshTokens.subjectType
    var tenantId by RefreshTokens.tenantId
    var expiresAt by RefreshTokens.expiresAt
    var createdAt by RefreshTokens.createdAt
    var revokedAt by RefreshTokens.revokedAt
}

class ApiKeyEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ApiKeyEntity>(ApiKeys)

    var tenantId by ApiKeys.tenantId
    var name by ApiKeys.name
    var keyHash by ApiKeys.keyHash
    var keyPrefix by ApiKeys.keyPrefix
    var scopes by ApiKeys.scopes
    var expiresAt by ApiKeys.expiresAt
    var createdAt by ApiKeys.createdAt
    var revokedAt by ApiKeys.revokedAt
}
