package com.playoutedge.server.routes

import com.playoutedge.contracts.error.ApiException
import com.playoutedge.contracts.error.ErrorCode
import com.playoutedge.domain.enums.AssetStatus
import com.playoutedge.persistence.repositories.AssetRepository
import com.playoutedge.persistence.repositories.CreateAssetRequest
import com.playoutedge.persistence.repositories.QuotaService
import com.playoutedge.persistence.repositories.UpdateAssetRequest
import com.playoutedge.server.plugins.AUTH_ADMIN
import com.playoutedge.server.plugins.adminClaims
import com.playoutedge.server.plugins.tenantId
import com.playoutedge.storage.AssetUploadService
import com.playoutedge.storage.StorageService
import com.playoutedge.storage.UploadRequest
import com.playoutedge.storage.ValidationResult
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File
import java.util.UUID

@Serializable
data class AssetResponse(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val mimeType: String,
    val sizeBytes: Long?,
    val checksum: String?,
    val durationMs: Int?,
    val width: Int?,
    val height: Int?,
    val createdAt: String
)

@Serializable
data class AssetsListResponse(
    val assets: List<AssetResponse>,
    val total: Int
)

@Serializable
data class UploadResponse(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val storageKey: String,
    val checksum: String,
    val sizeBytes: Long
)

fun Route.assetsRoutes(
    assetRepository: AssetRepository,
    quotaService: QuotaService,
    assetUploadService: AssetUploadService,
    storageService: StorageService
) {
    authenticate(AUTH_ADMIN) {
        route("/api/admin/assets") {
            // POST /api/admin/assets/upload - Multipart upload
            post("/upload") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )
                val adminClaims = call.adminClaims ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing admin claims"
                )

                val multipart = call.receiveMultipart()
                var filename: String? = null
                var mimeType: String? = null
                var tempFile: File? = null

                try {
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            filename = part.originalFileName ?: "unnamed"
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

                if (filename == null || tempFile == null || tempFile!!.length() == 0L) {
                    throw ApiException.badRequest(
                        ErrorCode.VALIDATION_ERROR,
                        "No file provided"
                    )
                }

                val contentLength = tempFile!!.length()

                // Check quota
                val quotaCheck = quotaService.checkStorageQuota(tenantId, contentLength)
                if (!quotaCheck.allowed) {
                    throw ApiException.forbidden(
                        ErrorCode.TENANT_QUOTA_EXCEEDED,
                        quotaCheck.reason ?: "Storage quota exceeded"
                    )
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
                    throw ApiException.badRequest(
                        ErrorCode.ASSET_INVALID_FORMAT,
                        validation.reason
                    )
                }

                val assetType = (validation as ValidationResult.Valid).assetType

                // Upload to storage
                val uploadResult = assetUploadService.upload(
                    uploadRequest,
                    tempFile!!.inputStream()
                )

                uploadResult.validationError?.let { error ->
                    throw ApiException.badRequest(
                        ErrorCode.ASSET_INVALID_FORMAT,
                        error
                    )
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
                        createdBy = adminClaims.subject
                    )
                )

                // Mark as ready (no transcoding in MVP)
                assetRepository.update(tenantId, asset.id.value, UpdateAssetRequest(status = AssetStatus.READY))

                call.respond(HttpStatusCode.Created, UploadResponse(
                    id = asset.id.value.toString(),
                    name = asset.name,
                    type = assetType.name,
                    status = AssetStatus.READY.name,
                    storageKey = uploadResult.storageKey,
                    checksum = uploadResult.checksumSha256,
                    sizeBytes = uploadResult.sizeBytes
                ))
                } finally {
                    tempFile?.delete()
                }
            }

            // GET /api/admin/assets - List assets
            get {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val assets = assetRepository.findAllActive(tenantId)

                val response = AssetsListResponse(
                    assets = assets.map { asset ->
                        AssetResponse(
                            id = asset.id.value.toString(),
                            name = asset.name,
                            type = asset.type.name,
                            status = asset.status.name,
                            mimeType = asset.mimeType,
                            sizeBytes = asset.fileSizeBytes,
                            checksum = asset.checksumSha256,
                            durationMs = asset.durationMs,
                            width = asset.width,
                            height = asset.height,
                            createdAt = asset.createdAt.toString()
                        )
                    },
                    total = assets.size
                )

                call.respond(response)
            }

            // GET /api/admin/assets/{id} - Get single asset
            get("/{id}") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val assetId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid asset ID")

                val asset = assetRepository.findById(tenantId, assetId)
                    ?: throw ApiException.notFound(ErrorCode.ASSET_NOT_FOUND, "Asset not found")

                call.respond(AssetResponse(
                    id = asset.id.value.toString(),
                    name = asset.name,
                    type = asset.type.name,
                    status = asset.status.name,
                    mimeType = asset.mimeType,
                    sizeBytes = asset.fileSizeBytes,
                    checksum = asset.checksumSha256,
                    durationMs = asset.durationMs,
                    width = asset.width,
                    height = asset.height,
                    createdAt = asset.createdAt.toString()
                ))
            }

            // DELETE /api/admin/assets/{id} - Soft delete (archive)
            delete("/{id}") {
                val tenantId = call.tenantId ?: throw ApiException.unauthorized(
                    ErrorCode.TOKEN_INVALID,
                    "Missing tenant context"
                )

                val assetId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Invalid asset ID")

                val asset = assetRepository.archive(tenantId, assetId)
                    ?: throw ApiException.notFound(ErrorCode.ASSET_NOT_FOUND, "Asset not found")

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
