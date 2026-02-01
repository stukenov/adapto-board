## 1. Storage Module Setup (libs/storage)

- [x] 1.1 Create build.gradle.kts for libs/storage with AWS SDK dependencies
- [x] 1.2 Create StorageConfig data class with mode (LOCAL/S3), paths, and S3 credentials
- [x] 1.3 Create StorageService interface (put, delete, getSignedUrl, exists)
- [x] 1.4 Create StorageResult data class with key, checksum, size

## 2. Local Storage Implementation

- [x] 2.1 Implement LocalStorageService with filesystem operations
- [x] 2.2 Implement HMAC-based signed URL generation for local storage
- [x] 2.3 Create storage file serving route for local signed URLs

## 3. S3 Storage Implementation

- [x] 3.1 Implement S3StorageService using AWS SDK
- [x] 3.2 Implement S3 presigned URL generation

## 4. Upload Pipeline

- [x] 4.1 Create AssetUploadService with upload orchestration logic
- [x] 4.2 Implement SHA256 checksum calculation during streaming upload
- [x] 4.3 Implement basic media validation (format, mime type, size)

## 5. Repository Updates

- [x] 5.1 Add archived_at field to assets table (migration V011)
- [x] 5.2 Update AssetRepository interface with soft delete and archive methods
- [x] 5.3 Update AssetRepositoryImpl with new methods

## 6. Admin API Routes

- [x] 6.1 Create POST /api/admin/assets/upload endpoint with multipart handling
- [x] 6.2 Create GET /api/admin/assets endpoint (list with tenant filtering)
- [x] 6.3 Create GET /api/admin/assets/{id} endpoint
- [x] 6.4 Create DELETE /api/admin/assets/{id} endpoint (soft delete)

## 7. Integration

- [x] 7.1 Wire StorageService into Application.module() with config-based selection
- [x] 7.2 Register asset routes in server routing
- [x] 7.3 Verify build compiles with all assets-management infrastructure
