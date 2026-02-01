# Media Validation Rules — Design

## Overview

Asset validation ensures media files are compatible with Android TV devices, controls storage costs, and guarantees predictable playback quality.

## Validation Rules

### System Defaults (domain constants)

```kotlin
object MediaValidationDefaults {
    // Video constraints
    val MAX_VIDEO_RESOLUTION = 1920 to 1080  // Full HD
    val MAX_VIDEO_BITRATE_BPS = 15_000_000L   // 15 Mbps
    val MAX_VIDEO_DURATION_SEC = 3600         // 1 hour
    val MAX_VIDEO_SIZE_BYTES = 2_147_483_648L // 2 GB
    val ALLOWED_VIDEO_CODECS = setOf("h264", "avc")
    val ALLOWED_VIDEO_CONTAINERS = setOf("mp4")

    // Image constraints
    val MAX_IMAGE_RESOLUTION = 3840 to 2160  // 4K
    val MAX_IMAGE_SIZE_BYTES = 20_971_520L    // 20 MB
    val ALLOWED_IMAGE_FORMATS = setOf("png", "jpeg", "jpg")
}
```

### Rejection Reasons

```kotlin
enum class RejectionReason(val message: String) {
    UNSUPPORTED_FORMAT("File format is not supported"),
    UNSUPPORTED_CONTAINER("Only MP4 video files are supported"),
    UNSUPPORTED_CODEC("Video codec must be H.264"),
    RESOLUTION_TOO_HIGH("Resolution exceeds maximum allowed"),
    BITRATE_TOO_HIGH("Bitrate exceeds maximum allowed"),
    FILE_TOO_LARGE("File size exceeds maximum allowed"),
    DURATION_TOO_LONG("Duration exceeds maximum allowed"),
    CORRUPT_FILE("File appears to be corrupted or unreadable")
}
```

## Validation Pipeline

### Step 1: Extension Check
- Verify file extension is in allowed list
- Quick fail for unsupported formats

### Step 2: Content Type Verification
- Check MIME type from upload
- Verify matches extension

### Step 3: Basic Metadata (Phase 1)
- File size validation
- Store MIME type and extension

### Step 4: Media Metadata (Future - requires ffprobe)
- Parse video duration, resolution, bitrate
- Validate codec and container
- Calculate checksum

## Components

### Domain Layer
- `MediaValidationDefaults` - System-wide limits
- `RejectionReason` enum with messages
- `ValidationResult` sealed class

### Storage Layer
- `MediaValidator` interface
- `BasicMediaValidator` - Extension and size checks
- Future: `FfprobeMediaValidator` - Full metadata extraction

### Server Layer
- Update asset upload to validate before saving
- Store rejection reason on failed assets

## API Changes

### Asset Response Enhancement
Add `rejectionReason` field to asset responses when status is REJECTED.

```json
{
  "id": "uuid",
  "status": "REJECTED",
  "rejectionReason": "RESOLUTION_TOO_HIGH",
  "rejectionMessage": "Resolution exceeds maximum allowed (1920x1080)"
}
```

## Scope

Phase 1 (this implementation):
- Add validation constants and rejection reasons to domain
- Basic extension/size validation in upload service
- Store rejection reason on assets

Phase 2 (future):
- ffprobe integration for video metadata
- Tenant-configurable policies
- Automatic transcoding
