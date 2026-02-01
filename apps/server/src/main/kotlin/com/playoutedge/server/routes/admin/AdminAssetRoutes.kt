package com.playoutedge.server.routes.admin

import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.assets.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

// Default storage limit: 10 GB
private const val DEFAULT_STORAGE_LIMIT_BYTES = 10_737_418_240L

/**
 * Admin asset management routes.
 */
fun Route.adminAssetRoutes(
    assetRepository: AssetRepository
) {
    route("/admin/assets") {
        // GET /admin/assets - List assets
        get {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)

            // Parse filters
            val typeParam = call.request.queryParameters["type"]
            val statusParam = call.request.queryParameters["status"]
            val search = call.request.queryParameters["search"]

            val filters = AssetFilters(
                type = typeParam?.let { runCatching { AssetType.valueOf(it) }.getOrNull() },
                status = statusParam?.let { runCatching { AssetStatus.valueOf(it) }.getOrNull() },
                search = search?.takeIf { it.isNotBlank() }
            )

            // Fetch assets (non-archived)
            var assets = assetRepository.findAllActive(tenantId)

            // Apply filters
            if (filters.type != null) {
                assets = assets.filter { it.type == filters.type }
            }
            if (filters.status != null) {
                assets = assets.filter { it.status == filters.status }
            }
            if (filters.search != null) {
                assets = assets.filter { it.name.contains(filters.search, ignoreCase = true) }
            }

            // Map to view models
            val assetItems = assets.map { asset ->
                AssetViewItem(
                    id = asset.id.value,
                    name = asset.name,
                    type = asset.type,
                    status = asset.status,
                    fileSize = asset.fileSizeBytes ?: 0,
                    duration = asset.durationMs,
                    createdAt = asset.createdAt
                )
            }

            // Get quota info
            val usedBytes = assetRepository.getTotalStorageBytes(tenantId)
            val quota = QuotaInfo(
                usedBytes = usedBytes,
                limitBytes = DEFAULT_STORAGE_LIMIT_BYTES
            )

            call.respondHtml {
                assetsListView(
                    session = session,
                    assets = assetItems,
                    quota = quota,
                    filters = filters
                )
            }
        }

        // GET /admin/assets/:id - Asset detail
        get("/{id}") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val assetId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

            if (assetId == null) {
                call.respondRedirect("/admin/assets")
                return@get
            }

            val asset = assetRepository.findById(tenantId, assetId)
            if (asset == null) {
                call.respondRedirect("/admin/assets")
                return@get
            }

            val assetModel = AssetDetailModel(
                id = asset.id.value,
                name = asset.name,
                type = asset.type,
                status = asset.status,
                mimeType = asset.mimeType,
                fileSize = asset.fileSizeBytes ?: 0,
                duration = asset.durationMs,
                width = asset.width,
                height = asset.height,
                rejectionReason = asset.rejectionReason,
                createdAt = asset.createdAt
            )

            call.respondHtml {
                assetDetailView(
                    session = session,
                    asset = assetModel
                )
            }
        }

        // POST /admin/assets/:id/archive - Archive asset
        post("/{id}/archive") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val assetId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

            if (assetId == null) {
                call.respondRedirect("/admin/assets")
                return@post
            }

            assetRepository.archive(tenantId, assetId)
            call.respondRedirect("/admin/assets")
        }
    }
}
