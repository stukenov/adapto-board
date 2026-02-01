# Media Validation Rules — Tasks

## Domain Layer

- [x] Create MediaValidationDefaults object with limits
- [x] Create RejectionReason enum with human-readable messages
- [x] Create ValidationResult sealed class
- [x] Create MediaMetadata data class

## Storage Layer

- [x] Create MediaValidator interface
- [x] Create BasicMediaValidator for extension/size checks
- [x] Update AssetUploadService to use MediaValidator

## Persistence Layer

- [x] rejectionReason column already exists in Assets table
- [x] rejectionReason already mapped in AssetEntity

## Server Layer

- [x] Asset upload already validates via AssetUploadService
- [x] Validation uses new MediaValidationDefaults

## Verification

- [x] Verify build compiles
