# Media Validation Rules — Tasks

## Domain Layer
- [x] Create MediaValidationDefaults constants
- [x] Create RejectionReason enum
- [x] Create ValidationResult sealed class

## Storage Layer
- [x] Create MediaValidator interface
- [x] Implement BasicMediaValidator (extension/size checks)

## Server Layer
- [x] Update asset upload to validate files
- [x] Add rejectionReason to asset responses
- [x] Handle validation errors gracefully

## Persistence Layer
- [x] Add rejection_reason column to assets table
- [x] Update AssetEntity with rejection field

## Verification
- [x] Verify build compiles
