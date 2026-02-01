## Context

Assets (video/images) are the core content in Playout Edge. The current state:
- `assets` and `asset_versions` tables exist in DB with necessary fields (checksum_sha256, storage_key, etc.)
- `AssetRepository` interface exists with basic CRUD + tenant isolation
- `AssetStatus` enum: UPLOADING → PROCESSING → READY/REJECTED/ARCHIVED
- `libs/storage` module declared in settings.gradle.kts but empty
- No upload endpoints or storage abstraction implemented yet

Constraints:
- Must work with both local filesystem and S3/MinIO
- Must respect tenant quotas (via existing QuotaService)
- Player devices need time-limited signed URLs
- No external transcoding service in MVP (validation only, no transcoding)

## Goals / Non-Goals

**Goals:**
- Storage abstraction layer (LOCAL/S3) with consistent interface
- Multipart upload endpoint with streaming to storage
- SHA256 checksum calculation during upload
- Basic media validation (format, size, dimensions)
- Signed URL generation for player access
- Soft delete with ARCHIVED status

**Non-Goals:**
- Transcoding (video re-encoding) — future phase
- Chunked/resumable uploads — future enhancement
- CDN integration — future phase
- Asset tagging/categorization — separate change

## Decisions

### D1: Storage Abstraction in libs/storage

**Decision**: Create `StorageService` interface with `LocalStorageService` and `S3StorageService` implementations.

**Rationale**: Allows testing with local files while deploying with S3/MinIO. Single interface keeps business logic storage-agnostic.

**Alternatives**:
- Direct S3 SDK usage everywhere → Coupling, hard to test
- Abstract filesystem library → Overkill, we only need put/get/delete/signUrl

**Interface**:
```kotlin
interface StorageService {
    suspend fun put(key: String, content: InputStream, contentLength: Long): StorageResult
    suspend fun delete(key: String): Boolean
    suspend fun getSignedUrl(key: String, ttl: Duration): String
    suspend fun exists(key: String): Boolean
}
```

### D2: Storage Key Format

**Decision**: `{tenant_id}/{asset_id}/{filename}`

**Rationale**: Tenant isolation at storage level, easy to identify and cleanup orphaned files.

**Example**: `550e8400-e29b/7c9e6679-aab3/promo-video.mp4`

### D3: Checksum Calculation

**Decision**: Calculate SHA256 during upload streaming using `DigestInputStream`.

**Rationale**: No extra read pass needed. Checksum stored for integrity verification on player download.

**Alternatives**:
- Post-upload calculation → Extra storage read, slower
- Client-provided checksum → Can't trust client

### D4: Media Validation

**Decision**: Basic validation using file headers/magic bytes + metadata extraction via Ktor's multipart parsing. No external tools in MVP.

**Rationale**: Keep simple for MVP. Can add FFprobe integration later for detailed validation.

**Validation rules**:
- Video: MP4 container, H.264/AAC codecs (by extension/mime for MVP)
- Images: PNG/JPEG only
- Max size: from tenant quotas
- Dimensions: extracted from first bytes where possible

### D5: Signed URL Scheme

**Decision**: For LOCAL storage, generate HMAC-signed URLs served by our server. For S3, use native presigned URLs.

**Rationale**: S3 has built-in presigning. Local storage needs our own scheme.

**LOCAL URL format**: `/api/storage/{storage_key}?token={hmac}&expires={timestamp}`

**S3**: Native presigned URL from AWS SDK

### D6: Upload Flow

**Decision**: Single multipart POST endpoint that streams to storage.

```
POST /api/admin/assets/upload
Content-Type: multipart/form-data

1. Check quota (QuotaService.checkStorageQuota)
2. Create asset record with UPLOADING status
3. Stream multipart to storage, calculate checksum
4. Update asset: checksumSha256, fileSizeBytes, status=PROCESSING
5. Validate media (format, dimensions)
6. Update status: READY or REJECTED with reason
7. Return asset DTO
```

### D7: Soft Delete

**Decision**: Use existing ARCHIVED status for soft delete. Add `archived_at` timestamp field.

**Rationale**: ARCHIVED status exists, semantics match. Separate purge job can cleanup after retention period.

## Risks / Trade-offs

**[Risk] Large file memory pressure** → Mitigation: Use streaming with Ktor's `receiveMultipart()`, don't buffer entire file

**[Risk] Incomplete uploads leave orphaned storage** → Mitigation: Cleanup job removes UPLOADING assets older than 1 hour

**[Risk] Validation rejects after storage write** → Mitigation: Acceptable for MVP; rejected files cleaned up by purge job

**[Trade-off] No detailed video validation** → Accept for MVP; can add FFprobe later

**[Trade-off] LOCAL signed URLs require server routing** → Acceptable for dev/small deployments; production uses S3
