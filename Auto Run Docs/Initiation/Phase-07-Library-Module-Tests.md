# Phase 07: Library Module Tests

This phase adds comprehensive tests for the shared library modules: auth, storage, domain, contracts, and observability. These libraries are used across the server and Android app - thorough testing ensures reliability at the foundation level.

## Tasks

- [ ] Expand auth library tests:
  - Enhance `/libs/auth/src/test/kotlin/com/playoutedge/auth/JwtServiceTest.kt`:
    - Test token generation with all claim types
    - Test token validation edge cases (clock skew, near-expiry)
    - Test refresh token generation and validation
    - Test token revocation (if implemented)
  - Enhance `/libs/auth/src/test/kotlin/com/playoutedge/auth/PasswordServiceTest.kt`:
    - Test password hashing with various input lengths
    - Test password verification with correct/incorrect passwords
    - Test handling of empty and null passwords
    - Test hash uniqueness (same password, different hashes)
  - Create `/libs/auth/src/test/kotlin/com/playoutedge/auth/RolePermissionsTest.kt`:
    - Test permission checks for each role (admin, operator, viewer, device)
    - Test role hierarchy and inheritance
    - Test permission constants

- [ ] Expand storage library tests:
  - Enhance `/libs/storage/src/test/kotlin/com/playoutedge/storage/LocalStorageServiceTest.kt`:
    - Test file upload with various sizes
    - Test file download and streaming
    - Test signed URL generation and expiration
    - Test file deletion
    - Test directory creation and cleanup
    - Test checksum calculation and verification
  - Create `/libs/storage/src/test/kotlin/com/playoutedge/storage/S3StorageServiceTest.kt`:
    - Test S3 client configuration
    - Test presigned URL generation
    - Test multipart upload handling
    - Test error handling for S3 failures
    - Use LocalStack or mock S3 client for testing
  - Create `/libs/storage/src/test/kotlin/com/playoutedge/storage/StorageFactoryTest.kt`:
    - Test factory creates correct implementation based on config
    - Test local storage selection
    - Test S3 storage selection

- [ ] Add domain library tests:
  - Create `/libs/domain/src/test/kotlin/com/playoutedge/domain/ChannelStatusTest.kt`:
    - Test valid status transitions
    - Test invalid status transitions
    - Test status enum values
  - Create `/libs/domain/src/test/kotlin/com/playoutedge/domain/DeviceStatusTest.kt`:
    - Test device status values
    - Test status display names
  - Create `/libs/domain/src/test/kotlin/com/playoutedge/domain/AssetTypeTest.kt`:
    - Test asset type detection from MIME types
    - Test asset type from file extension
    - Test supported formats validation
  - Create `/libs/domain/src/test/kotlin/com/playoutedge/domain/MediaValidationTest.kt`:
    - Test video format validation
    - Test image format validation
    - Test audio format validation
    - Test file size limits

- [ ] Add contracts library tests:
  - Create `/libs/contracts/src/test/kotlin/com/playoutedge/contracts/ErrorResponseTest.kt`:
    - Test error response serialization
    - Test error code constants
    - Test error message formatting
  - Create `/libs/contracts/src/test/kotlin/com/playoutedge/contracts/ApiContractTest.kt`:
    - Test request/response DTO serialization
    - Test validation annotations
    - Test nullable field handling

- [ ] Add observability library tests:
  - Create `/libs/observability/src/test/kotlin/com/playoutedge/observability/MetricsTest.kt`:
    - Test metric recording
    - Test counter increments
    - Test histogram recordings
    - Test tag application
  - Create `/libs/observability/src/test/kotlin/com/playoutedge/observability/LoggingTest.kt`:
    - Test structured logging
    - Test log context propagation
    - Test log level filtering

- [ ] Run all library tests and verify coverage:
  - Execute tests for each library:
    - `./gradlew :libs:auth:test`
    - `./gradlew :libs:storage:test`
    - `./gradlew :libs:domain:test`
    - `./gradlew :libs:contracts:test`
    - `./gradlew :libs:observability:test`
    - `./gradlew :libs:persistence:test`
  - Generate combined coverage report
  - Target: 85%+ coverage for library code
