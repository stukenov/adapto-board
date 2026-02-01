package com.playoutedge.server.routes.admin

import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.domain.enums.AssetType
import com.playoutedge.domain.tenant.TenantId
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.persistence.repositories.CreateAssetRequest
import com.playoutedge.persistence.repositories.QuotaService
import com.playoutedge.persistence.repositories.UpdateAssetRequest
import com.playoutedge.server.plugins.adminSession
import com.playoutedge.server.views.assets.*
import com.playoutedge.storage.AssetUploadService
import com.playoutedge.storage.UploadRequest
import com.playoutedge.storage.ValidationResult
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

// Default storage limit: 10 GB
private const val DEFAULT_STORAGE_LIMIT_BYTES = 10_737_418_240L

/**
 * Admin asset management routes.
 */
fun Route.adminAssetRoutes(
    assetRepository: AssetRepository,
    quotaService: QuotaService,
    assetUploadService: AssetUploadService
) {
    route("/admin/assets") {
        // GET /admin/assets/upload - Upload form (must be before /{id} route)
        get("/upload") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@get
            }

            val tenantId = TenantId(session.tenantId)
            val usedBytes = assetRepository.getTotalStorageBytes(tenantId)
            val quota = QuotaInfo(
                usedBytes = usedBytes,
                limitBytes = DEFAULT_STORAGE_LIMIT_BYTES
            )

            call.respondHtml {
                assetUploadView(
                    session = session,
                    quota = quota
                )
            }
        }

        // POST /admin/assets/upload - Handle file upload
        post("/upload") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val usedBytes = assetRepository.getTotalStorageBytes(tenantId)
            val quota = QuotaInfo(
                usedBytes = usedBytes,
                limitBytes = DEFAULT_STORAGE_LIMIT_BYTES
            )

            val multipart = call.receiveMultipart()
            var filename: String? = null
            var mimeType: String? = null
            var fileBytes: ByteArray? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        filename = part.originalFileName?.takeIf { it.isNotBlank() }
                        mimeType = part.contentType?.toString() ?: "application/octet-stream"
                        fileBytes = part.streamProvider().readBytes()
                    }
                    else -> {}
                }
                part.dispose()
            }

            // Validate file presence
            if (filename == null || fileBytes == null || fileBytes!!.isEmpty()) {
                call.respondHtml {
                    assetUploadView(
                        session = session,
                        quota = quota,
                        error = "Please select a file to upload"
                    )
                }
                return@post
            }

            val contentLength = fileBytes!!.size.toLong()

            // Check quota
            val quotaCheck = quotaService.checkStorageQuota(tenantId, contentLength)
            if (!quotaCheck.allowed) {
                call.respondHtml {
                    assetUploadView(
                        session = session,
                        quota = quota,
                        error = quotaCheck.reason ?: "Storage quota exceeded"
                    )
                }
                return@post
            }

            val assetId = UUID.randomUUID()
            val uploadRequest = UploadRequest(
                tenantId = tenantId.value,
                assetId = assetId,
                filename = filename!!,
                mimeType = mimeType!!,
                contentLength = contentLength
            )

            // Validate format
            val validation = assetUploadService.validateUpload(uploadRequest)
            if (validation is ValidationResult.Invalid) {
                call.respondHtml {
                    assetUploadView(
                        session = session,
                        quota = quota,
                        error = validation.reason
                    )
                }
                return@post
            }

            val assetType = (validation as ValidationResult.Valid).assetType

            // Upload to storage
            val uploadResult = assetUploadService.upload(
                uploadRequest,
                fileBytes!!.inputStream()
            )

            if (uploadResult.validationError != null) {
                call.respondHtml {
                    assetUploadView(
                        session = session,
                        quota = quota,
                        error = uploadResult.validationError
                    )
                }
                return@post
            }

            // Create asset record
            val asset = assetRepository.create(
                tenantId,
                CreateAssetRequest(
                    type = assetType,
                    name = filename!!,
                    mimeType = mimeType!!,
                    storageKey = uploadResult.storageKey,
                    fileSizeBytes = uploadResult.sizeBytes,
                    checksumSha256 = uploadResult.checksumSha256,
                    createdBy = session.subject
                )
            )

            // Mark as ready (no transcoding in MVP)
            assetRepository.update(tenantId, asset.id.value, UpdateAssetRequest(status = AssetStatus.READY))

            // Redirect to asset detail page
            call.respondRedirect("/admin/assets/${asset.id.value}")
        }

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
