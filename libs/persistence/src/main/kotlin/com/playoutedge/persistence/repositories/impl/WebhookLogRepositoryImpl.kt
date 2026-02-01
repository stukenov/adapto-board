package com.playoutedge.persistence.repositories.impl

import com.playoutedge.persistence.entities.OverlayBindingEntity
import com.playoutedge.persistence.entities.WebhookLogEntity
import com.playoutedge.persistence.repositories.CreateWebhookLog
import com.playoutedge.persistence.repositories.WebhookLogRepository
import com.playoutedge.persistence.tables.WebhookLogs
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.time.Duration.Companion.days

class WebhookLogRepositoryImpl : WebhookLogRepository {

    override suspend fun create(log: CreateWebhookLog): WebhookLogEntity =
        newSuspendedTransaction {
            val binding = OverlayBindingEntity.findById(log.bindingId)
                ?: error("Binding not found: ${log.bindingId}")

            WebhookLogEntity.new {
                this.binding = binding
                this.statusCode = log.statusCode
                this.latencyMs = log.latencyMs
                this.payloadSize = log.payloadSize
                this.error = log.error
                this.createdAt = Clock.System.now()
            }
        }

    override suspend fun findByBinding(bindingId: UUID, limit: Int): List<WebhookLogEntity> =
        newSuspendedTransaction {
            WebhookLogEntity.find {
                WebhookLogs.bindingId eq bindingId
            }.orderBy(WebhookLogs.createdAt to SortOrder.DESC)
                .limit(limit)
                .toList()
        }

    override suspend fun deleteOldLogs(retentionDays: Int): Long =
        newSuspendedTransaction {
            val cutoff = Clock.System.now() - retentionDays.days
            WebhookLogs.deleteWhere {
                createdAt less cutoff
            }.toLong()
        }
}
