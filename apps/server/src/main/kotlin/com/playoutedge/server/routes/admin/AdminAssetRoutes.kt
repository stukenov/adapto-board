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
import java.io.File
import java.security.MessageDigest
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
            var tempFile: File? = null

            try {
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        filename = part.originalFileName?.takeIf { it.isNotBlank() }
                        mimeType = part.contentType?.toString() ?: "application/octet-stream"
                        tempFile = File.createTempFile("upload-", ".tmp").also { tf ->
                            part.streamProvider().use { input ->
                                tf.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            // Validate file presence
            if (filename == null || tempFile == null || tempFile!!.length() == 0L) {
                call.respondHtml {
                    assetUploadView(
                        session = session,
                        quota = quota,
                        error = "Please select a file to upload"
                    )
                }
                return@post
            }

            val contentLength = tempFile!!.length()

            // Compute SHA-256 checksum for duplicate detection
            val digest = MessageDigest.getInstance("SHA-256")
            tempFile!!.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            val fileChecksum = digest.digest().joinToString("") { "%02x".format(it) }

            // Check for duplicate files
            var duplicateWarning: String? = null
            val duplicates = assetRepository.findByChecksumSha256(tenantId, fileChecksum)
            if (duplicates.isNotEmpty()) {
                duplicateWarning = "Warning: A file with the same content already exists (${duplicates.first().name}). Upload proceeded anyway."
            }

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
                tempFile!!.inputStream()
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
            val redirectUrl = if (duplicateWarning != null) {
                "/admin/assets/${asset.id.value}?success=${java.net.URLEncoder.encode(duplicateWarning, "UTF-8")}"
            } else {
                "/admin/assets/${asset.id.value}"
            }
            call.respondRedirect(redirectUrl)
            } finally {
                tempFile?.delete()
            }
        }

        // POST /admin/assets/bulk-upload - Handle multiple file upload
        post("/bulk-upload") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val multipart = call.receiveMultipart()
            var successCount = 0
            val tempFiles = mutableListOf<File>()

            try {
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            val fname = part.originalFileName?.takeIf { it.isNotBlank() } ?: run {
                                part.dispose()
                                return@forEachPart
                            }
                            val mime = part.contentType?.toString() ?: "application/octet-stream"
                            val tempFile = File.createTempFile("bulk-upload-", ".tmp").also { tf ->
                                part.streamProvider().use { input ->
                                    tf.outputStream().use { output -> input.copyTo(output) }
                                }
                            }
                            tempFiles.add(tempFile)

                            if (tempFile.length() > 0) {
                                val assetId = UUID.randomUUID()
                                val uploadRequest = UploadRequest(
                                    tenantId = tenantId.value,
                                    assetId = assetId,
                                    filename = fname,
                                    mimeType = mime,
                                    contentLength = tempFile.length()
                                )

                                val validation = assetUploadService.validateUpload(uploadRequest)
                                if (validation is ValidationResult.Valid) {
                                    val uploadResult = assetUploadService.upload(uploadRequest, tempFile.inputStream())
                                    if (uploadResult.validationError == null) {
                                        val asset = assetRepository.create(
                                            tenantId,
                                            CreateAssetRequest(
                                                type = validation.assetType,
                                                name = fname,
                                                mimeType = mime,
                                                storageKey = uploadResult.storageKey,
                                                fileSizeBytes = uploadResult.sizeBytes,
                                                checksumSha256 = uploadResult.checksumSha256,
                                                createdBy = session.subject
                                            )
                                        )
                                        assetRepository.update(tenantId, asset.id.value, UpdateAssetRequest(status = AssetStatus.READY))
                                        successCount++
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }
            } finally {
                tempFiles.forEach { it.delete() }
            }

            call.respondRedirect("/admin/assets?success=${java.net.URLEncoder.encode("$successCount file(s) uploaded successfully", "UTF-8")}")
        }

        // POST /admin/assets/:id/replace - Replace file for existing asset
        post("/{id}/replace") {
            val session = call.adminSession ?: run {
                call.respondRedirect("/admin/login")
                return@post
            }

            val tenantId = TenantId(session.tenantId)
            val assetId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            if (assetId == null) { call.respondRedirect("/admin/assets"); return@post }

            val existingAsset = assetRepository.findById(tenantId, assetId)
            if (existingAsset == null) { call.respondRedirect("/admin/assets"); return@post }

            val multipart = call.receiveMultipart()
            var filename: String? = null
            var mimeType: String? = null
            var tempFile: File? = null

            try {
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            filename = part.originalFileName?.takeIf { it.isNotBlank() }
                            mimeType = part.contentType?.toString() ?: "application/octet-stream"
                            tempFile = File.createTempFile("replace-", ".tmp").also { tf ->
                                part.streamProvider().use { input ->
                                    tf.outputStream().use { output -> input.copyTo(output) }
                                }
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (filename == null || tempFile == null || tempFile!!.length() == 0L) {
                    call.respondRedirect("/admin/assets/$assetId?error=${java.net.URLEncoder.encode("Please select a file", "UTF-8")}")
                    return@post
                }

                val uploadRequest = UploadRequest(
                    tenantId = tenantId.value,
                    assetId = assetId,
                    filename = filename!!,
                    mimeType = mimeType!!,
                    contentLength = tempFile!!.length()
                )

                val validation = assetUploadService.validateUpload(uploadRequest)
                if (validation is ValidationResult.Invalid) {
                    call.respondRedirect("/admin/assets/$assetId?error=${java.net.URLEncoder.encode(validation.reason, "UTF-8")}")
                    return@post
                }

                val uploadResult = assetUploadService.upload(uploadRequest, tempFile!!.inputStream())
                if (uploadResult.validationError != null) {
                    call.respondRedirect("/admin/assets/$assetId?error=${java.net.URLEncoder.encode(uploadResult.validationError!!, "UTF-8")}")
                    return@post
                }

                // Update storage key, mime type, and file size - keep all other metadata
                assetRepository.update(tenantId, assetId, UpdateAssetRequest(
                    storageKey = uploadResult.storageKey,
                    mimeType = mimeType,
                    fileSizeBytes = uploadResult.sizeBytes,
                    checksumSha256 = uploadResult.checksumSha256
                ))

                call.respondRedirect("/admin/assets/$assetId?success=${java.net.URLEncoder.encode("File replaced successfully", "UTF-8")}")
            } finally {
                tempFile?.delete()
            }
        }

        // GET /admin/assets/:id/edit
        get("/{id}/edit") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@get }
            val tenantId = TenantId(session.tenantId)
            val assetId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            if (assetId == null) { call.respondRedirect("/admin/assets"); return@get }
            val asset = assetRepository.findById(tenantId, assetId)
            if (asset == null) { call.respondRedirect("/admin/assets"); return@get }
            val allTags = assetRepository.getAllTags(tenantId)
            call.respondHtml {
                assetEditView(session = session, asset = AssetEditModel(
                    id = asset.id.value, name = asset.name,
                    description = asset.description, tags = asset.tags,
                    approvalStatus = asset.approvalStatus,
                    expiresAt = asset.expiresAt?.toString()?.substringBefore("T"),
                    allTags = allTags
                ))
            }
        }

        // POST /admin/assets/:id/edit
        post("/{id}/edit") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@post }
            val tenantId = TenantId(session.tenantId)
            val assetId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            if (assetId == null) { call.respondRedirect("/admin/assets"); return@post }
            val params = call.receiveParameters()
            val name = params["name"]?.takeIf { it.isNotBlank() }
            val description = params["description"]
            val tags = params["tags"]
            val approvalStatus = params["approval_status"]
            val expiresAtStr = params["expires_at"]
            val expiresAt = expiresAtStr?.takeIf { s -> s.isNotBlank() }?.let { s ->
                runCatching { kotlinx.datetime.Instant.parse(s + "T00:00:00Z") }.getOrNull()
            }
            assetRepository.update(tenantId, assetId, UpdateAssetRequest(
                name = name,
                description = description,
                tags = tags,
                approvalStatus = approvalStatus,
                expiresAt = expiresAt
            ))
            call.respondRedirect("/admin/assets/$assetId")
        }

        // GET /admin/assets/:id/download
        get("/{id}/download") {
            val session = call.adminSession ?: run { call.respondRedirect("/admin/login"); return@get }
            val tenantId = TenantId(session.tenantId)
            val assetId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            if (assetId == null) { call.respondRedirect("/admin/assets"); return@get }
            val asset = assetRepository.findById(tenantId, assetId)
            if (asset == null) { call.respondRedirect("/admin/assets"); return@get }
            // For local storage, redirect to the storage URL
            call.respondRedirect("/storage/${asset.storageKey}")
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
            val page = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val pageSize = 50

            val filters = AssetFilters(
                type = typeParam?.let { runCatching { AssetType.valueOf(it) }.getOrNull() },
                status = statusParam?.let { runCatching { AssetStatus.valueOf(it) }.getOrNull() },
                search = search?.takeIf { it.isNotBlank() }
            )

            // Fetch assets with pagination
            val (allAssets, totalCount) = assetRepository.findAllActivePaged(tenantId, pageSize, (page - 1) * pageSize)

            // Apply filters (on paged results — for full filtering, would need DB-level filters)
            var assets = allAssets
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
                    createdAt = asset.createdAt,
                    approvalStatus = asset.approvalStatus,
                    width = asset.width,
                    height = asset.height
                )
            }

            // Get quota info
            val usedBytes = assetRepository.getTotalStorageBytes(tenantId)
            val quota = QuotaInfo(
                usedBytes = usedBytes,
                limitBytes = DEFAULT_STORAGE_LIMIT_BYTES
            )

            val totalPages = ((totalCount + pageSize - 1) / pageSize).toInt().coerceAtLeast(1)
            val queryParams = buildString {
                if (filters.type != null) append("&type=${filters.type!!.name}")
                if (filters.status != null) append("&status=${filters.status!!.name}")
                if (filters.search != null) append("&search=${filters.search}")
            }.let { if (it.isNotEmpty()) "?${it.removePrefix("&")}" else "" }

            val pagination = com.playoutedge.server.views.PaginationInfo(
                currentPage = page,
                totalPages = totalPages,
                totalItems = totalCount,
                basePath = "/admin/assets",
                queryParams = queryParams
            )

            call.respondHtml {
                assetsListView(
                    session = session,
                    assets = assetItems,
                    quota = quota,
                    filters = filters,
                    pagination = pagination
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
